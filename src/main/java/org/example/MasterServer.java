package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MasterServer {
    // Map to track client connections by address
    private Map<String, ClientConnection> clientConnections = new ConcurrentHashMap<>();
    int number = 0;

    // Class to hold both socket and its streams
    private static class ClientConnection {
        Socket socket;
        ObjectOutputStream out;

        public ClientConnection(Socket socket, ObjectOutputStream out) {
            this.socket = socket;
            this.out = out;
        }
    }

    void openServer() {
        try (ServerSocket serverSocket = new ServerSocket(5012)) {
            System.out.println("Master Server is listening on port " + serverSocket.getLocalPort());
 
            while (true) {
                number++;
                Socket socket = serverSocket.accept();
                String clientAddress = socket.getInetAddress().getHostAddress();

                String tag = clientAddress.endsWith("1") ? "1" : "2";
                
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                
                if (tag.equals("1")) {
                    clientConnections.put(clientAddress, new ClientConnection(socket, out));
                    System.out.println("Registered Master client: " + clientAddress);
                }
                
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                
                new Thread(() -> handleClient(socket, in, out, tag, clientAddress)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket, ObjectInputStream in, ObjectOutputStream out, String tag, String clientAddress) {
        try {
            Object received;

            while ((received = in.readObject()) != null) {

                if (tag.equals("2")) {
                    if (!clientConnections.isEmpty()) {
                        ClientConnection masterConn = clientConnections.values().iterator().next();
                        if (masterConn != null && !masterConn.socket.isClosed()) {

                            ActionsForMaster actionThread = new ActionsForMaster(received, tag, masterConn.socket, number);
                            actionThread.start();
                            System.out.println("Forwarding to Master client");
                        }
                    }
                } else {
                    ActionsForMaster actionThread = new ActionsForMaster(received, tag, socket, number);
                    actionThread.start();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected or error: " + e.getMessage());


            if (tag.equals("1")) {
                clientConnections.remove(clientAddress);
                System.out.println("Unregistered Master client: " + clientAddress);
            }
        } finally {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new MasterServer().openServer();
    }
}