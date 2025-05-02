package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MasterServer {

    int number = 0;

    void openServer() {
        try (ServerSocket serverSocket = new ServerSocket(5012)) {
            System.out.println("Master Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                number++;
                Socket socket = serverSocket.accept();
                String clientAddress = socket.getInetAddress().getHostAddress();
                System.out.println("Connection #" + number + " from " + clientAddress);

                String tag = clientAddress.endsWith("1") ? "1" : "2";

                new Thread(() -> handleClient(socket, tag)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket, String tag) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Object received;

            while ((received = in.readObject()) != null) {
                System.out.println("Received object: " + received.getClass().getSimpleName());

                // Pass the received object and situation to a new thread
                Thread actionThread = new ActionsForMaster(received, tag);
                actionThread.start();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected or error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new MasterServer().openServer();
    }
}
