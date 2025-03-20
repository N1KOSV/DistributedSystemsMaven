package org.example;

import java.io.*;
import java.sql.SQLOutput;
import java.util.*;

public class master {

    static List<store> myStores = new ArrayList<store>();
    
    
    public void createStore(String path) throws IOException {
        parser parser = new parser(path);
        String[] myData = parser.getStore();
        String[][] myProducts = parser.getProducts();
        store myStore = new store(myData[0],Double.valueOf(myData[1]),Double.valueOf(myData[2]),myData[3],Double.valueOf(myData[4]),Integer.parseInt(myData[5]),myData[6], myStores.size() + 1);
        for (int i=0;i<myProducts.length;i++) {
            String[] myProdData = myProducts[i];
            product myProduct = new product(myProdData[0],myProdData[1],Integer.parseInt(myProdData[2]),Double.valueOf(myProdData[3]));
            myStore.addProduct(myProduct);
        }
        myStores.add(myStore);
    }
    
    public void addProduct(store myStore, product myProduct) throws IOException {
        myStore.addProduct(myProduct);
        int path = myStore.storeID;
        parser parser = new parser("src/main/resources/Store"+path+".json");
        parser.addProductJson(new String[]{myProduct.getName(),myProduct.getType(),String.valueOf(myProduct.getAmount()),String.valueOf(myProduct.getPrice())});
    }
    
    
    
    //Παραλαβή objects product & store
    //Διεπαφή (Console) με τις δυνατότητες του manager
    //Ανοιχτό κανάλι για επικοινωνία από χρήστες
    //Επεξεργασία αιτημάτων χρήστη και προσθήκη φίλτρων
    //Επιστροφή καταστημάτων στον χρήστη βάσει της τοποθεσίας του

    public static void main(String[] args) throws IOException {
        
        master JJJ = new master();
        for (int i=1;i<16;i++) {
           JJJ.createStore("src/main/resources/Store"+i+".json");
        }
        System.out.println(myStores.get(0).isWithin5km(38.01,23.74));

        Scanner scanner = new Scanner(System.in);

        System.out.print("What would you like to do? ");
        System.out.print("1. See all the available stores\n");
        System.out.print("2. Add a new store\n");
        System.out.print("3. Edit a store\n");
        for (int j=0;j<myStores.getFirst().getProducts().size();j++) {
            System.out.println(myStores.getFirst().getProducts().get(j).getName());
        }
        JJJ.addProduct(myStores.getFirst(), new product("Pizza Pizza","pjjasta",12,3.5));
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
        for (int j=0;j<myStores.getFirst().getProducts().size();j++) {
            System.out.println(myStores.getFirst().getProducts().get(j).getName());
        }
        
    }
}
