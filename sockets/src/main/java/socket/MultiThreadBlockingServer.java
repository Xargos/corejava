package socket;

import socket.handler.IOExceptionHandler;
import socket.handler.MagicHandler;
import socket.handler.PrintingHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadBlockingServer {

    public static void main(String... args) throws IOException {
        var server = new ServerSocket(8080);
        var handler = new IOExceptionHandler(new PrintingHandler<>(new MagicHandler()));
        while (true) {
            var clientSocket = server.accept();
            new Thread(() -> handler.handle(clientSocket)).start();
        }
    }


}
