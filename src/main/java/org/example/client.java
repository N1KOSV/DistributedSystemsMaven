package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class  client extends Thread{

    double latitude;
    double longitude;
    int userId;
    static int nrUsers;

    ObjectInputStream in;
    ObjectOutputStream out;
    private boolean Alive = false;
    Socket socket;

    public client(Socket socket){
        this.socket = socket;
    }


    public client(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        nrUsers++;
        userId = nrUsers;
    }

    // public client(Socket connection) {
    // 	try {
    // 		out = new ObjectOutputStream(connection.getOutputStream());
    // 		in = new ObjectInputStream(connection.getInputStream());
    // 	} catch (IOException e) {
    // 		e.printStackTrace();
    // 	}
    // }
    public void run() {
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        int port = 50000;

        try{

            requestSocket = new Socket("127.0.0.1",port);
            System.out.println("Connected with the server in port "+ port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());


            out.writeObject(socket);
            out.flush();
            Socket res = (Socket) in.readObject();
            System.out.println("Server> ");
        }catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                in.close();	out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

    public void searchStores(){
        //Επιστρέφει array με μαγαζιά κοντά στις συντεταγμένες του πελάτη
    }

    public static void main(String[] args) {
        client c = new client(38, 23);
        c.start();
    }
}