package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ActionsForWorker extends Thread {
    Socket socket;
    static List<Store> myStores = new ArrayList<Store>();

    public ActionsForWorker(Socket socket) {
        this.socket = socket;
    }

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }
    
    public void printStores() {
        System.out.println(myStores.size());
        System.out.println(myStores.getLast());
        System.out.println(myStores.getFirst());
    }

    public void run() {
        int i = 0;
        while (running) {
            try {
                i++;
                // Read data from Master (through existing socket)
                ObjectInputStream masterIn = new ObjectInputStream(socket.getInputStream());
                Store receivedFromMaster = (Store) masterIn.readObject();
                System.out.println(receivedFromMaster.toString());
                myStores.add(receivedFromMaster);
                
                if (i==1) {
                    Socket reducerSocket = new Socket("127.0.0.2", 5002);
                    ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream());
                    reducerOut.writeObject(receivedFromMaster);
                    reducerOut.flush();
                
                // Clean up
                reducerSocket.close();
                //workerSocket.close();
                }
                socket.close();
                printStores();
                stopThread();

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Actions for Worker failed.");
                e.printStackTrace();
                stopThread();
            }
        }
    }
}
