package bgu.spl.net.impl.echo;

import bgu.spl.net.api.MessageEncoderDecoder;

import bgu.spl.net.command.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CommandEncoderDecoder implements MessageEncoderDecoder<Command> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    boolean complexMessageType1 = false; // OPs: 1,2,3 : 2 bytes + string + 0 + string + 0
    boolean complexMessageType2 = false;//OPs: 8 : 2 bytes + string + 0
    boolean complexMessageType3 = false;//Ops: 5,6,7,9,10 : 2 bytes + 2 bytes
    int zeroCharCounter = 0; // Relevant to complexMessageType1 and complexMessageType2
    short opCode;


    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    @Override
    public Command decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        pushByte(nextByte);
        if(this.len == 2){
            byte[] op = {this.bytes[0], this.bytes[1]};
            this.opCode = bytesToShort(op);
//            Class c =commands.get(opCode);// Continue later
            if(this.opCode >= 1 && this.opCode <= 3){
                this.complexMessageType1 = true;
            }
            else if(this.opCode == 4 || this.opCode == 11){
                this.len = 0;
                this.bytes = new byte[1 << 10];
                if(this.opCode == 4) {
                    return new Logout(this.opCode + "");
                }
                else {
                    return new MyCourses(this.opCode + "");
                }

            }
            else if(opCode == 8){
                this.complexMessageType2 = true;
            }
            else {
                this.complexMessageType3 = true;
            }

        }
        if(this.len > 2 ) {
            if(nextByte == '\0') {
                this.zeroCharCounter++;
            }
            if (this.complexMessageType2 && this.zeroCharCounter == 1) {
                return new StudentStat(popString());
            }
            else if (this.complexMessageType1 && this.zeroCharCounter == 2) {
                if (this.opCode == 1) {
                    return new AdminReg(popString());
                }
                if (this.opCode == 2) {
                    return new StudentReg(popString());
                }
                else if (this.opCode == 3) {
                    return new Login(popString());
                }
            } else if (this.len == 4 && this.complexMessageType3) {
                byte[] num = {this.bytes[2], this.bytes[3]};
                short commandNum = bytesToShort(num);
                this.len = 0;
                this.complexMessageType3 = false;
                this.bytes = new byte[1 << 10];
                this.zeroCharCounter = 0;
                if (this.opCode == 5) {
                    return new CourseReg(this.opCode + " " + commandNum);
                }
                if (this.opCode == 6) {
                    return new KdamCheck(this.opCode + " " + commandNum);
                }
                if (this.opCode == 7) {
                    return new CourseStat(this.opCode + " " + commandNum);
                }
                if (this.opCode == 9) {
                    return new IsReg(this.opCode + " " + commandNum);
                } else if (this.opCode == 10) {
                    return new UnRegister(this.opCode + " " + commandNum);
                }
            }
        }
        return null;
    }

    @Override
    public byte[] encode(Command message) {
        String[] temp = message.getMsg().split("\\|");//should change char?
        String[] temp1;
        short opCode = Short.parseShort(temp[0]);
        //changed name from num to commandNum to avoid confusion
        short commandNum = Short.parseShort(temp[1]);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(shortToBytes(opCode));
            outputStream.write(shortToBytes(commandNum));
            //"12|6 or 11|listOfKdamCourses
            if (opCode == 12) {
                if (commandNum == 6 || commandNum == 11) {
                    //check if there is need to delete ,
                    if (!temp[2].equals("[]")) {
                        temp1 = temp[2].substring(1, temp[2].length() - 1).split(",");
                    }
                    else temp1= new String[0];
                    outputStream.write(Arrays.toString(temp1).getBytes());
                    outputStream.write("\0".getBytes());
                }
                //"12|7|courseNum|courseName|seatsAvailable|maxSeats|listOfStudents()
                else if (commandNum == 7) {
                    outputStream.write((temp[2] + '\0').getBytes());
                    outputStream.write((temp[3] + '\0').getBytes());
                    outputStream.write((temp[4] + '\0').getBytes());
                    outputStream.write((temp[5] + '\0').getBytes());
                    String regList = temp[6].replaceAll("\\u0000","");
                    outputStream.write(regList.getBytes());
                    outputStream.write("\0".getBytes());
                }
                //"12|8|student.getUser()|listOfCourse
                else if (commandNum == 8) {
                    outputStream.write(temp[2].getBytes());
                    String courseList = temp[3].replaceAll("\\u0000","");
                    outputStream.write(courseList.getBytes());
                    outputStream.write("\0".getBytes());
                }
                //"12|9|NOT REGISTERED" or "12|9|REGISTERED"
                else if (commandNum == 9) {
                    temp1 = temp[2].split(" ");
                    for (String st : temp1) {
                        outputStream.write(st.getBytes());
                    }
                    outputStream.write("\0".getBytes());
                }
            }

            //If we get an error message the required data is already stored
            return outputStream.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }
    private void pushByte(byte nextByte) {
        if (this.len >= this.bytes.length) {
            this.bytes = Arrays.copyOf(this.bytes, this.len * 2);
        }
        this.bytes[this.len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        byte[] op = {this.bytes[0], this.bytes[1]};
        String finalResult = bytesToShort(op) +"";
        int prev = 2;
        for (int i = 2; i < this.len+1; i++) {
            if (this.bytes[i] == '\0') {
                finalResult = finalResult + " " + new String(this.bytes, prev, i - prev+1, StandardCharsets.US_ASCII);
                prev = i + 1;
            }
        }
        this.complexMessageType1 = false;
        this.complexMessageType2 = false;
        this.len = 0;
        this.zeroCharCounter = 0;
        this.bytes = new byte[1 << 10];
        return   finalResult;
    }

}
