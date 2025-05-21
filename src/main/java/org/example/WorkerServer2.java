package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class WorkerServer2 {

    static List<Store> myStores = new ArrayList<>();
    private Map<Integer, List<Store>> nearbyStores = new HashMap();
    int number = 0;
    Double longitude = 0.0;
    Double latitude = 0.0;

    void openServer() {
        try (ServerSocket serverSocket = new ServerSocket(5015)) {
            System.out.println("Worker Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                number++;
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start(); // Handle each client on a separate thread
                //System.out.println("Connection #" + number);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (ObjectInputStream masterIn = new ObjectInputStream(socket.getInputStream())) {
            Object received;

            while ((received = masterIn.readObject()) != null) {
                if (received instanceof Store) {
                    Store store = (Store) received;
                    Boolean alreadyExists = false;
                    for(Store store1 : myStores) {if (store.storeID == store1.storeID) {alreadyExists = true;}}
                    if (!alreadyExists) {myStores.add(store);}
                } else if (received instanceof Map.Entry) {
                    Map.Entry<Integer, String> kvp = (Map.Entry<Integer, String>) received;
                    String message = kvp.getValue();
                    int senderID = kvp.getKey();
                    if (message.startsWith("Lat::")){latitude = Double.parseDouble(message.substring(5));}
                    else if (message.startsWith("Lon::")) {
                        longitude = Double.parseDouble(message.substring(5));
                        if (latitude != 0 && longitude != 0 && !nearbyStores.containsKey(senderID)) {
                            List<Store> clientNearby = new ArrayList<>();
                            for (Store store1 : myStores) {if (store1.isWithin5km(latitude, longitude)) {clientNearby.add(store1);}}
                            nearbyStores.put(senderID, clientNearby);
                        } else if (nearbyStores.containsKey(senderID)) {
                            nearbyStores.get(senderID).clear();
                            for (Store store1 : myStores) {if (store1.isWithin5km(latitude, longitude)) {nearbyStores.get(senderID).add(store1);}}
                        }
                    }
                    if (message.equalsIgnoreCase("send")) {new ActionsForWorker(nearbyStores.get(senderID), kvp).start();}
                    
                    else if (message.equalsIgnoreCase("admin")) {nearbyStores.put(senderID, myStores);}
                    
                    else if (message.startsWith("newProd::")) {
                        String[] parts = message.split("::");
                        for (Store store : nearbyStores.get(senderID)) {if (store.storeID == Integer.parseInt(parts[1])){
                            store.addProduct(parts[2],parts[3],Integer.parseInt(parts[5]),Double.parseDouble(parts[4]));
                            for (Product p : store.products) {System.out.println(p.getName() + " " + p.getPrice());}}}}
                    
                    else if (message.startsWith("changeAvailability::")) {
                        String[] parts = message.split("::");
                        for (Store store : nearbyStores.get(senderID)) {
                            if (store.storeID == Integer.parseInt(parts[1])) {
                                for (Product p : store.products) {
                                    if (p.getName().equals(parts[2])) {p.setAmount(Integer.parseInt(parts[3]));}}
                                for (Product p : store.products) {System.out.println(p.getName() + " " + p.getAmount());}}
                        }
                    } else if (message.startsWith("categories::")) {
                        Map<String, List<String>> result = new HashMap<>();
                        String[] parts = message.split("::");
                        for (int i = 0; i < parts.length - 1; i += 2) {
                            String key = parts[i];
                            String value = parts[i + 1];
                            if (key.equals("ratings")) {value = value.replaceAll("[<\\s]", "");}
                            List<String> values = Arrays.asList(value.split(","));
                            result.put(key, values);}
                        System.out.println("Categories: " + result.get("categories"));
                        for (Store s : nearbyStores.get(senderID)) {
                            if (!result.get("categories").contains(s.foodCategory) || !result.get("prices").contains(s.getAvgPrice()) || Double.parseDouble(Collections.max(result.get("ratings"))) > s.stars ) {
                                nearbyStores.get(senderID).remove(s);
                                System.out.println(s.toString());
                            }
                        }
                        new ActionsForWorker(nearbyStores.get(senderID), kvp).start();
                        System.out.println("Prices: " + result.get("prices"));
                        System.out.println("Ratings: " + result.get("ratings"));
                        
                    } else {
                        System.out.println("Unknown command: " + message);
                    }
                } else {
                    System.out.println("Unknown object received: " + received.getClass());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            //System.out.println("Client disconnected or error occurred: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new WorkerServer2().openServer();
    }
}
