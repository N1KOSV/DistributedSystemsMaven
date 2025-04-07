package org.example;

import java.io.*;
import java.net.Socket;

class ActionsForReducer extends Thread {
   Socket socket;

    public ActionsForReducer(Socket socket) {
       this.socket = socket;
    }

    public void run() {
        try{
            //Connection with MasterServer
            Socket MasterSocket = new Socket("127.0.0.1", 5000);
            ObjectOutputStream MasterOut = new ObjectOutputStream(MasterSocket.getOutputStream());
            //ObjectInputStream MasterIn = new ObjectInputStream(MasterSocket.getInputStream());

            //Connection with WorkerServer
            //Socket WorkerSocket = new Socket("127.0.0.1", 5001);
            ObjectOutputStream WorkerOut = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream WorkerIn = new ObjectInputStream(socket.getInputStream());

            String received = (String) WorkerIn.readObject();
            System.out.println("Reducer received from Worker: " + received);

            String data = received + "Processed from Reducer";
            MasterOut.writeObject(data);
            MasterOut.flush();

            MasterSocket.close();
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Actions for Reducer failed.");
            e.printStackTrace();
        }
    }
}
