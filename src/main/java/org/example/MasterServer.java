package org.example;

import java.io.*;
import java.net.*;

public class MasterServer {
    void openServer () {
        try (ServerSocket serverSocket = new ServerSocket(5012)) {
            System.out.println("Master Server is listening on port " + serverSocket.getLocalPort());
            int i = 0;
            while (true) {
                Socket socket = serverSocket.accept();
                String clientAddress = socket.getInetAddress().getHostAddress();
                System.out.println(clientAddress);
                if (clientAddress.endsWith("1")) {
                    if (i==0) {
                        System.out.println("First Case");
                        Thread m = new ActionsForMaster(socket, "1");
                        m.start();
                        i++;
                    }
                }
                else{
                    System.out.println(socket);
                    System.out.println("Second Case");
                    Thread m = new ActionsForMaster(socket, "2");
                    m.start();
                }
                //ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                //String msg = (String) in.readObject();
                //System.out.println("Master received from Reducer: " + msg);


            }

        //} catch (IOException | ClassNotFoundException e) {
        } catch (IOException /*| ClassNotFoundException*/ e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MasterServer().openServer();
    }
}
