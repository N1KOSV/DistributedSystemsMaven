package org.example;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class ActionsForMaster extends Thread {

    private final Object receivedObject;
    private final MasterServer.ClientConnection clientConn;
    private final int clientKey;

    public ActionsForMaster(Object receivedObject, MasterServer.ClientConnection clientConn, int clientKey) {
        this.receivedObject = receivedObject;
        this.clientConn = clientConn;
        this.clientKey = clientKey;
    }

    @Override
    public void run() {
        try {
            if (receivedObject instanceof Store) {
                Store current = (Store) receivedObject;
                int port;
                String ip = "127.0.0.1";

                if (current.storeID % 3 == 0) {
                    port = 5014;
                } else if (current.storeID % 3 == 1) {
                    port = 5015;
                } else {
                    port = 5016;
                }

                try (Socket workerSocket = new Socket(ip, port);
                     ObjectOutputStream out = new ObjectOutputStream(workerSocket.getOutputStream())) {
                    out.writeObject(receivedObject);
                    out.flush();
                }
            } else if (receivedObject instanceof String) {
                String receivedOrder = (String) receivedObject;
                System.out.println(clientKey + " Has ordered " + receivedOrder);

                Map.Entry<Integer, String> kvp = new AbstractMap.SimpleEntry<>(clientKey, receivedOrder);

                String ip = "127.0.0.1";
                int[] ports = {5014, 5015, 5016};

                for (int port : ports) {
                    try (Socket ws = new Socket(ip, port);
                         ObjectOutputStream oos = new ObjectOutputStream(ws.getOutputStream())) {
                        oos.writeObject(kvp);
                        oos.flush();
                    }
                }
            } else {
                // Εδώ λαμβάνουμε αποτελέσματα που πρέπει να στείλουμε στον client (Android/Manager)
                synchronized (clientConn.out) { // για ασφάλεια νημάτων
                    clientConn.out.writeObject(receivedObject);
                    clientConn.out.flush();
                }

                if (receivedObject instanceof Map.Entry) {
                    Map.Entry<Integer, List<Store>> kvp = (Map.Entry<Integer, List<Store>>) receivedObject;
                    System.out.println("Sent to client " + clientKey + ": Order ID " + kvp.getKey() +
                            ", Stores Count: " + kvp.getValue().size());
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to handle action for client " + clientKey);
            e.printStackTrace();
        }
    }
}
