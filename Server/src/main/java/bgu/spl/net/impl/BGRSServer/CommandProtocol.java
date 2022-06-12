package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.command.*;
import bgu.spl.net.command.Error;

public class CommandProtocol implements MessagingProtocol<Command> {
    String userName;
    String password;
    boolean shouldTerminate = false;
    boolean admin;
    boolean loggedIn = false;

    @Override
    public Command process(Command msg) {
        //update registered and admin so we can tell later if client registered and if client is an admin or not
        //if client registered before we send error response
        if (msg instanceof AdminReg || msg instanceof StudentReg) {
            if (this.loggedIn){
                return new Error("13|" + msg.getStrArray()[0]);
            }
            return msg.commandProcess(msg instanceof AdminReg ,msg.getStrArray()[1],msg.getStrArray()[2]);
        }
        //check if user registered or logged in, if so return Error and if not update logged in
        else if (msg instanceof Login){
            if (loggedIn){
                return new Error("13|" + msg.getStrArray()[0]);
            }
            else {
                Command LogProcess = msg.commandProcess(false,msg.getStrArray()[1],msg.getStrArray()[2]);
                //to avoid an unregistered user to send commands to server while other client
                //logged in, the only user to save boolean logged in will be the one who succeed to log in
                if (LogProcess instanceof Ack){
                    this.userName = msg.getStrArray()[1];
                    this.password = msg.getStrArray()[2];
                    this.loggedIn = true;
                    if (LogProcess.getMsg().equals("admin")){
                        this.admin = true;
                    }
                    else if (LogProcess.getMsg().equals("student")){
                        this.admin = false;
                    }
                    return new Ack("12|3");
                }
                return LogProcess;
            }
        }
        //till this point we already checked for registration and login.
        //for other commands we should check if client logged in and return error if not
        else if (!this.loggedIn) {
            return new Error("13|" + msg.getStrArray()[0]);
        }
        //if no error returned so far so we can check if logout has been sent.
        else if (msg instanceof Logout) {
            {
                this.loggedIn = false;
                Command logoutProcess = msg.commandProcess(this.admin,this.userName,this.password);
                if(logoutProcess instanceof Ack) {
                    this.shouldTerminate = true;
                }
                return logoutProcess;
            }
        }
        //if no error occur until this point we can now process the command itself
        else {
            return msg.commandProcess(this.admin, this.userName, this.password);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}