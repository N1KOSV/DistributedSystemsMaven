package org.example;

import java.io.*;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.Map;

public class ActionsForMaster extends Thread {

    private final Object receivedObject;
    private final String situation;
    private final Socket socket;
    private final int number;

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }

    public ActionsForMaster(Object receivedObject, String situation, Socket socket, int number) {
        this.receivedObject = receivedObject;
        this.situation = situation;
        this.socket = socket;
        this.number = number;
    }


    @Override
    public void run() {
        if (situation.equals("1")) {
            try {
                int port = 5014;
                String ip = "127.0.0.1";
                if (receivedObject instanceof Store) {
                    Store current = (Store) receivedObject;
                    if (current.storeID % 3 == 0) {
                        port = 5014;
                        ip = "127.0.0.1";
                    } else if (current.storeID % 3 == 1) {
                        port = 5015;
                        ip = "127.0.0.1";
                    } else {
                        port = 5016;
                        ip = "127.0.0.1";
                    }
                    System.out.println(port);
                    Socket workerSocket = new Socket(ip, port);
                    ObjectOutputStream out = new ObjectOutputStream(workerSocket.getOutputStream());

                    out.writeObject(receivedObject);
                    out.flush();
                    
                    workerSocket.close();
                    stopThread();
                } else {
                    Socket workerSocket1 = new Socket("127.0.0.1", 5014);
                    Socket workerSocket2 = new Socket("127.0.0.1", 5015);
                    Socket workerSocket3 = new Socket("127.0.0.1", 5016);
                    ObjectOutputStream out1 = new ObjectOutputStream(workerSocket1.getOutputStream());
                    ObjectOutputStream out2 = new ObjectOutputStream(workerSocket2.getOutputStream());
                    ObjectOutputStream out3 = new ObjectOutputStream(workerSocket3.getOutputStream());
                    Map.Entry<Integer, String> kvp = new AbstractMap.SimpleEntry<>(number, (String) receivedObject);
                    out1.writeObject(kvp);
                    out1.flush();
                    out2.writeObject(kvp);
                    out2.flush();
                    out3.writeObject(kvp);
                    out3.flush();
                    System.out.println("Sent to WorkerS: " + kvp.getClass().getSimpleName());

                    // Close all worker sockets
                    workerSocket1.close();
                    workerSocket2.close();
                    workerSocket3.close();
                    stopThread();
                }
            } catch (IOException e) {
                System.out.println("Failed to send to worker");
                e.printStackTrace();
            }

        } else {
            try {
                // Make sure we have a valid socket and it's open
                if (socket != null && !socket.isClosed()) {
                    // Use a special technique to avoid stream corruption, We'll need to create a temporary ByteArrayOutputStream to hold the serialized object
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream tempOut = new ObjectOutputStream(baos);
                    tempOut.writeObject(receivedObject);
                    tempOut.flush();

                    // Now write the length and bytes to the socket output stream
                    DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream());
                    byte[] serializedObject = baos.toByteArray();
                    socketOut.writeInt(serializedObject.length);
                    socketOut.write(serializedObject);
                    socketOut.flush();

                    // Don't close the socket here - let the MasterServer handle that
                } else {
                    System.out.println("Error: Socket is null or closed, cannot forward to Master");
                }
                stopThread();
            } catch (IOException e) {
                System.out.println("Failed to forward to Master");
                e.printStackTrace();
            }
        }
    }
}