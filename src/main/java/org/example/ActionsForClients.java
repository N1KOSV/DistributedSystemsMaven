package org.example;

import java.io.*;
import java.net.Socket;

class ActionsForClients implements Runnable {
    private final Socket socket;

    public ActionsForClients(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                try {
                    Object received = in.readObject();

                    if (received == null) {
                        System.out.println("Client disconnected");
                        break;
                    }
                    //System.out.println("Running ActionsForClients for: " + socket.getInetAddress());
                    System.out.println("Received from Aclient: " + received);

                    out.writeObject("msg received: " + received);
                    out.flush();

                } catch (EOFException e) {
                    //System.out.println("Client disconnected.");
                    continue;
                }catch (ClassNotFoundException e) {
                    System.out.println("Unknown object received.");
                }
            }
            //System.out.println("Client disconnected.");

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }finally {
            try {
                socket.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}