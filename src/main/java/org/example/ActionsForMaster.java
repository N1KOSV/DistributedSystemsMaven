package org.example;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class ActionsForMaster extends Thread {
    Socket socket;

    public ActionsForMaster(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (
                ObjectOutputStream masterOut = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream masterIn = new ObjectInputStream(socket.getInputStream());
                ) {
            masterOut.flush();
            String data = (String) masterIn.readObject();
            System.out.println("[Master server got from Master: " + data);
            //Connection with WorkerServer
            Socket Workersocket = new Socket("127.0.0.1", 5001);
            ObjectOutputStream WorkerOut = new ObjectOutputStream(Workersocket.getOutputStream());
            ObjectInputStream WorkerIn = new ObjectInputStream(Workersocket.getInputStream());

            data = "Raw " + data;
            WorkerOut.writeObject(data);
            WorkerOut.flush();

            String received = (String) WorkerIn.readObject();
            System.out.println("AMaster received from Reducer: " + received);


            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(received);
            out.flush();

        } catch (ConnectException e) {
            System.out.println("Worker Server Not Found");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("Actions for Master failed");
            e.printStackTrace();
        }
    }
}