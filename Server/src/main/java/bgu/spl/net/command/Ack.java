package bgu.spl.net.command;

public class Ack extends Command{

    public Ack(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {
        return null;
    }
}
