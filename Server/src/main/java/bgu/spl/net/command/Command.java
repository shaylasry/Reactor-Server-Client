package bgu.spl.net.command;

import bgu.spl.net.impl.BGRSServer.Database;

public abstract class Command{
    private String msg;
    private String [] strArray;
    protected Database data;

    public Command(String msg){
        this.msg = msg;
        this.strArray = msg.split(" ");
        data = Database.getInstance();
    }
    public abstract Command commandProcess(boolean admin,String userName,String password);

    public String getMsg() {
        return msg;
    }

    public String[] getStrArray() {
        return strArray;
    }

    public Database getData() {
        return data;
    }
}
