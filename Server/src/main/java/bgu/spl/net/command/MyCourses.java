package bgu.spl.net.command;

import bgu.spl.net.impl.BGRSServer.Database;

public class MyCourses extends Command{

    public MyCourses(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {
        return Database.getInstance().myCourses(userName);
    }
}
