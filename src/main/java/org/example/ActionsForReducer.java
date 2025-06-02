package org.example;

import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class ActionsForReducer extends Thread {
    Socket socket;
    
    private static final Map<String, List<Store>> aggregatedData = new ConcurrentHashMap<>();
    private static final Map<String, Integer> messageCountPerKey = new ConcurrentHashMap<>();
    private static final int EXPECTED_MESSAGES_PER_KEY = 3;
    
    private static final String MASTER_HOST = "127.0.0.2";
    private static final int MASTER_PORT = 5012;

    public ActionsForReducer(Socket socket) {
        this.socket = socket;
    }

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }

    public void run() {
        try (ObjectInputStream workerIn = new ObjectInputStream(socket.getInputStream())) {
            System.out.println("Reducer: Connection received");

            while (running) {
                try {
                    Object receivedObject = workerIn.readObject();
                    if (receivedObject instanceof Map.Entry) {
                        System.out.println("Map entry received");
                        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) receivedObject;
                        String key = (String) entry.getKey();
                        List<Store> tempList = (List<Store>) entry.getValue();
                        for (Store store : tempList) {
                            System.out.println(store.toString());
                        }

                        if (tempList != null && !tempList.isEmpty()) {
                            synchronized (aggregatedData) {
                                aggregatedData.putIfAbsent(key, new ArrayList<>());
                                aggregatedData.get(key).addAll(tempList);

                                int currentCount = messageCountPerKey.getOrDefault(key, 0) + 1;
                                messageCountPerKey.put(key, currentCount);

                                System.out.println("Key: " + key + ", Message count: " + currentCount);

                                if (currentCount >= EXPECTED_MESSAGES_PER_KEY) {
                                    forwardToMaster(key, aggregatedData.get(key));
                                    aggregatedData.remove(key);
                                    messageCountPerKey.remove(key);
                                }
                            }
                        }
                    }
                } catch (EOFException eof) {
                    System.out.println("Reducer: Client closed connection (EOF)");
                    break;
                } catch (ClassNotFoundException | IOException e) {
                    System.out.println("Reducer: Error reading object");
                    e.printStackTrace();
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Reducer: Error setting up input stream");
            e.printStackTrace();
        } finally {
            stopThread();
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("Reducer: Failed to close client socket.");
            }
        }
    }



    private void forwardToMaster(String key, List<Store> storeList) {
        try (Socket masterSocket = new Socket(MASTER_HOST, MASTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream())) {
            System.out.println("Open up the safe");
            System.out.println("Forwarding combined data for key: " + key +
                    " with " + storeList.size() + " items");

            // Create a new Map.Entry to send to master
            Map.Entry<String, List<Store>> combinedEntry = new AbstractMap.SimpleEntry<>(key, storeList);
            out.writeObject(combinedEntry);
            out.flush();

        } catch (IOException e) {
            System.out.println("Failed to forward data to master server.");
            e.printStackTrace();
        }
    }
}