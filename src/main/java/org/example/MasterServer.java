package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MasterServer {
    private Map<Integer, ClientConnection> clientConnections = new ConcurrentHashMap<>();
    private int clientCounter = 0;

    public static class ClientConnection {
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
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            int clientKey = getNextClientKey();
            clientConnections.put(clientKey, new ClientConnection(socket, out, in));
            System.out.println("New client connected with key: " + clientKey);
            
            handleClient(clientKey);

        } catch (IOException e) {
            System.out.println("Error setting up client connection: " + e.getMessage());
        }
    }

    private synchronized int getNextClientKey() {
        return ++clientCounter;
    }

    private void handleClient(int clientKey) {
        ClientConnection clientConn = clientConnections.get(clientKey);
        if (clientConn == null) return;

        try {
            ObjectInputStream in = clientConn.in;
            ObjectOutputStream out = clientConn.out;
            Socket socket = clientConn.socket;

            Object received;
            while ((received = in.readObject()) != null) {
                if (received instanceof Map.Entry) {
                    int targetKey = (Integer) ((Map.Entry<?, ?>) received).getKey();
                    ClientConnection targetConn = clientConnections.get(targetKey);
                    if (targetConn != null && !targetConn.socket.isClosed()) {
                        ActionsForMaster actionThread = new ActionsForMaster(received, targetConn, targetKey);
                        actionThread.start();
                    } else {
                        System.out.println("No connection found for key " + targetKey);
                    }
                } else if (received instanceof String) {
                    System.out.println("Client " + clientKey + " sent String: " + received);

                    ActionsForMaster actionThread = new ActionsForMaster(received, clientConn, clientKey);
                    actionThread.start();
                } else if (received instanceof Store) {
                    System.out.println("Client " + clientKey + " sent Store object");

                    ActionsForMaster actionThread = new ActionsForMaster(received, clientConn, clientKey);
                    actionThread.start();
                } else {
                    System.out.println("Unknown object received from client " + clientKey);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client " + clientKey + " disconnected or error: " + e.getMessage());
            clientConnections.remove(clientKey);
            try {
                clientConn.socket.close();
            } catch (IOException ignored) {}
        }
    }

    public static void main(String[] args) {
        new MasterServer().openServer();
    }
}
