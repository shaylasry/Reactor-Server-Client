CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -pthread

all: BGRSclient
	g++ -o bin/BGRSclient bin/SocketConnectionHandler.o bin/readSocket.o bin/echoClient.o bin/readKeyboard.o $(LDFLAGS)

BGRSclient: bin/SocketConnectionHandler.o bin/readSocket.o bin/echoClient.o bin/readKeyboard.o

bin/SocketConnectionHandler.o: src/SocketConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/SocketConnectionHandler.o src/SocketConnectionHandler.cpp

bin/readSocket.o: src/readSocket.cpp
	g++ $(CFLAGS) -o bin/readSocket.o src/readSocket.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/readKeyboard.o: src/readKeyboard.cpp
	g++ $(CFLAGS) -o bin/readKeyboard.o src/readKeyboard.cpp

.PHONY: clean
clean:
	rm -f bin/*
