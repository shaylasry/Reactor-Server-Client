package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<MessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;

    public BaseServer(
            int port,
            //**we should notice that this factory will create onlu one protocol
            //**and one encoder decoder for the whole server.
            //**it means that if we wan to create two deffrent protocols we should
            //**probably use 1 protocol as parent and extend it to 2 protocols
            //**lets call it user protocol and student protocol and admin protocol
            //**will extend user
            //**in main it will be very east to declare new supplier just by using lambda
            Supplier<MessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");
            //**the base server creates new socket and define sock for curr server
            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {
                //**when getting the accept msg sock will create new client socket
                //**this will be the socket for the client that asked to connect.
                //**the = is so the client can tell which socket belongs to him
                //**the = is so the client can tell which socket belongs to him
                //**so while we define the connection handler
                Socket clientSock = serverSock.accept();
                //**Base server will create new Connection Handler according
                //**EncoderDecoder and Protocol
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get());
                //**now connection handler will be the one
                //**to wait for msg from the client and preform
                //** them from its protocol and incoder decoder

                //**notice that execute will define by the threadpool later
                execute(handler);
                //**connection handler will get the msg from client and take it
                //**byte after byte. when finished(all msg has been read) he will sent the msg
                //**to the Protocol and Protocol will act according to msg
                //**after protocol finish if respone is needed protocol return the respone
                //**respone should be encoded first so the protocol send it to encoder decoder
                //**then encoder decoder will reutnr it to client socket and then client will get it
                //**while comunication reutrns.
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }
    //**in lecture there is a class extends BaseServer(threadpool) and we
    //**implement this method there
    //**here it looks like ActorThreadPool wont extend BaseServer but has execute method)
    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
