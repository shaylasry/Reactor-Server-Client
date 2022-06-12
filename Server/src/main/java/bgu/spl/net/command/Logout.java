package bgu.spl.net.command;

public class Logout extends Command{

    public Logout(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {
        return data.logout(userName);
    }
}
