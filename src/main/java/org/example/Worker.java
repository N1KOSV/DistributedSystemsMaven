package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Worker extends Thread {
        private Socket socket;
        int port = 5000;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        int a;
        int b;

        public Worker(Socket socket) {
            this.socket = socket;
        }

        public Worker(int a, int b) {
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
            for (int a = 0; a < 5; a++) {
                new Client(a, a*2).start();
            }
        }
    }

