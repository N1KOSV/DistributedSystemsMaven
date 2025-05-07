package org.example;

import java.io.*;
import java.net.Socket;

public class ActionsForMaster extends Thread {

    private final Object receivedObject;
    private final String situation;
    private final Socket socket;

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
                workerSocket.close();}
                else{
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
                }
            } catch (IOException e) {
                System.out.println("Failed to send to worker");
                e.printStackTrace();
            }

        } else { // situation "2"
                try {
                    //Socket masterSocket = new Socket("127.0.0.1", 5013);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.flush();
                    out.writeObject(receivedObject);
                    out.flush();
                    System.out.println("Forwarded to Master from Reducer: " + receivedObject);

                    //masterSocket.close();
                } catch (IOException e) {
                    System.out.println("Failed to forward to Master");
                    e.printStackTrace();
                }
        }
    }
}
