package org.example;

public class  client {
    
    double latitude;
    double longitude;
    int userId;
    static int nrUsers;
    
    
    public client(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        nrUsers++;
        userId = nrUsers;
    }
    
    
    
    public void searchStores(){
        //Επιστρέφει array με μαγαζιά κοντά στις συντεταγμένες του πελάτη
    }


    
}
