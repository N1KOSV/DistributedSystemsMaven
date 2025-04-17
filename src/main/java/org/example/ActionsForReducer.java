package org.example;

import java.io.*;
import java.net.Socket;

class ActionsForReducer extends Thread {
   Socket socket;

    public ActionsForReducer(Socket socket) {
       this.socket = socket;
    }

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
                //Connection with MasterServer
                // Receive data from Worker
                ObjectInputStream workerIn = new ObjectInputStream(socket.getInputStream());
                //ObjectOutputStream WorkerOut = new ObjectOutputStream(socket.getOutputStream());
                String receivedFromWorker = (String) workerIn.readObject();

                // Process data (reduce operation)
                String reducedData = receivedFromWorker + " Processed from Reducer";
                System.out.println(reducedData);
                // Connect back to Master
                Socket masterSocket = new Socket("127.0.0.2", 5012); // Use correct MasterServer port
                System.out.println("Reducer: Connection received");
                ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());
                out.writeObject(reducedData); // Send something immediately
                out.flush();
                System.out.println("Reducer: Sent initial data");

                // Clean up
                //WorkerOut.close();
                //masterSocket.close();
                //socket.close();
                stopThread();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Actions for Reducer failed.");
                e.printStackTrace();
                stopThread();
            }
        }
    }
}
