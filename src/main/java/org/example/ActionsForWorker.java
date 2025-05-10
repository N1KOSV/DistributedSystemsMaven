package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ActionsForWorker extends Thread {
    static List<Store> myStores;
    static String command = "";
    
    public ActionsForWorker(List<Store> myStores) {
        this.myStores = myStores;
    }
    
    public ActionsForWorker(String command) { this.command = command; }

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }

    public void printStores() {
        if(!myStores.isEmpty()) {
            System.out.println(myStores.size());
            //System.out.println(myStores.getLast());
            //System.out.println(myStores.getFirst());
        }
    }
    

    public void run() {
        int i = 0;
        while (running) {
            try {
                if (command.equals("")) {Socket reducerSocket = new Socket("127.0.0.2", 5002);
                    ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream());
                    reducerOut.writeObject(myStores.getFirst());
                    reducerOut.flush();reducerSocket.close();}
                else{Socket reducerSocket = new Socket("127.0.0.2", 5002);
                    System.out.println("Σπάω με την Visa, ρουφάω από το πενηντάρι");
                    System.out.println(command);
                    ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream());
                    reducerOut.writeObject(myStores.getFirst());
                    reducerOut.flush();reducerSocket.close();}
                i++;

                
                printStores();
                stopThread();

            } catch (IOException e) {
                System.out.println("Actions for Worker failed.");
                e.printStackTrace();
                stopThread();
            }
        }
    }
}
