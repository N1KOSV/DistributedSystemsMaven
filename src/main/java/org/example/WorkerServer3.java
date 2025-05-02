package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WorkerServer3 {

    static List<Store> myStores = new ArrayList<>();
    int number = 0;

    void openServer() {
        try (ServerSocket serverSocket = new ServerSocket(5016)) {
            System.out.println("Worker Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                number++;
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start(); // Handle each client on a separate thread
                System.out.println("Connection #" + number);
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
                    //System.out.println("Received Store: " + store);
                    myStores.add(store);
                    Thread m = new ActionsForWorker(myStores);
                    m.start();
                } else if (received instanceof String) {
                    String message = (String) received;
                    //System.out.println("Received message: " + message);
                    Thread m = new ActionsForWorker(message);
                    m.start();
                    if (message.equalsIgnoreCase("PROCESS")) {
                        // Start processing the list (e.g., launch thread)
                        new ActionsForWorker(myStores).start();
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
            System.out.println("Client disconnected or error occurred: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new WorkerServer3().openServer();
    }
}
