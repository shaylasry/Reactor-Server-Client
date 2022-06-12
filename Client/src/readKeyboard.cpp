//
// Created by spl211 on 06/01/2021.
//

#include "readKeyboard.h"
readKeyboard::readKeyboard(SocketConnectionHandler *ch):socketConnectionHandler(ch) {
}

void readKeyboard::shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}
void readKeyboard::keyboardRun(){
    while(true) {
        const short bufsize = 1024;
        char buf[bufsize];
        cin.getline(buf, bufsize);
        string line(buf);
        vector<string> input;
        boost::split(input, line, boost::is_any_of(" "));
        char op[2];
        if (input[0] == "ADMINREG") {
            shortToBytes(1, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            this->socketConnectionHandler->sendLine(input[1]);
            this->socketConnectionHandler->sendLine(input[2]);
        }
        else if (input[0] == "STUDENTREG") {
            shortToBytes(2, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            this->socketConnectionHandler->sendLine(input[1]);
            this->socketConnectionHandler->sendLine(input[2]);
        }
        else if (input[0] == "LOGIN") {
            shortToBytes(3, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            this->socketConnectionHandler->sendLine(input[1]);
            this->socketConnectionHandler->sendLine(input[2]);
        }
        else if (input[0] == "LOGOUT") {
            shortToBytes(4, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            break;
        }
        else if (input[0] == "COURSEREG") {
            shortToBytes(5, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            shortToBytes(boost::lexical_cast<short>(input[1]), op);
            this->socketConnectionHandler->sendBytes(op, 2);
        }
        else if (input[0] == "KDAMCHECK") {
            shortToBytes(6, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            shortToBytes(boost::lexical_cast<short>(input[1]), op);
            this->socketConnectionHandler->sendBytes(op, 2);
        }
        else if (input[0] == "COURSESTAT") {
            shortToBytes(7, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            shortToBytes(boost::lexical_cast<short>(input[1]), op);
            this->socketConnectionHandler->sendBytes(op, 2);
        }
        else if (input[0] == "STUDENTSTAT") {
            shortToBytes(8, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            this->socketConnectionHandler->sendLine(input[1]);
        }
        if (input[0] == "ISREGISTERED") {
            shortToBytes(9, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            shortToBytes(boost::lexical_cast<short>(input[1]), op);
            this->socketConnectionHandler->sendBytes(op, 2);
        }

        else if (input[0] == "UNREGISTER") {
            shortToBytes(10, op);
            this->socketConnectionHandler->sendBytes(op, 2);
            shortToBytes(boost::lexical_cast<short>(input[1]), op);
            this->socketConnectionHandler->sendBytes(op, 2);
        }
        else if (input[0] == "MYCOURSES") {
            shortToBytes(11, op);
            this->socketConnectionHandler->sendBytes(op, 2);
        }
       
    }
}





