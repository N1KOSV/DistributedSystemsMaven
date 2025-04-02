package org.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class client extends Thread {
    private Socket socket;
    private int a;
    private int b;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public client(Socket socket) {
        this.socket = socket;
    }

    public client(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public void run() {
        try {
            int port = 5000;
            socket = new Socket("localhost", port);
            //System.out.println("Connected with the server on port " + port);

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
                if (out != null) {
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        for (int a = 0; a < 500; a++) {
            new client(a, a*2).start();
        }
    }
}
