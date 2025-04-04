package org.example;

import java.io.*;
import java.net.Socket;

public class ActionsForMaster extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;

    public ActionsForMaster(Socket socket) {
        try{
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String msg = (String) in.readObject();
            System.out.println("AMaster received from Reducer: " + msg);

            out.writeObject("Hi Worker");
            out.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Actions for master failed");
            e.printStackTrace();
        }finally {
            try {
                in.close();
                out.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }
}
