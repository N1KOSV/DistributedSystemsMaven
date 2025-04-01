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
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.flush();
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Object received = in.readObject();
            System.out.println("Client received: " + received);

            out.writeObject("msg received: " + received);
            out.flush();

        } catch (ClassNotFoundException | IOException e) {
            System.out.println("connection with client lost");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }}