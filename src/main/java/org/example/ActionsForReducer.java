package org.example;

import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class ActionsForReducer extends Thread {
    Socket socket;

    // Static maps shared across all ActionsForReducer instances
    private static final Map<Integer, List<Store>> aggregatedData = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> messageCountPerKey = new ConcurrentHashMap<>();
    private static final int EXPECTED_MESSAGES_PER_KEY = 3;

    // Master server details
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
        try {
            ObjectInputStream workerIn = new ObjectInputStream(socket.getInputStream());
            System.out.println("Reducer: Connection received");
            Object receivedObject;
            while (running && (receivedObject = workerIn.readObject()) != null) {
                if (receivedObject instanceof Map.Entry) {
                    System.out.println("Map entry received");
                    // Extract key and value from the Map.Entry
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) receivedObject;
                    int key = (int) entry.getKey();
                    List<Store> tempList = (List<Store>) entry.getValue();

                    if (!tempList.isEmpty()) {
                        // Cast to the correct type

                        // Aggregate the data with the same key
                        synchronized (aggregatedData) {
                            if (!aggregatedData.containsKey(key)) {
                                aggregatedData.put(key, new ArrayList<>());
                            }
                            aggregatedData.get(key).addAll(tempList);

                            // Update message count for this key
                            messageCountPerKey.put(key,
                                    messageCountPerKey.getOrDefault(key, 0) + 1);

                            System.out.println("Key: " + key + ", Message count: " +
                                    messageCountPerKey.get(key));

                            // When we've received all expected messages for this key, forward to master
                            if (messageCountPerKey.get(key) >= EXPECTED_MESSAGES_PER_KEY) {
                                forwardToMaster(key, aggregatedData.get(key));
                                // Clear data for this key after sending
                                aggregatedData.remove(key);
                                messageCountPerKey.remove(key);
                            }
                        }
                    }
                }
            }
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Actions for Reducer failed.");
            e.printStackTrace();
        } finally {
            stopThread();
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("Failed to close client socket.");
            }
        }
    }

    private void forwardToMaster(int key, List<Store> storeList) {
        try (Socket masterSocket = new Socket(MASTER_HOST, MASTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream())) {
            System.out.println("Open up the safe");
            System.out.println("Forwarding combined data for key: " + key +
                    " with " + storeList.size() + " items");

            // Create a new Map.Entry to send to master
            Map.Entry<Integer, List<Store>> combinedEntry = new AbstractMap.SimpleEntry<>(key, storeList);
            out.writeObject(combinedEntry);
            out.flush();

        } catch (IOException e) {
            System.out.println("Failed to forward data to master server.");
            e.printStackTrace();
        }
    }
}