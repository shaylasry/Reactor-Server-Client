//
// Created by spl211 on 06/01/2021.
//

#ifndef UNTITLED5_READKEYBOARD_H
#define UNTITLED5_READKEYBOARD_H
#include <stdlib.h>
#include "SocketConnectionHandler.h"
#include <boost/algorithm/string.hpp>//Allowing the use of string-manipulating functions
#include "boost/lexical_cast.hpp"//Allowing to cast from string to int\short and backwards

using namespace std;


class readKeyboard {
    private:
    SocketConnectionHandler* socketConnectionHandler;
    void shortToBytes(short num, char* bytesArr);
    public:
    readKeyboard(SocketConnectionHandler* ch);
    void keyboardRun();
};


#endif //UNTITLED5_READKEYBOARD_H
