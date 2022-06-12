package bgu.spl.net.command;

public class IsReg extends Command{

    public IsReg(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {
        return data.isReg(userName,Integer.parseInt(this.getStrArray()[1]));
    }
}
