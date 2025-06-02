package org.example;

import java.io.*;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ActionsForWorker extends Thread {
    static List<Store> myStores;
    static String command = "";
    Map.Entry<String, String> kvp;
    
    public ActionsForWorker(List<Store> myStores) {this.myStores = myStores;}
    
    public ActionsForWorker(List<Store> myStores,Map.Entry kvp) {this.myStores = myStores ;this.kvp = kvp; }

    public ActionsForWorker(List<Store> myStores,String command) { this.command = command; this.myStores = myStores;}


    public ActionsForWorker(List<Store> myStores,String command,int longitude, int latitude) { this.command = command; this.myStores = myStores;}

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }

    public void printStores() {
        if(!myStores.isEmpty()) {
            System.out.println(myStores.size());
        }
    }
    

    public void run() {
        int i = 0;
        while (running) {
            try {
                System.out.println(kvp.getKey() + " " + kvp.getValue());
                if (kvp.getValue().equals("send")){Socket reducerSocket = new Socket("127.0.0.2", 5002);
                    ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream());
                    Map.Entry<String, List<Store> > response = new AbstractMap.SimpleEntry<>(kvp.getKey(), myStores);
                    if (myStores != null) {printStores();}
                    reducerOut.writeObject(response);
                    reducerOut.flush();
                    reducerSocket.close();}
                else{Socket reducerSocket = new Socket("127.0.0.2", 5002);
                    System.out.println(command);
                    ObjectOutputStream reducerOut = new ObjectOutputStream(reducerSocket.getOutputStream());
                    reducerOut.flush();reducerSocket.close();}
                i++;
                stopThread();
            } catch (IOException e) {
                System.out.println("Actions for Worker failed.");
                e.printStackTrace();
                stopThread();
            }
        }
    }
}
