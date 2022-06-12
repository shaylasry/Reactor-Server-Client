package bgu.spl.net.command;

public class KdamCheck extends Command{

    public KdamCheck(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {
        return data.kdamCheck(admin,Integer.parseInt(this.getStrArray()[1]));
    }
}
