package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;

public class MasterServer {
    void openServer () {
        try (ServerSocket serverSocket = new ServerSocket(5009)) {
            System.out.println("Master Server is listening on port " + serverSocket.getLocalPort());
            int i = 0;
            while (true) {
                i++;
                System.out.println(i);
                Socket socket = serverSocket.accept();
                Thread m = new ActionsForMaster(socket);
                //m.start();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                String msg = (String) in.readObject();
                System.out.println("Master received from Reducer: " + msg);


            }

        } catch (IOException | ClassNotFoundException e) {
        //} catch (IOException /*| ClassNotFoundException*/ e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MasterServer().openServer();
    }
}
