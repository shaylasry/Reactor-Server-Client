package bgu.spl.net.impl.echo;

import bgu.spl.net.api.MessageEncoderDecoder;

import bgu.spl.net.command.AdminReg;
import bgu.spl.net.command.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;


public class LineMessageEncoderDecoder implements MessageEncoderDecoder<String> {
    private HashMap <Short,Class<? extends Command>> commands;
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    boolean complexMessageType1 = false; // OPs: 1,2,3 : 2 bytes + string + 0 + string + 0
    boolean complexMessageType2 = false;//OPs: 8 : 2 bytes + string + 0
    boolean complexMessageType3 = false;//Ops: 5,6,7,9,10 : 2 bytes
    int zeroCharCounter = 0; // Relevant to complexMessageType1 and complexMessageType2

    public LineMessageEncoderDecoder(){
        commands.put((short) 1, AdminReg.class);
    }

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
    public String decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if(len == 2){
            byte[] op = {bytes[0], bytes[1]};
            short opCode = bytesToShort(op);
            if(1<=opCode & 3>=opCode){
                complexMessageType1 = true;
            }
            else if(4==opCode | 11==opCode){
                len = 0;
                return ""+opCode;
            }
            else if(8 == opCode){
                complexMessageType2 = true;
            }
            else complexMessageType3 = true;
        }
        if(len == 4 && complexMessageType3){
            byte[] op = {bytes[0], bytes[1]};
            byte [] num = {bytes[2],bytes[3]};
            len = 0;
            return  bytesToShort(op)+" "+bytesToShort(num);
        }

        if(len > 2 && nextByte == '\0'){
            zeroCharCounter ++;
            if(complexMessageType2 && zeroCharCounter ==1){
                return popString();
            }
            if(complexMessageType1 && zeroCharCounter==2){
                return  popString();
            }
        }

        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(String message) {
        String [] temp = message.split("|");
        String [] temp1;
        short opCode = Short.parseShort(temp[0]);
        short num = Short.parseShort(temp[1]);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(shortToBytes(opCode));
            outputStream.write(shortToBytes(num));
        }catch (IOException e){}
        //"12|6 or 11|listOfKdamCourses
        if(num == 6 | num == 11){
            temp1 = temp[2].substring(1,temp[2].length()-1).split(",");
            try {
                for (String st : temp1) {
                    outputStream.write(shortToBytes(Short.parseShort(st)));
                }
            } catch (IOException e){e.printStackTrace();}
            return outputStream.toByteArray();
        }
        //"12|7|courseNum|courseName|seatsAvailable|maxSeats|listOfStudents()
        if(num == 7) {
            temp1 = temp[6].substring(1,temp[2].length()-1).split(",");
            try {
                outputStream.write(shortToBytes(Short.parseShort(temp[2])));
                outputStream.write(temp[3].getBytes());
                outputStream.write(Short.parseShort(temp[4]));
                outputStream.write(Short.parseShort(temp[5]));
                for (String st : temp1) {
                    outputStream.write(shortToBytes(Short.parseShort(st)));
                }
            } catch (IOException e){e.printStackTrace();}
            return outputStream.toByteArray();
        }
        //"12|8|student.getUser()|listOfCourse
        if(num == 8) {
            temp1 = temp[3].substring(1,temp[2].length()-1).split(",");
            try {
                outputStream.write(temp[2].getBytes());
                for (String st : temp1) {
                    outputStream.write(shortToBytes(Short.parseShort(st)));
                }
            } catch (IOException e){e.printStackTrace();}
            return outputStream.toByteArray();
        }
        return outputStream.toByteArray();
//        return (message + "\n").getBytes(); //uses utf8 by default
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String finalResult="";
        int prev = 2;
        byte[] op = {bytes[0], bytes[1]};
        finalResult = bytesToShort(op) + finalResult;
        for (int i = 2; i < len; i++) {
            if (bytes[i] == '\0') {
                finalResult = finalResult + " " + new String(bytes, prev, i - prev, StandardCharsets.US_ASCII);
                prev = i + 1;
            }
        }
        len = 0; zeroCharCounter = 0;
        return   finalResult;
    }

}
