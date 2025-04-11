/*
package org.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread {
    private Socket socket;
    int port = 5000;
    private int a;
    private int b;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public client(Socket socket) {
        this.socket = socket;
    }

    public Client(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public void run() {
        try {
            socket = new Socket("localhost", port);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            int sum = a + b;
            out.writeObject(sum);
            out.flush();
            System.out.println("Sent to the server: " + sum);

            Object response = in.readObject();
            System.out.println("Received from the server: " + response);

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        for (int a = 0; a < 50; a++) {
//            new client(a, a*2).start();
//        }
        for (int a = 0; a < 300; a++) {
            Thread.sleep(1);
            new Client(a, a*2).start();
        }

    }
}
*/