package bgu.spl.net.command;

public class CourseReg extends Command{

    public CourseReg(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {
        return data.courseReg(admin,userName,Integer.parseInt(this.getStrArray()[1]));
    }
}
