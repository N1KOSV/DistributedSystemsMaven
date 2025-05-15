package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReducerServer {


    static List<Store> myStores = new ArrayList<>();
    int mssgsReceived = 0;

    void openServer() {
        try (ServerSocket serverSocket = new ServerSocket(5002)) {
            System.out.println("Reducer Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                mssgsReceived++;
                System.out.println("Reducer connected " + socket.getInetAddress());

                Thread m = new ActionsForReducer(socket);
                m.start();
                //if (mssgsReceived )
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


        public static void main (String[]args){
            new ReducerServer().openServer();
        }
    }
