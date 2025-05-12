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
                //Store receivedFromWorker = (Store) workerIn.readObject();

                // Connect back to Master
                Socket masterSocket = new Socket("127.0.0.2", 5012); // Use correct MasterServer port
                System.out.println("Reducer: Connection received");
                ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());
                if(workerIn.readObject()!=null ) { if (workerIn.readObject() instanceof Store){ Store newStore = (Store) workerIn.readObject();
                    System.out.println(newStore.toString()); } }
                out.writeObject(workerIn.readObject()); // Send something immediately
                out.flush();

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
