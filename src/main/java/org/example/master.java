package org.example;

import java.io.*;
import java.util.*;

public class master {
    public master() {}
    private Server server;

    public master(int port) {
        server = new Server(port);
    }

    public void start() {
        server.openServer();
    }

    static List<store> myStores = new ArrayList<store>();

    public static void read(String path) throws IOException {
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.startsWith("Store") && name.endsWith(".json"));
            if (files != null) {
                // Ταξινόμηση των αρχείων με βάση τον αριθμό στο όνομα (π.χ. Store1, Store2, ...)
                Arrays.sort(files, Comparator.comparingInt(file ->
                        Integer.parseInt(file.getName().replaceAll("[^0-9]", "")))
                );
                int id = 0;
                for (File file : files) {
                    parser parser = new parser(file.getPath());
                    String[] myData = parser.getStore();
                    String[][] myProducts = parser.getProducts();

                    store myStore = new store(
                            myData[0], Double.parseDouble(myData[1]), Double.parseDouble(myData[2]),
                            myData[3], Double.parseDouble(myData[4]), Integer.parseInt(myData[5]),
                            myData[6], id + 1
                    );
                    id++;
                    for (String[] myProdData : myProducts) {
                        product myProduct = new product(
                                myProdData[0], myProdData[1], Integer.parseInt(myProdData[2]),
                                Double.parseDouble(myProdData[3])
                        );
                        myStore.addProduct(myProduct);
                    }
                    myStores.add(myStore);
                }
            }
        }
    }

    public void editStore() {
        Scanner scanner = new Scanner(System.in);
        int i = 1;
        myStores.sort(Comparator.comparingInt(store::getStoreID));
        for (store store : myStores) {System.out.println(i + ". " + store.name);i++;}
        System.out.println("Which store would you like to edit?");
        int answer = Integer.parseInt(scanner.nextLine());
        //parser parser = new parser("src/main/resources/Store"+answer+".json");
        System.out.println("What do you want to edit?");System.out.println("1. Add a new product");System.out.println("2. Edit a product quantity");
        int answer2 = Integer.parseInt(scanner.nextLine());
        if (answer2 == 1) {
            System.out.println("Enter the product name");String productName = scanner.nextLine();System.out.println("Enter the product type");String productType = scanner.nextLine();System.out.println("Enter the product price");String productPrice = scanner.nextLine();System.out.println("Enter the product amount");String productAmount = scanner.nextLine();
            myStores.get(answer - 1).addProduct(productName,productType,Integer.parseInt(productAmount),Double.parseDouble(productPrice));}
        //parser.addProductJson(new String[] {productName, productType, productPrice, productAmount});}
        else if (answer2 == 2) {
            for (product product : myStores.get(answer - 1).getProducts()) System.out.println(product.getName());
            answer2 = Integer.parseInt(scanner.nextLine());
            System.out.println("How much of this item is in stock?");
            int answer3 = Integer.parseInt(scanner.nextLine());
            myStores.get(answer - 1).products.get(answer2-1).setAmount(answer3);}
        else if (answer2 == 3) {
            i = 0;
            for (product product : myStores.get(answer - 1).getProducts()) {
                i++;
                System.out.println(i + ". " + product.getName());
            }
            answer2 = Integer.parseInt(scanner.nextLine());
            myStores.get(answer - 1).products.get(answer2 - 1).setAmount(-1);
        }
        //parser.changeAvailableAmount("src/main/resources/Store" + answer + ".json", myStores.get(answer - 1).products.get(answer2 - 1).getName(), answer3); }
    }
    
    public void sell() {
        Scanner scanner = new Scanner(System.in);
        int i = 1;
        myStores.sort(Comparator.comparingInt(store::getStoreID));
        for (store store : myStores) {
            System.out.println(i + ". " + store.name);
            i++;
        }
        System.out.println("Which store would you like to edit?");
        int answer = Integer.parseInt(scanner.nextLine());
        i = 0;
        for (product product : myStores.get(answer - 1).getProducts()) {
            i++;
            System.out.println(i + ". " + product.getName());
        }
        int answer2 = Integer.parseInt(scanner.nextLine());
        myStores.get(answer - 1).sell(answer2 - 1);
        i = 0;
        for (store store : myStores) {
            System.out.println(i + ". " + myStores.get(i).toString());
            i++;
        }
    }

    public void newStore(Scanner scanner) throws IOException {
        System.out.println("Enter the name of the store");
        String storeName = scanner.nextLine();
        System.out.println("Enter the type of store");
        String storeType = scanner.nextLine();
        System.out.println("Enter the latitude");
        String storeLat = scanner.nextLine();
        System.out.println("Enter the longitude");
        String storeLon = scanner.nextLine();
        System.out.println("Enter the stars");
        String storeStars = scanner.nextLine();
        System.out.println("Enter the ratings");
        String storeRatings = scanner.nextLine();
        System.out.println("Enter the logo");
        String storeLogo = scanner.nextLine();
        boolean moreProducts = true;
        int nrProducts = 0;
        store myStore = new store(storeName,Double.valueOf(storeLat),Double.valueOf(storeLon),storeType,Double.valueOf(storeStars),Integer.parseInt(storeRatings),storeLogo,myStores.size());
        myStores.add(myStore);
        while (moreProducts) {
            nrProducts++;
            System.out.println("Enter the product name");
            String productName = scanner.nextLine();
            System.out.println("Enter the product type");
            String productType = scanner.nextLine();
            System.out.println("Enter the product price");
            String productPrice = scanner.nextLine();
            System.out.println("Enter the product amount");
            String productAmount = scanner.nextLine();
            System.out.println("Is there another product? \n Y: Yes\n N: No");
            if (nrProducts == 1){
                myStore.addProduct(productName,productType,Integer.parseInt(productAmount),Double.parseDouble(productPrice));
                //parser parser = new parser("src/main/resources/Store"+ (myStores.size() + 1 )+".json");
                //parser.createJsonFile("src/main/resources/Store"+ (myStores.size() + 1 )+".json",storeName,Double.valueOf(storeLat),Double.valueOf(storeLon),storeType,Double.valueOf(storeStars),Integer.parseInt(storeRatings),storeLogo,new String[][] {{productName, productType, productAmount, productPrice}});
            }
            else{
                myStore.addProduct(productName,productType,Integer.parseInt(productAmount),Double.parseDouble(productPrice));
                //parser.addProductJson(new String[] {productName, productType, productAmount, productPrice});
            }
            if (scanner.nextLine().equals("N")) {moreProducts = false;}
        }
        int i =0;
        
        //read("src/main/resources");
        for (store store : myStores) {
            i++;
            System.out.println(i + ". " + store.toString() + " - " + myStores.get(i-1).products.size());
        }
    }

    //Παραλαβή objects product & store
    //Διεπαφή (Console) με τις δυνατότητες του manager
    //Ανοιχτό κανάλι για επικοινωνία από χρήστες
    //Επεξεργασία αιτημάτων χρήστη και προσθήκη φίλτρων
    //Επιστροφή καταστημάτων στον χρήστη βάσει της τοποθεσίας του
    
    // Όχι τόσες αλλαγές στο json
    // Να φτιάξω αντικείμενα για κάθε μαγαζί και να τα περνάω
    // int για τις πωλήσεις του κάθε προϊόντος
    // Κάποιον τρόπο να προκύπτει το StoreID και το ProductID μέσω Hashing για να μπορώ να κρατάω το ποιό προϊόν κοιτάω
    // Κάποια λίστα στο Client.java ώστε να μπορώ να κρατάω κάθε πώληση ως kvp
    // ΝΔ πώς θα χρησιμοποιήσω mapReduce και για τί KVPs
    
    public void seeAllAvailableStores() {
        int i = 0;for (store store : myStores){
            i++;
        System.out.println(i + ". " + myStores.get(i-1).toString());
        }
    }

    public static Set<String> getStoreCategories() {
        Set<String> categories = new HashSet<>();
        for (store store : myStores) {
            categories.add(store.foodCategory);
    public static Set<String> getStoreCategories() {
        Set<String> categories = new HashSet<>();
        for (store store : myStores) {
            categories.add(store.foodCategory);
        }
        return categories;
    }

    public void seeAvailableStores(double lon, double lat) {
        double x = lon - lat;
        int i = 0;for (store store : myStores){
            i++;
            System.out.println(i + ". " + myStores.get(i-1).toString());
        }
        return categories;
    }

    public void seeAvailableStores(double lon, double lat) {
        double x = lon - lat;
        int i = 0;for (store store : myStores){
            i++;
            System.out.println(i + ". " + myStores.get(i-1).toString());
        }
    }

    public static void main(String[] args) throws IOException {
        master masterServer = new master(5000);
        masterServer.start();

        master master = new master();

        //master.read("src/main/resources");


    }
}