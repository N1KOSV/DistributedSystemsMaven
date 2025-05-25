package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
            
                if (tag.equals("1")) {
                    number++;
                    clientConnections.put(number, new ClientConnection(socket, out, in));
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
                if (received instanceof Map.Entry) {
                    if (!clientConnections.isEmpty()) {
                        ClientConnection masterConn = clientConnections.get((Integer) ((Map.Entry<Integer, ?>) received).getKey());
                        if (masterConn != null && !masterConn.socket.isClosed()) {
                            ActionsForMaster actionThread = new ActionsForMaster(received, masterConn.socket, number);
                            actionThread.start();
                        }
                    }
                } else {
                    if (received instanceof String) {System.out.println((String) received);}
                    for (Map.Entry<Integer, ClientConnection> entry : clientConnections.entrySet()) {
                        if (entry.getValue().socket.equals(socket)){
                    ActionsForMaster actionThread = new ActionsForMaster(received, socket, entry.getKey());
                    actionThread.start();
                    }}
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected or error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new MasterServer().openServer();
    }
}