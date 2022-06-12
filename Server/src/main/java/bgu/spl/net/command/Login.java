package bgu.spl.net.command;

public class Login extends Command{

    public Login(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password)
    {
        return data.login(userName,password);
    }
}
