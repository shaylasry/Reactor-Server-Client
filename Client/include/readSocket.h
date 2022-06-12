//
// Created by spl211 on 06/01/2021.
//

#ifndef UNTITLED5_READSOCKET_H
#define UNTITLED5_READSOCKET_H
#include <stdlib.h>
#include "SocketConnectionHandler.h"
#include <boost/algorithm/string.hpp>//Allowing the use of string-manipulating functions
#include "boost/lexical_cast.hpp"//Allowing to cast from string to int\short and backwards

using namespace std;


class readSocket {
    private:
    SocketConnectionHandler* socketConnectionHandler;
    short bytesToShort(char* bytesArr);
    public:
    readSocket(SocketConnectionHandler* ch);
    void readRun();

};


#endif //UNTITLED5_READSOCKET_H
