package org.example;

import java.io.*;
import java.net.Socket;

public class ActionsForMaster extends Thread {
    Socket socket;
    String situation;

    public ActionsForMaster(Socket socket, String number) {
        this.socket = socket;
        this.situation = number;
    }

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
                if (situation.equals("1")) {
                Socket Workersocket = new Socket("127.0.0.1", 5001);
                
                    ObjectOutputStream WorkerOut = new ObjectOutputStream(Workersocket.getOutputStream());

                    WorkerOut.writeObject("Raw Data");
                    WorkerOut.flush();
                    Workersocket.close();
                    socket.close();
                    //} catch (IOException | ClassNotFoundException e) {

                } else {
                    Socket ReducerSocket = new Socket("127.0.0.2", 5002);
                    ObjectInputStream ReducerIn = new ObjectInputStream(ReducerSocket.getInputStream());
                    System.out.println("Connected to Reducer");
                    System.out.println("You can see me");
                    String received = (String) ReducerIn.readObject();
                    System.out.println("Master received from Reducer: " + received);
                    String reducedData = received + " Processed from Reducer";
                    System.out.println(reducedData);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(reducedData);
                    out.flush();
                    System.out.println("Reducer: Sent processed data");
                    socket.close();
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Actions for master failed");
                stopThread();
                e.printStackTrace();
            }
        }
    }
}