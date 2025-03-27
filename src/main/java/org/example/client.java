package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class  client extends Thread{
    
    double latitude;
    double longitude;
    int userId;
    static int nrUsers;

    ObjectInputStream in;
	ObjectOutputStream out;
    
    
    public client(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        nrUsers++;
        userId = nrUsers;
    }

	public client(Socket connection) {
		try {
			out = new ObjectOutputStream(connection.getOutputStream());
			in = new ObjectInputStream(connection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void run() {
		try {		
			worker t =  (worker)in.readObject();
			t.setAlive(true);
			out.writeObject(t);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
    
    public void searchStores(){
        //Επιστρέφει array με μαγαζιά κοντά στις συντεταγμένες του πελάτη
    }


    
}
