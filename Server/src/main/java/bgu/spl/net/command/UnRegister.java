package bgu.spl.net.command;


public class UnRegister extends Command{

    public UnRegister(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {
        return data.unRegister(userName,Integer.parseInt(this.getStrArray()[1]));
    }
}
