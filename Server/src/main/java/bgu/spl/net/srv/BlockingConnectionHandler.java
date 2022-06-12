package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {
    //**Generic T for connection handler can be a String msg but also an object
    //**it means the connection handler get generic T and according to it will
    //**should get proper protocol and encoder decoder
    private final MessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, MessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }
    //**we should notice that the connection handler is Runnale and the Threads
    //**we use it while we start them.
    @Override
    public void run() {
        try (Socket sock = this .sock) { //just for automatic closing
            int read;
            //**while using connection handler we want it to keep
            //**incoming msg, it will get it from the socket given to it
            //**according to this program the coonection handler gets the client socket from
            //**the server, but client and server both have reference to this socket.
            //**so socket we use in and out buffers below to take the
            //**data from client socket for inputStream and use it for in
            //**so connection handler decode the msg and outputStream for out
            //**so the connection handler can update the client socket output.

            //**to sum it up, connection handler takes the messages that we got
            //**from the client handle it and then return to the client socket the
            //*response if needed (we should remember both, server and client has access to clientSocket).
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            //**in each iteration we check if we protocol should finish
            //**if the socket is connect (it wont be only if we call close method for curr connection handler)
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                //**in.read, read single byte from client input stream
                //this method returns value from 0 for success reading and negative
                //**number if failed to. (read as an integer int range 0 to 65353)
                //**we probably need to implement decodeNextByte to move each bye
                //**so we need to use casting from integer to byte
                T nextMessage = encdec.decodeNextByte((byte) read);
                //**we want to start the protocol process only if we got whole msg
                //**if not we will return null for this metho implemention and then
                //**nothing will happen
                if (nextMessage != null) {
                    T response = protocol.process(nextMessage);
                    //**we will only sent back respone if needed
                    if (response != null) {
                        out.write(encdec.encode(response));
                        out.flush();
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }
}
