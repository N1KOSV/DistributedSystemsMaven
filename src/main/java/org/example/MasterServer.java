package org.example;

import java.io.IOException;
import java.net.*;

public class MasterServer{
    void openServer () {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Master Server is listening on port " + serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Master connected" + socket.getInetAddress());

                Thread m = new ActionsForMaster(socket);
                m.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String [] args){
        new MasterServer().openServer();
    }
}
