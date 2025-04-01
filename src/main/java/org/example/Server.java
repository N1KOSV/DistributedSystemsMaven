package org.example;

import java.io.IOException;
import java.net.*;

public class Server {
    int port;

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port = 5000;
        new Server(port).openServer();
    }

    void openServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                new Thread(new ActionsForClients(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}