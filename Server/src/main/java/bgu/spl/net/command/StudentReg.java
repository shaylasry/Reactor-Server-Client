package bgu.spl.net.command;

import bgu.spl.net.impl.BGRSServer.Database;

public class StudentReg extends Command{

    public StudentReg(String msg) {
        super(msg);
    }

    @Override
    public Command commandProcess(boolean admin,String userName,String password) {
        return  Database.getInstance().studentReg(admin,this.getStrArray()[1],this.getStrArray()[2]);
    }
}
