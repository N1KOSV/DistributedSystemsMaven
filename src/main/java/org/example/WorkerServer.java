package org.example;

import java.io.IOException;
import java.net.*;

public class WorkerServer{
    void openServer () {
        try (ServerSocket serverSocket = new ServerSocket(5001)) {
            System.out.println("Worker Server is listening on port " + serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Worker connected" + socket.getInetAddress());

                Thread m = new ActionsForWorker(socket);
                m.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String [] args){
        new WorkerServer().openServer();

    }
}
