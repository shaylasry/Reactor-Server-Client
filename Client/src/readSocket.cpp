//
// Created by spl211 on 06/01/2021.
//

#include "readSocket.h"
#include <algorithm>
#include <string>
using namespace std;
readSocket::readSocket(SocketConnectionHandler *ch):socketConnectionHandler(ch) {
}

short readSocket::bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}


void readSocket::readRun() {
    while(true) {
        char *opAns = new char[2];
        this->socketConnectionHandler->getBytes(opAns, 2);
        short op = bytesToShort(opAns);
        delete[] opAns;
        if (op == 13) {
            char *opErr = new char[2];
            this->socketConnectionHandler->getBytes(opErr, 2);
            cout << "ERROR " + to_string(bytesToShort(opErr)) << endl;
            delete[] opErr;
        } else if (op == 12) {
            char *opAck = new char[2];
            this->socketConnectionHandler->getBytes(opAck, 2);
            short opack = bytesToShort(opAck);
            delete [] opAck;
            if (opack == 6 || opack == 11) {
                string temp = "";
                this->socketConnectionHandler->getLine(temp);
                string newtemp ="";
                for (int i = 0; i < temp.length(); i++) {
                    if (temp[i] != ' ') {
                        newtemp += temp[i];
                    }
                }
                cout << "ACK " + to_string(opack) + '\n' + newtemp << endl;
            } else if(opack == 9){
	         	string temp = "";
                this->socketConnectionHandler->getLine(temp);
                if(temp.compare("NOTREGISTERED") == 0){
                    cout << "ACK " + to_string(opack) + '\n' + "NOT REGISTERED" << endl;}
                else cout << "ACK " + to_string(opack) + '\n' + temp << endl;
            } else if (opack == 7) {
                string courseNum = "";
                this->socketConnectionHandler->getLine(courseNum);
                string courseName = "";
                this->socketConnectionHandler->getLine(courseName);
                string numOfSeatAvailable = "";
                this->socketConnectionHandler->getLine(numOfSeatAvailable);
                string maxNumOfSeats = "";
                this->socketConnectionHandler->getLine(maxNumOfSeats);
                string listOfStudents = "";
                this->socketConnectionHandler->getLine(listOfStudents);
                string newtemp ="";
                for (int i = 0; i < listOfStudents.length(); i++) {
                    if (listOfStudents[i] != ' ') {
                        newtemp += listOfStudents[i];
                    }
                }
                string ans = "ACK " + to_string(opack) + '\n';
                ans = ans + "Course: (" + courseNum + ") " + courseName + '\n';
                ans = ans + "Seats Available: " + numOfSeatAvailable + "/" + maxNumOfSeats + '\n';
                cout << ans +"Students Registered: "+ newtemp << endl;
            } else if (opack == 8) {
                string student = "";
                this->socketConnectionHandler->getLine(student);
                string courses = "";
                this->socketConnectionHandler->getLine(courses);
                string newtemp ="";
                for (int i = 0; i < courses.length(); i++) {
                    if (courses[i] != ' ') {
                        newtemp += courses[i];
                    }
                }
                cout << "ACK " + to_string(opack) + '\n' +"Student: "+ student + '\n' +"Courses: "+ newtemp << endl;
            } else if (opack == 4) {
                  cout<<"ACK "+to_string(opack)<<endl;
                  break;
            }
            else{ cout<<"ACK "+to_string(opack)<<endl;}
        }
    }
}




