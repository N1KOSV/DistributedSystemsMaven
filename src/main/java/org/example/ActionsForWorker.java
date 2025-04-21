package org.example;

import java.io.*;
import java.net.Socket;

class ActionsForWorker extends Thread {
    Socket socket;

    public ActionsForWorker(Socket socket) {
        this.socket = socket;
    }

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
                // Read data from Master (through existing socket)
                ObjectInputStream masterIn = new ObjectInputStream(socket.getInputStream());
                String receivedFromMaster = (String) masterIn.readObject();
                System.out.println("88");

                // Process data (map operation)
                String processedData = receivedFromMaster + " Processed from Worker";
                System.out.println(processedData);

                Socket reducerSocket = new Socket("127.0.0.2", 5002);
                ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream());
                reducerOut.writeObject(processedData);
                reducerOut.flush();

                // Clean up
                reducerSocket.close();
                //workerSocket.close();
                socket.close();
                stopThread();

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Actions for Worker failed.");
                e.printStackTrace();
                stopThread();
            }
        }
    }
}
