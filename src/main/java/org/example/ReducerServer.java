package org.example;

import java.io.IOException;
import java.net.*;

public class ReducerServer{
    void openServer () {
        try (ServerSocket serverSocket = new ServerSocket(5002)) {
            System.out.println("Reducer Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Reducer connected " + socket.getInetAddress());

                Thread m = new ActionsForReducer(socket);
                m.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String [] args){
        new ReducerServer().openServer();
    }
}
