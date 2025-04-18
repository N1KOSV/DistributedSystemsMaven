package org.example;

import java.io.IOException;
import java.net.*;

public class WorkerServer{
    
    int number;
    
    public WorkerServer(int number){
        this.number = number;
    }
    
    void openServer () {
        try (ServerSocket serverSocket = new ServerSocket(5001)) {
            System.out.println("Worker Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Worker connected" + socket.getInetAddress());

                Thread w1 = new ActionsForWorker(new Socket("127.0.0.1", 5005));
                w1.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*public static void main(String [] args){
        new WorkerServer(number).openServer();
    }*/
}
