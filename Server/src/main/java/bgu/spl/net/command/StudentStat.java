package bgu.spl.net.command;

public class StudentStat extends Command{

    public StudentStat(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String pasword) {
        return data.studentStat(admin, this.getStrArray()[1]);

    }
}
