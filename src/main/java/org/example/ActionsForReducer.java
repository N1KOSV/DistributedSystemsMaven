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
                ObjectInputStream workerIn = new ObjectInputStream(socket.getInputStream());
                Socket masterSocket = new Socket("127.0.0.2", 5012);
                System.out.println("Reducer: Connection received");
                ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());

                Object receivedObject;
                while ((receivedObject = workerIn.readObject()) != null) {
                    if (receivedObject instanceof Store) {
                        Store newStore = (Store) receivedObject;
                        System.out.println("STORE: " + newStore.toString());

                        // Forward the store to the master server
                        out.writeObject(newStore);
                        out.flush();
                    } else if (receivedObject instanceof String) {
                        System.out.println("Received message: " + receivedObject);
                    }
                }

                masterSocket.close();
                stopThread();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Actions for Reducer failed.");
                e.printStackTrace();
                stopThread();
            }
        }
    }
}
