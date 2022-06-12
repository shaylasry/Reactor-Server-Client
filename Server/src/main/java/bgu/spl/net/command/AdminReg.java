package bgu.spl.net.command;

public class AdminReg extends Command{

    public AdminReg(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {

         return data.adminReg(admin,userName,password);
    }
}
