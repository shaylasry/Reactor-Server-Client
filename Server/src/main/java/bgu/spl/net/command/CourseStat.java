package bgu.spl.net.command;

public class CourseStat extends Command{
    public CourseStat(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {

        return data.courseStat(admin,Integer.parseInt(this.getStrArray()[1]));
    }
}
