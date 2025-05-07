package org.example;

import java.io.*;
import java.net.Socket;

class ActionsForWorker extends Thread {
    Socket socket;

    public ActionsForWorker(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            //Connection with ReducerServer
            Socket ReducerSocket = new Socket("127.0.0.1", 5002);
            ObjectOutputStream ReducerOut = new ObjectOutputStream(ReducerSocket.getOutputStream());
            ObjectInputStream ReducerIn = new ObjectInputStream(ReducerSocket.getInputStream());

            //Connection with MasterServer
            ObjectOutputStream MasterOut = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream MasterIn = new ObjectInputStream(socket.getInputStream());

            String received = (String) MasterIn.readObject();
            System.out.println("AWorker received from Master: " + received);

            Socket WorkerSocket = new Socket("127.0.0.1", 5001);
            ObjectOutputStream WorkerOut = new ObjectOutputStream(WorkerSocket.getOutputStream());
            ObjectInputStream WorkerIn = new ObjectInputStream(WorkerSocket.getInputStream());



            ReducerSocket.close();
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Actions for Worker failed.");
            e.printStackTrace();
        }
    }
}
