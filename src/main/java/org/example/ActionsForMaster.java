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
                    stopThread();
                    e.printStackTrace();
                    stopThread();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        else{
                try{
                    ObjectInputStream ReducerIn = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream MasterOut = new ObjectOutputStream(socket.getOutputStream());
                    String received = (String) ReducerIn.readObject();
                    System.out.println("Master received from Reducer: " + received);
                    MasterOut.writeObject(received);
                    MasterOut.flush();
                    MasterOut.close();
                    socket.close();
                    stopThread();
                }
                catch(Exception e){
                    System.out.println("Actions for master failed 2");
                    stopThread();
                    e.printStackTrace();
                    stopThread();
                }
            }
        }
    }
}