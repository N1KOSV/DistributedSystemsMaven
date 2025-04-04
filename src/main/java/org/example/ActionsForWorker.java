package org.example;

import java.io.*;
import java.net.Socket;

class ActionsForWorker extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;

    public ActionsForWorker(Socket socket) {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String received = (String) in.readObject();
            System.out.println("AWorker received from Master: " + received);

            out.writeObject("Hi Reducer");
            out.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Actions for Worker failed.");
            e.printStackTrace();
        }
    }
}
