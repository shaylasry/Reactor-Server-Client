#include <stdlib.h>
#include "SocketConnectionHandler.h"
#include <thread>
#include <boost/algorithm/string.hpp>//Allowing the use of string-manipulating functions
#include "boost/thread.hpp"
#include "readSocket.h"
#include "readKeyboard.h"


using namespace std;
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    SocketConnectionHandler socketConnectionHandler(host, port);
    if (!socketConnectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    readSocket readSock(&socketConnectionHandler);
    readKeyboard readKey(&socketConnectionHandler);
    thread sockThread(&readSocket::readRun,&readSock);
    //boost::thread sockThread(boost::bind(&readSocket::readRun,&readSock));
    readKey.keyboardRun();
    sockThread.join();
    return 0;

}
