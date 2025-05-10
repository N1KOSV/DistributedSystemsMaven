package org.example;

import java.io.*;
import java.net.Socket;

public class ActionsForMaster extends Thread {

    private final Object receivedObject;
    private final String situation;
    private final Socket socket;

    private volatile boolean running = true;

    public void stopThread() {
        running = false;
    }

    public ActionsForMaster(Object receivedObject, String situation, Socket socket) {
        this.receivedObject = receivedObject;
        this.situation = situation;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (situation.equals("1")) {
            try {
                int number = 5014;
                String ip = "127.0.0.1";
                if (receivedObject instanceof Store) {
                    Store current = (Store) receivedObject;
                    if (current.storeID % 3 == 0) {
                        number = 5014;
                        ip = "127.0.0.1";
                    } else if (current.storeID % 3 == 1) {
                        number = 5015;
                        ip = "127.0.0.1";
                    } else {
                        number = 5016;
                        ip = "127.0.0.1";
                    }
                    System.out.println(number);
                    Socket workerSocket = new Socket(ip, number);
                    ObjectOutputStream out = new ObjectOutputStream(workerSocket.getOutputStream());

                    out.writeObject(receivedObject);
                    out.flush();

                    System.out.println("Sent to Worker: " + receivedObject.getClass().getSimpleName());
                    workerSocket.close();
                    stopThread();
                } else {
                    Socket workerSocket1 = new Socket("127.0.0.1", 5014);
                    Socket workerSocket2 = new Socket("127.0.0.1", 5015);
                    Socket workerSocket3 = new Socket("127.0.0.1", 5016);
                    ObjectOutputStream out1 = new ObjectOutputStream(workerSocket1.getOutputStream());
                    ObjectOutputStream out2 = new ObjectOutputStream(workerSocket2.getOutputStream());
                    ObjectOutputStream out3 = new ObjectOutputStream(workerSocket3.getOutputStream());

                    out1.writeObject(receivedObject);
                    out1.flush();
                    out2.writeObject(receivedObject);
                    out2.flush();
                    out3.writeObject(receivedObject);
                    out3.flush();
                    System.out.println("Sent to WorkerS: " + receivedObject.getClass().getSimpleName());

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

        } else { // situation "2" - handling data from Reducer back to Master
            try {
                // Make sure we have a valid socket and it's open
                if (socket != null && !socket.isClosed()) {
                    // Create new output stream for the existing socket if needed
                    // This is important because we're writing back to the original connection
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.flush();

                    // Write the object back to the client (Master)
                    out.writeObject(receivedObject);
                    out.flush();

                    System.out.println("Forwarded to Master from Reducer: " + receivedObject);

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