package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

                // Determine client type by IP - this logic might need adjustment depending on your network
                String tag = clientAddress.endsWith("1") ? "1" : "2";
                System.out.println("Client identified as type: " + tag);

                // Start a dedicated thread for this client
                new Thread(() -> handleClient(socket, tag)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket, String tag) {
        try {
            // Important: create output stream first to avoid blocking issues
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Object received;
            System.out.println("Ready to receive objects from client " + tag);

            while ((received = in.readObject()) != null) {
                System.out.println("Received object: " + received.getClass().getSimpleName() +
                        " from client type: " + tag);

                // Pass the received object and situation to a new thread for processing
                ActionsForMaster actionThread = new ActionsForMaster(received, tag, socket);
                actionThread.start();

                // For debugging: print when the action thread is started
                System.out.println("Started processing thread for: " + received.getClass().getSimpleName());

                // If it's a command like "PROCESS", send acknowledgment back to Master
                if (received instanceof String) {
                    // Wait briefly for worker processing to start
                    Thread.sleep(500);
                    // Send acknowledgment
                    out.writeObject((String)received);
                    System.out.println((String)received);
                    out.flush();
                    System.out.println("Sent acknowledgment back to Master");
                }
                else if (received instanceof Store && tag.equals("2")) {
                    Thread.sleep(500);
                    // Send acknowledgment
                    Store myStore = (Store)received;
                    System.out.println(myStore.toString());
                    out.writeObject((Store)received);
                    out.flush();
                    System.out.println("Sent acknowledgment back to Master");
                }
            }

        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            System.out.println("Client disconnected or error: " + e.getMessage());
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