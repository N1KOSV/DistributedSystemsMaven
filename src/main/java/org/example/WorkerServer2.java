package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkerServer2 {

    static List<Store> myStores = new ArrayList<>();
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
                    myStores.add(store);
                } else if (received instanceof Map.Entry) {
                    Map.Entry<Integer, String> kvp = (Map.Entry<Integer, String>) received;
                    String message = kvp.getValue();
                    if (message.startsWith("Lat: ")){latitude = Double.parseDouble(message.substring(5));}
                    if (message.startsWith("Lon: ")){longitude = Double.parseDouble(message.substring(5));}
                    System.out.println(message);
                    if (message.equalsIgnoreCase("send")) {
                        System.out.println(myStores.size() + " <- HERE");
                        new ActionsForWorker(myStores, kvp).start();
                    } else if (message.equalsIgnoreCase("RESET")) {
                        myStores.clear();
                        System.out.println("Store list has been reset.");
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
