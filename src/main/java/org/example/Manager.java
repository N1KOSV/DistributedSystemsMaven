package org.example;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Manager extends Thread {
    double latitude;
    double longitude;
    int userId;
    static int nrUsers = 0;
    boolean isAdmin;

    public Manager(double longitude, double latitude, boolean isAdmin) {
        this.longitude = longitude;
        this.latitude = latitude;
        nrUsers++;
        this.isAdmin = isAdmin;
        userId = nrUsers;
    }
    boolean allresults = true;
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        try (Socket socket = new Socket("127.0.0.1", 5012)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Step 1: Send Store to MasterServer
            for (Store store : myStores) {
                out.writeObject(store);
                out.flush();
                System.out.println("Sent store: " + store.name);
            }
            System.out.println("Master sent Store to MasterServer");

            // Step 2: Send acknowledgment to MasterServer to start processing
            out.writeObject("Lat: " + String.valueOf(latitude));
            out.flush();
            out.writeObject("Lon: " + String.valueOf(longitude));
            out.flush();
            out.writeObject("send");
            out.flush();
            Thread.sleep(100);
            
            try {
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());

                while (true) {
                    try {
                        if (dataIn.available() > 0) {
                            int dataLength = dataIn.readInt();
                            byte[] data = new byte[dataLength];
                            dataIn.readFully(data);
                            ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data));
                            Object receivedObject = objIn.readObject();
                            if (receivedObject instanceof Map.Entry<?,?>) {
                                Map.Entry<Integer,List<?>> entry = (Map.Entry <Integer,List<?>>) receivedObject;
                                List<Store> tempList = (List<Store>) entry.getValue();
                                if (!tempList.isEmpty() && tempList.get(0) instanceof Store) {
                                    for (Store store : tempList) {
                                        System.out.println("Processed (And by that I mean received) store: " + store.name);
                                    }
                                }
                            }
                            else{
                            }
                        }
                        if (allresults) {
                            System.out.println("What do you want to do?");
                            System.out.println("1. See all the stores");
                            System.out.println("2. Add a new store");
                            System.out.println("3. Edit a store");
                            System.out.println("4. View store details");
                            String command = scanner.nextLine();
                            System.out.println(command);
                            if (command.equals("1")) {out.writeObject("send");Thread.sleep(100);}
                            if (command.equals("2")) {
                                Store newStore = newStore(scanner);
                                out.writeObject(newStore);
                            }
                            if (command.equals("3")) {
                                System.out.println("Which store do you want to edit?");
                                out.writeObject("send");
                                Thread.sleep(100);
                                int dataLength = dataIn.readInt();
                                byte[] data = new byte[dataLength];
                                dataIn.readFully(data);
                                ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data));
                                Object receivedObject = objIn.readObject();
                                if (receivedObject instanceof Map.Entry<?,?>) {
                                    Map.Entry<Integer,List<?>> entry = (Map.Entry <Integer,List<?>>) receivedObject;
                                    List<Store> tempList = (List<Store>) entry.getValue();
                                    tempList.sort(Comparator.comparingInt(Store::getStoreID));
                                    myStores = tempList;
                                    if (!tempList.isEmpty() && tempList.get(0) instanceof Store) {for (Store store : tempList) {System.out.println(store.storeID + ". " + store.name);}}}
                                System.out.println("Press the corresponding number to select a store");
                                int storeID =  Integer.parseInt(scanner.nextLine());
                                Store myStore = getByStoreID(storeID, myStores);
                                String response = editStore(scanner, myStore);
                                System.out.println(response);
                                out.writeObject(response);
                            }
                            if (command.equals("4")) {out.writeObject("send");Thread.sleep(100);
                                int dataLength = dataIn.readInt();
                                byte[] data = new byte[dataLength];
                                dataIn.readFully(data);
                                ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data));
                                Object receivedObject = objIn.readObject();
                                if (receivedObject instanceof Map.Entry<?,?>) {
                                    Map.Entry<Integer,List<?>> entry = (Map.Entry <Integer,List<?>>) receivedObject;
                                    List<Store> tempList = (List<Store>) entry.getValue();
                                    tempList.sort(Comparator.comparingInt(Store::getStoreID));
                                    myStores = tempList;
                                    if (!tempList.isEmpty() && tempList.get(0) instanceof Store) {for (Store store : tempList) {System.out.println(store.storeID + ". " + store.name);}}}
                                System.out.println("Press the corresponding number to select a store");
                                int storeID =  Integer.parseInt(scanner.nextLine());
                                Store myStore = getByStoreID(storeID, myStores);
                                System.out.println(myStore.detailedToString());
                            }
                        }      
                    } catch (IOException e) {break;}}
            } catch (Exception e) {System.out.println("Connection closed or error: " + e.getMessage());}
        } catch (IOException e) {e.printStackTrace();} catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    static List<Store> myStores = new ArrayList<Store>();

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
                    Parser parser = new Parser(file.getPath());
                    String[] myData = parser.getStore();
                    String[][] myProducts = parser.getProducts();

                    Store myStore = new Store(
                            myData[0], Double.parseDouble(myData[1]), Double.parseDouble(myData[2]),
                            myData[3], Double.parseDouble(myData[4]), Integer.parseInt(myData[5]),
                            myData[6], id + 1
                    );
                    id++;
                    for (String[] myProdData : myProducts) {
                        Product myProduct = new Product(
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

    public String editStore(Scanner scanner, Store store) {
        Boolean ready = false;
        System.out.println("What do you want to edit?");
        System.out.println("1. Add a new product");
        System.out.println("2. Edit a product quantity");
        int answer2 = Integer.parseInt(scanner.nextLine());
        if (answer2 == 1) {
        while (!ready) {
            System.out.println("Enter the product name");
            String productName = scanner.nextLine();
            System.out.println("Enter the product type");
            String productType = scanner.nextLine();
            System.out.println("Enter the product price");
            String productPrice = scanner.nextLine();
            System.out.println("Enter the product amount");
            String productAmount = scanner.nextLine();
            System.out.println("This is your product: ");
            System.out.println("Name: " + productName);
            System.out.println("Type: " + productType);
            System.out.println("Price: " + productPrice + " €");
            System.out.println("Amount: " + productAmount + " pcs");
            System.out.println("For the store: " + store.name);
            System.out.println("Is this information correct? Y/N");
            String answer3 = scanner.nextLine();
            if (answer3.equals("Y")) {ready = true; return "newProd::" + store.storeID + "::" + productName + "::" + productType + "::" + productPrice + "::" + productAmount;}
        }
        }
        else if (answer2 == 2) {
            int i = 0;
            System.out.println("Select the product to edit");
            for (Product p : store.getProducts()) {i++; System.out.println(i + ". " + p.getName());}
            answer2 = Integer.parseInt(scanner.nextLine());
            Product myProduct = store.getProducts().get(answer2 - 1);
            System.out.println("How much of " + myProduct.getName() + " is in stock?");
            int answer3 = Integer.parseInt(scanner.nextLine());
            return "changeAvailability::" + store.storeID + "::" + myProduct.getName() + "::" + answer3;
        }
return null;
    }

    public void sell() {
        Scanner scanner = new Scanner(System.in);
        int i = 1;
        myStores.sort(Comparator.comparingInt(Store::getStoreID));
        for (Store store : myStores) {
            System.out.println(i + ". " + store.name);
            i++;
        }
        System.out.println("Which store would you like to edit?");
        int answer = Integer.parseInt(scanner.nextLine());
        i = 0;
        for (Product product : myStores.get(answer - 1).getProducts()) {
            i++;
            System.out.println(i + ". " + product.getName());
        }
        int answer2 = Integer.parseInt(scanner.nextLine());
        myStores.get(answer - 1).sell(answer2 - 1);
        i = 0;
        for (Store store : myStores) {
            System.out.println(i + ". " + myStores.get(i).toString());
            i++;
        }
    }


    public Store getByStoreID(int storeID, List<Store> stores) {
        for (Store store : stores) {if (store.storeID == storeID){return store;}}
        return null;
    }


    public Store newStore(Scanner scanner) throws IOException {
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
        Store myStore = new Store(storeName, Double.valueOf(storeLat), Double.valueOf(storeLon), storeType, Double.valueOf(storeStars), Integer.parseInt(storeRatings), storeLogo, myStores.size());
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
            if (nrProducts == 1) {
                myStore.addProduct(productName, productType, Integer.parseInt(productAmount), Double.parseDouble(productPrice));
            } else {
                myStore.addProduct(productName, productType, Integer.parseInt(productAmount), Double.parseDouble(productPrice));
            }
            if (scanner.nextLine().equals("N")) {
                moreProducts = false;
            }
        }
        return myStore;
    }

    public void seeAllAvailableStores() {
        for (int i = 0; i < myStores.size(); i++) {
            System.out.println((i + 1) + ". " + myStores.get(i).toString());
        }
    }

    public static Set<String> getStoreCategories() {
        Set<String> categories = new HashSet<>();
        for (Store store : myStores) {
            categories.add(store.foodCategory);
        }
        return categories;
    }

    public static void seeAvailableStores(double lon, double lat, int buckets) {
        // Calculate distance or apply filters based on lon and lat if needed
        for (int i = 0; i < myStores.size(); i++) {
            System.out.println((i + 1) + ". " + myStores.get(i).toString() + " bucket: " + myStores.get(i).storeID % buckets);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Manager manager = new Manager(23.333, 21.2478, false);
        int buckets = 3;
        manager.read("src/main/resources");
        manager.seeAvailableStores(23.333, 21.2478, buckets);
        manager.start();
    }
}