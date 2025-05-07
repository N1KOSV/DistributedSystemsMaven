package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Worker extends Thread {
    Socket socket;
    public Worker() {}
    public Worker(Socket socket) {
        this.socket = socket;
    }

    void openServer () {
        try (ServerSocket serverSocket = new ServerSocket(5001)) {
            System.out.println("Master Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Master connected" + socket.getInetAddress());

                Thread worker = new ActionsForWorker(socket);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            ObjectInputStream MasterIn = new ObjectInputStream(socket.getInputStream());
            //ObjectOutputStream MasterOut = new ObjectOutputStream(socket.getOutputStream());

            Socket ReducerSocket = new Socket("127.0.0.1", 5002);
            ObjectOutputStream ReducerOut = new ObjectOutputStream(ReducerSocket.getOutputStream());
            //ObjectInputStream ReducerIn = new ObjectInputStream(ReducerSocket.getInputStream());

            String response = (String) MasterIn.readObject();
            System.out.println("Worker received from Master: " + response);

            ReducerOut.writeObject("Hi Reducer!");
            ReducerOut.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Worker failed.");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        Worker WorkerServer = new Worker();
        new Thread(WorkerServer::openServer).start();
        Thread.sleep(1000);

        Worker worker = new Worker();
        worker.start();
    }

}
