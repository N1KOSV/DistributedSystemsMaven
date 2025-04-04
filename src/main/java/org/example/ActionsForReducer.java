package org.example;

import java.io.*;
import java.net.Socket;

class ActionsForReducer extends Thread {
    ObjectOutputStream out;
    ObjectInputStream in;

    public ActionsForReducer(Socket socket) {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            Object received = in.readObject();
            System.out.println("AReducer received from Worker : " + received);

            out.writeObject("Hi master!!");
            out.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Actions for Reducer failed.");
            e.printStackTrace();
        }
    }
}
