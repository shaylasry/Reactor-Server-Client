package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.echo.CommandEncoderDecoder;
import bgu.spl.net.srv.Server;


public class ReactorMain {

    public static void main(String[] args) {
        Server.reactor(Integer.parseInt(args[1]), Integer.parseInt(args[0]), CommandProtocol::new, CommandEncoderDecoder::new).serve();
    }
}
