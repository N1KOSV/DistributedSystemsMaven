package org.example;

import java.io.*;
import java.net.Socket;

public class ActionsForMaster extends Thread {
    Socket socket;

    public ActionsForMaster(Socket socket) {
        this.socket = socket;
    }

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
                //Connection with WorkerServer
                Socket Workersocket = new Socket("127.0.0.1", 5001);
                ObjectOutputStream WorkerOut = new ObjectOutputStream(Workersocket.getOutputStream());
                //ObjectInputStream WorkerIn = new ObjectInputStream(Workersocket.getInputStream());


                WorkerOut.writeObject("Raw Data");
                WorkerOut.flush();

                //Connection with ReducerServer
                System.out.println("About to connect to Reducer");
                Socket ReducerSocket = new Socket("127.0.0.1", 5002);
                System.out.println("Connected to Reducer");

                //ReducerSocket.setSoTimeout(50); // 5 second timeout
                System.out.println("Attempting to create ReducerIn stream");
                ObjectInputStream ReducerIn = new ObjectInputStream(ReducerSocket.getInputStream());
                System.out.println("You can see me");
                String received = (String) ReducerIn.readObject();
                
                System.out.println("Master received from Reducer: " + received);

                Workersocket.close();

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(received);
                out.flush();
                socket.close();
                stopThread();

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Actions for master failed");
                stopThread();
                e.printStackTrace();
            }
        }
    }
}