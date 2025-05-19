package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkerServer3 {

    static List<Store> myStores = new ArrayList<>();
    int number = 0;
    Double longitude = 0.0;
    Double latitude = 0.0;

    void openServer() {
        try (ServerSocket serverSocket = new ServerSocket(5016)) {
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
                    if (message.startsWith("Lat: ")){latitude = Double.parseDouble(message.substring(5));}
                    if (message.startsWith("Lon: ")){longitude = Double.parseDouble(message.substring(5));}
                    //Thread m = new ActionsForWorker(myStores, message);
                    //m.start();
                    System.out.println(message);
                    if (message.equalsIgnoreCase("send")) {
                        System.out.println("Shout");
                        new ActionsForWorker(myStores, kvp).start();
                    } else if (message.startsWith("newProd::")) {
                        String[] parts = message.split("::");
                        for (Store store : myStores) {if (store.storeID == Integer.parseInt(parts[1])){
                            store.addProduct(parts[2],parts[3],Integer.parseInt(parts[5]),Double.parseDouble(parts[4]));
                            for (Product p : store.products) {System.out.println(p.getName() + " " + p.getPrice());}}}}
                        else if (message.startsWith("changeAvailability::")) {
                        String[] parts = message.split("::");
                        for (Store store : myStores) {if (store.storeID == Integer.parseInt(parts[1])) {
                            for (Product p : store.products) { if (p.getName().equals(parts[2])){ p.setAmount(Integer.parseInt(parts[3]));} }
                            for (Product p : store.products) {System.out.println(p.getName() + " " + p.getAmount());}
                        }}
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
        new WorkerServer3().openServer();
    }
}
