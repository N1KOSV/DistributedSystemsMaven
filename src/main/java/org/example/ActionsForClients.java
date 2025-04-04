//package org.example;
//
//import java.io.*;
//import java.net.Socket;
//
//
//class ActionsForClients implements Runnable {
//    private final Socket socket;
//
//    public ActionsForClients(Socket socket) {
//        this.socket = socket;
//    }
//
//    @Override
//    public void run() {
//        try {
//            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//
//            while (true) {
//                try {
//                    Object received = in.readObject();
//
//                    //System.out.println("Running ActionsForClients for: " + socket.getInetAddress());
//                    System.out.println("Received from Aclient: " + received);
//
//                    out.writeObject("msg received: " + received);
//                    out.flush();
//
//                } catch (EOFException e) {
//                    //System.out.println("Client disconnected. Wating for reconnection");
//                    Thread.sleep(1000);
//                }catch (ClassNotFoundException e) {
//                    System.out.println("Unknown object received.");
//                }catch (IOException e) {
//                    System.out.println("Actions for cleints failed.");
//                    e.printStackTrace();
//                }
//            }
//            //System.out.println("Client disconnected.");
//
//        } catch (IOException | InterruptedException e) {
//            System.out.println("Actions for cleints failed.");
//            e.printStackTrace();
//        }
//    }
//}