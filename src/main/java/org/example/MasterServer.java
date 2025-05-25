package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MasterServer {
    // Map to track client connections by address
    private Map<Integer, ClientConnection> clientConnections = new ConcurrentHashMap<>();
    int number = 0;

    // Class to hold both socket and its streams
    private static class ClientConnection {
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;

        public ClientConnection(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
            this.socket = socket;
            this.out = out;
            this.in = in;
        }
    }

    void openServer() {
        try (ServerSocket serverSocket = new ServerSocket(5012)) {
            System.out.println("Master Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleNewClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleNewClient(Socket socket) {
        String clientAddress = socket.getInetAddress().getHostAddress();
        String tag = clientAddress.endsWith("1") || clientAddress.endsWith("150")? "1" : "2";

        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Synchronize access to the number and clientConnections map
                if (tag.equals("1")) {
                    number++;
                    clientConnections.put(number, new ClientConnection(socket, out, in));
                    System.out.println("Registered Master client: " + clientAddress + " with ID: " + number);
                }
            // Handle this client's communication
            new Thread(() -> handleClient(socket, in, out, tag, clientAddress)).start();

        } catch (IOException e) {
            System.out.println("Error setting up client connection: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket, ObjectInputStream in, ObjectOutputStream out, String tag, String clientAddress) {
        try {
            Object received;

            while ((received = in.readObject()) != null) {

                if (tag.equals("2")) {
                    if (!clientConnections.isEmpty()) {
                        //System.out.println((Integer) ((Map.Entry<Integer, ?>) received).getKey());
                        ClientConnection masterConn = clientConnections.get((Integer) ((Map.Entry<Integer, ?>) received).getKey());
                        //System.out.println(masterConn.socket.getInetAddress().getHostAddress() + ":" + masterConn.socket.getPort());
                        if (masterConn != null && !masterConn.socket.isClosed()) {
                            ActionsForMaster actionThread = new ActionsForMaster(received, tag, masterConn.socket, number);
                            actionThread.start();
                        }
                    }
                } else {
                    if (received instanceof String) {System.out.println((String) received);}
                    
                    for (Map.Entry<Integer, ClientConnection> entry : clientConnections.entrySet()) { if (entry.getValue().socket.equals(socket)){
                    //    System.out.println(entry.getValue() + " Τώρα μου μιλάει σαν γνωστή-η-η");
                    
                    ActionsForMaster actionThread = new ActionsForMaster(received, tag, socket, entry.getKey());
                    actionThread.start();
                    } }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected or error: " + e.getMessage());
            
        } finally {
            try {
                //if (!socket.isClosed()) {
                    //socket.close();
                //    System.out.println("PTSD-ι-ι, στο σπίτι μου δεν θέλει να 'ρθεί-ι-ι");
                //}
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    public static void main(String[] args) {
        new MasterServer().openServer();
    }
}