package org.example;

import java.io.*;
import java.net.Socket;

public class ActionsForMaster extends Thread {
    Socket socket;

    public ActionsForMaster(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            //Connection with WorkerServer
            Socket Workersocket = new Socket("127.0.0.1", 5001);
            ObjectOutputStream WorkerOut = new ObjectOutputStream(Workersocket.getOutputStream());
            ObjectInputStream WorkerIn = new ObjectInputStream(Workersocket.getInputStream());

            WorkerOut.writeObject("Raw Data");
            WorkerOut.flush();

            //Connection with ReducerServer
            //Socket ReducerSocket = new Socket("127.0.0.1", 5002);
            //ObjectOutputStream ReducerOut = new ObjectOutputStream(ReducerSocket.getOutputStream());
            //ObjectInputStream ReducerIn = new ObjectInputStream(ReducerSocket.getInputStream());

            String received = (String) WorkerIn.readObject();
            System.out.println("Master received from Reducer: " + received);

            Workersocket.close();

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(received);
            out.flush();
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Actions for master failed");
            e.printStackTrace();
        }
    }
}