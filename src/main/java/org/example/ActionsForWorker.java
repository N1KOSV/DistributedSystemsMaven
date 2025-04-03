//package org.example;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.Socket;
//
//public class ActionsForWorker implements Runnable {
//    private final Socket socket;
//    public ActionsForWorker(Socket socket) {
//        this.socket = socket;
//    }
//
//    @Override
//    public void run() {
//        try {
//            ObjectOutputStream out = new ObjectOutputStream((socket.getOutputStream()));
//            ObjectInputStream in = new ObjectInputStream((socket.getInputStream()));
//
//            Object received = in.readObject();
//            System.out.println("Client received: " + received);
//
//            out.writeObject("msg received: " + received);
//            out.flush();
//
//        } catch (IOException | ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//}
