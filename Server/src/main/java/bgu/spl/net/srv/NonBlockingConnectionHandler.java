package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NonBlockingConnectionHandler<T> implements ConnectionHandler<T> {

    private static final int BUFFER_ALLOCATION_SIZE = 1 << 13; //8k
    private static final ConcurrentLinkedQueue<ByteBuffer> BUFFER_POOL = new ConcurrentLinkedQueue<>();

    private final MessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    //**pay attention, writeQueue will be concurrent to prevent scenario when reactor and working thread working at the same time.
    private final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
    private final SocketChannel chan;
    private final Reactor reactor;

    public NonBlockingConnectionHandler(
            MessageEncoderDecoder<T> reader,
            MessagingProtocol<T> protocol,
            SocketChannel chan,
            Reactor reactor) {
        this.chan = chan;
        this.encdec = reader;
        this.protocol = protocol;
        this.reactor = reactor;
    }

    public Runnable continueRead() {
        //**using leaseBuffer will take byteBuffer from BUFFER_POOL.
        //**when we finish the use of this byteBuffer we weill return it (we rent it for the action and return when finished)
        ByteBuffer buf = leaseBuffer();

        //** try to read the msg from the clientChannel
        boolean success = false;
        try {
            //**chan is the reference to the clientChannel. if we couldn't read it the method return -1 value.
            success = chan.read(buf) != -1;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (success) {
            //**if we successed reading we should use flip() method to make new limit in current position and then change current position to 0.
            buf.flip();
            //**we want to return runnable task so we return the method below writing as lambda.
            //**it means this method will return task (run method for thread implantation) or null if something went wrong
            //**the lambda only define the method and to this point Main Thread (server) is still the Thread that runs the program.
            //**after return this lambda server will give it to workingThread
            return () -> {
                try {
                    while (buf.hasRemaining()) {
                        T nextMessage = encdec.decodeNextByte(buf.get());
                        if (nextMessage != null) {
                            T response = protocol.process(nextMessage);
                            if (response != null) {
                                //**if response is needed, while working thread run this method
                                //**writeQueue is CH instance that keeps the response client should get
                                //**working thread will first take the response and encode it to byte array. when working with channels we need to use byteBUffer
                                //**so we use method wrap who takes byte array and make it byte Buffer and then we add it to writeQueue of CH.
                                writeQueue.add(ByteBuffer.wrap(encdec.encode(response)));
                                //**working thread use the line below to inform server he should pou READ and WRITE OP in the curr clientChannel(green note)
                                reactor.updateInterestedOps(chan, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            }
                        }
                    }
                } finally {
                    //**releaseBuffer returns the buff back to the BUFFER_POOL, in other words, rent time is over.
                    releaseBuffer(buf);
                }
            };
        } else {
            releaseBuffer(buf);
            close();
            return null;
        }

    }
    //**close will close and delete the chan from the selector not to be used again
    public void close() {
        try {
            chan.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isClosed() {
        return !chan.isOpen();
    }

    public void continueWrite() {
        while (!writeQueue.isEmpty()) {
            try {
                //**as long as writeQueue is not empty Main thread(server) will use peek to take the byteBuffer at the top
                //**peek won't remove the top byteBuffer.
                ByteBuffer top = writeQueue.peek();
                chan.write(top);
                //**if after using write server still didn't wrote all the message we will use return and stop trying until
                //**selector let him know that there is free space again. because we didn't remove the byteBuffer from the top
                //**the top byteBuffer keep currentPosition from the last time.
                if (top.hasRemaining()) {
                    return;
                } else {
                    //**if we finish writing all the byteBuffer we can now remove it from writeQueue
                    writeQueue.remove();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                close();
            }
        }

        if (writeQueue.isEmpty()) {
            //**if we finished handling the client we close the clientChannel(remove it from selector)
            if (protocol.shouldTerminate()) close();
                 //**if we still waiting for msg(protocol shouldTerminate) and there is nothing to write
                // **Main Thread(server) will update selector by removing the OP_Write for this channel socket
            else reactor.updateInterestedOps(chan, SelectionKey.OP_READ);
        }
    }

    private static ByteBuffer leaseBuffer() {
        //**BUFFER_POOL keeps all Buffer that made for the curr CH
        //**poll action try to get byteBuffer from the pool
        ByteBuffer buff = BUFFER_POOL.poll();
        //**if pool is empty(can happen for first iteration or if all byteBuffer is now is use) we create new byteBuffer and return it.
        //**notice that after creation even if the byteBuffer wasn't in the pool when CH finish using it CH will put it in the BUFFER_POOL
        if (buff == null) {
            return ByteBuffer.allocateDirect(BUFFER_ALLOCATION_SIZE);
        }
        //**if byteBuffer available and no need to create new one we make sure it's clean by using method clear and clean all data from the last use.
        buff.clear();
        return buff;
    }

    private static void releaseBuffer(ByteBuffer buff) {
        BUFFER_POOL.add(buff);
    }

}
