package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class ReducerServer{
    static List<Store> myStores = new ArrayList<>();
    static int count = 0;

    void openServer () {
        try (ServerSocket serverSocket = new ServerSocket(5002)) {
            System.out.println("Reducer Server is listening on port " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Reducer connected " + socket.getInetAddress());

                //Thread m = new ActionsForReducer(socket);
                //m.start();

//------
                    ObjectInputStream workerIn = new ObjectInputStream(socket.getInputStream());
                    Socket masterSocket = new Socket("127.0.0.2", 5012);
                    System.out.println("Reducer: Connection received");
                    ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());

                    Object receivedObject;
                    while ((receivedObject = workerIn.readObject()) != null) {
                        if (receivedObject instanceof Map.Entry) {
                            count++;
                            System.out.println("This the case");
                            List<?> tempList = (List<?>) ((Map.Entry<?, ?>) receivedObject).getValue();
                            int Key = (int) ((Map.Entry<?, ?>) receivedObject).getKey();
                            if (!tempList.isEmpty() && tempList.get(0) instanceof Store) {
                                List<Store> storeList = (List<Store>) tempList;
                                myStores.addAll(storeList);
                                System.out.println("Key: " + Key);

                                // Forward the store to the master server
                                if (count==3) {
                                    out.writeObject(myStores);
                                    out.flush();
                                    myStores.clear();
                                }
                            }
                        } else if (receivedObject instanceof String) {
                            System.out.println("Received message: " + receivedObject);
                        }
                    }
                    masterSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String [] args){
        new ReducerServer().openServer();
    }
}
