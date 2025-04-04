package org.example;

import java.io.*;
import java.net.Socket;

public class Reducer extends Thread {
    public static void main(String[] args) {
        Reducer reducer = new Reducer();
        reducer.start();
    }
    public void run() {
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket("127.0.0.1", 5002);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("Hi Master!");
            out.flush();

            String response = (String) in.readObject();
            System.out.println("Reducer received from Worker: " + response);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Reducer failed.");
            e.printStackTrace();
        }
    }
}
