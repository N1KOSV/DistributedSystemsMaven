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
            if (situation.equals("1")) {
                try {
                    Socket Workersocket = new Socket("127.0.0.1", 5001);

                    ObjectOutputStream WorkerOut = new ObjectOutputStream(Workersocket.getOutputStream());
                    ObjectInputStream MasterIn = new ObjectInputStream(socket.getInputStream());
                    WorkerOut.writeObject(MasterIn.readObject().toString());
                    WorkerOut.flush();
                    Workersocket.close();
                    socket.close();
                    stopThread();
                } catch (IOException e) {
                    System.out.println("Actions for master failed");
                    e.printStackTrace();
                    stopThread();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    // First receive data from Reducer
                    ObjectInputStream ReducerIn = new ObjectInputStream(socket.getInputStream());
                    String received = (String) ReducerIn.readObject();
                    System.out.println("ActionsForMaster received from Reducer: " + received);

                    // Now connect to Master.java and forward the data
                    Socket masterSocket = new Socket("127.0.0.1", 5013);
                    ObjectOutputStream masterOut = new ObjectOutputStream(masterSocket.getOutputStream());
                    masterOut.flush();
                    masterOut.writeObject(received);
                    masterOut.flush();
                    System.out.println("ActionsForMaster forwarded data to Master");

                    // Send acknowledgment back to Reducer if needed
                    ObjectOutputStream ReducerOut = new ObjectOutputStream(socket.getOutputStream());
                    ReducerOut.flush();
                    ReducerOut.writeObject("Received");
                    ReducerOut.flush();

                    // Close resources
                    masterOut.close();
                    masterSocket.close();
                    ReducerIn.close();
                    ReducerOut.close();
                    socket.close();
                    stopThread();
                }
                catch(Exception e) {
                    System.out.println("Actions for master failed 2");
                    e.printStackTrace();
                    stopThread();
                }
            }
        }
    }
}