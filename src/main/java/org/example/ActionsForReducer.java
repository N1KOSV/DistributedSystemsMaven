package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ActionsForReducer extends Thread {
   Socket socket;

    public ActionsForReducer(Socket socket) {
       this.socket = socket;
    }

    private volatile boolean running = true;
    static List<Store> myStores = new ArrayList<>();

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
                    if (receivedObject instanceof Map.Entry) {
                        System.out.println("This the case");
                        List<?> tempList = (List<?>) ((Map.Entry<?, ?>) receivedObject).getValue();
                        int Key = (int) ((Map.Entry<?, ?>) receivedObject).getKey();
                        if (!tempList.isEmpty() && tempList.get(0) instanceof Store) {
                            List<Store> storeList = (List<Store>) tempList;
                            myStores.addAll(storeList);
                            System.out.println("Key: " + Key);

                            // Forward the store to the master server
                            out.writeObject(storeList);
                            out.flush();
                            myStores.clear();
                        }
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
