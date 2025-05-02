package org.example;

import java.io.*;
import java.net.Socket;

public class ActionsForMaster extends Thread {

    private final Object receivedObject;
    private final String situation;

    public ActionsForMaster(Object receivedObject, String situation) {
        this.receivedObject = receivedObject;
        this.situation = situation;
    }

    @Override
    public void run() {
        if (situation.equals("1")) {
            try {
                int number = 5014;
                if (receivedObject instanceof Store){
                Store current = (Store) receivedObject;
                if (current.storeID % 3 == 0){number = 5014;}else if(current.storeID % 3 == 1){number = 5015;}else{number = 5016;}}
                System.out.println(number);
                Socket workerSocket = new Socket("127.0.0.1", number);
                ObjectOutputStream out = new ObjectOutputStream(workerSocket.getOutputStream());

                out.writeObject(receivedObject);
                out.flush();

                System.out.println("Sent to Worker: " + receivedObject.getClass().getSimpleName());

                workerSocket.close();
            } catch (IOException e) {
                System.out.println("Failed to send to worker");
                e.printStackTrace();
            }

        } else { // situation "2"
                try {
                    Socket masterSocket = new Socket("127.0.0.1", 5013);
                    ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());

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
