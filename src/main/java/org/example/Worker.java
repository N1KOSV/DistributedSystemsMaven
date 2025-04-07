package org.example;

import java.io.*;
import java.net.Socket;

public class Worker extends Thread {
    public static void main(String[] args) {
        Worker worker = new Worker();
        worker.start();
    }
    public void run() {
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket("127.0.0.1", 5001);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            String response = (String) in.readObject();
            System.out.println("Worker received from Master: " + response);

            out.writeObject("Hi Reducer!");
            out.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Worker failed.");
            e.printStackTrace();
        }
    }
}
