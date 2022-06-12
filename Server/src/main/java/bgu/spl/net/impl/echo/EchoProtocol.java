package bgu.spl.net.impl.echo;

import bgu.spl.net.api.MessagingProtocol;
import java.time.LocalDateTime;

public class EchoProtocol implements MessagingProtocol<String> {
    //**should terminate to connection to the server
    //**this field will change if there protocol use not needed anymore
    //**it means we probably need to terminate when client sent Logout msg
    private boolean shouldTerminate = false;
    //**process will define the protocol handling with the msg
    //**and returns the proper msg  according to the request
    //**in out program we probably want the process to return ack msg
    //**if we succeed and Error msg if not
    @Override
    public String process(String msg) {
        shouldTerminate = "bye".equals(msg);
        System.out.println("[" + LocalDateTime.now() + "]: " + msg);
        return createEcho(msg);
    }

    private String createEcho(String message) {
        String echoPart = message.substring(Math.max(message.length() - 2, 0), message.length());
        return message + " .. " + echoPart + " .. " + echoPart + " ..";
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
