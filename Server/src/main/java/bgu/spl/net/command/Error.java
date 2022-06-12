package bgu.spl.net.command;

public class Error extends Command{

    public Error(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {
        return null;
    }
}
