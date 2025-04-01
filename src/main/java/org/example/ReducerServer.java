package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReducerServer {
        int port;

        public ReducerServer(int port) {
            this.port = port;
        }

        public static void main(String[] args) {
            int port = 6000;
            new ReducerServer(port).openServer();
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
