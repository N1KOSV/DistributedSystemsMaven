package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Master extends Thread {
    double latitude;
    double longitude;
    int userId;
    static int nrUsers = 0;
    boolean isAdmin;

    public Master(double longitude, double latitude, boolean isAdmin) {
        this.longitude = longitude;
        this.latitude = latitude;
        nrUsers++;
        this.isAdmin = isAdmin;
        userId = nrUsers;
    }

    @Override
    public void run() {
        try (
                Socket socket = new Socket("127.0.0.1", 5012);  // MasterServer
        ) {
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
            System.out.println("Master sent PROCESS command");

            // Step 3: Set up a loop to continuously read responses
            try {
                // This approach handles both ObjectInputStream and the modified approach
                // from ActionsForMaster where we're sending data with DataOutputStream
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());

                while (true) {
                    try {
                        // Check if there's data available
                        if (dataIn.available() > 0) {
                            // Try to read object size and then the object
                            int dataLength = dataIn.readInt();
                            byte[] data = new byte[dataLength];
                            dataIn.readFully(data);

                            // Deserialize the object
                            ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data));
                            Object receivedObject = objIn.readObject();

                            // Process the received object
                            if (receivedObject instanceof String) {
                                System.out.println("Master received from MasterServer: " + receivedObject);
                            } else if (receivedObject instanceof Store) {
                                Store storeResponse = (Store) receivedObject;
                                System.out.println("Processed store: " + storeResponse.name);
                            }
                        } else {
                            // No data available, sleep briefly to avoid CPU spinning
                            Thread.sleep(100);
                        }
                    } catch (EOFException e) {
                        // Handle end of input gracefully
                        System.out.println("End of input stream");
                        break;
                    } catch (IOException e) {
                        // We might get stream errors if the format doesn't match
                        System.out.println("Stream error: " + e.getMessage());

                        // Try regular object input stream for backward compatibility
                        try {
                            Object receivedObject = in.readObject();
                            if (receivedObject != null) {
                                if (receivedObject instanceof String) {
                                    System.out.println("Master received (legacy mode): " + receivedObject);
                                } else if (receivedObject instanceof Store) {
                                    Store storeResponse = (Store) receivedObject;
                                    System.out.println("Processed store (legacy mode): " + storeResponse.name);
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Legacy read failed: " + ex.getMessage());
                            // Continue trying - don't break the loop
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Connection closed or error: " + e.getMessage());
            }

        } catch (IOException e) {
            System.out.println("Master run failed.");
            e.printStackTrace();
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

    public void editStore() {
        Scanner scanner = new Scanner(System.in);
        int i = 1;
        myStores.sort(Comparator.comparingInt(Store::getStoreID));
        for (Store store : myStores) {
            System.out.println(i + ". " + store.name);
            i++;
        }
        System.out.println("Which store would you like to edit?");
        int answer = Integer.parseInt(scanner.nextLine());
        System.out.println("What do you want to edit?");
        System.out.println("1. Add a new product");
        System.out.println("2. Edit a product quantity");
        int answer2 = Integer.parseInt(scanner.nextLine());
        if (answer2 == 1) {
            System.out.println("Enter the product name");
            String productName = scanner.nextLine();
            System.out.println("Enter the product type");
            String productType = scanner.nextLine();
            System.out.println("Enter the product price");
            String productPrice = scanner.nextLine();
            System.out.println("Enter the product amount");
            String productAmount = scanner.nextLine();
            myStores.get(answer - 1).addProduct(productName, productType, Integer.parseInt(productAmount), Double.parseDouble(productPrice));
        }
        else if (answer2 == 2) {
            for (Product product : myStores.get(answer - 1).getProducts()) System.out.println(product.getName());
            answer2 = Integer.parseInt(scanner.nextLine());
            System.out.println("How much of this item is in stock?");
            int answer3 = Integer.parseInt(scanner.nextLine());
            myStores.get(answer - 1).products.get(answer2 - 1).setAmount(answer3);
        } else if (answer2 == 3) {
            i = 0;
            for (Product product : myStores.get(answer - 1).getProducts()) {
                i++;
                System.out.println(i + ". " + product.getName());
            }
            answer2 = Integer.parseInt(scanner.nextLine());
            myStores.get(answer - 1).products.get(answer2 - 1).setAmount(-1);
        }
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
        int i = 0;

        for (Store store : myStores) {
            i++;
            System.out.println(i + ". " + store.toString() + " - " + myStores.get(i - 1).products.size());
        }
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
        Master master = new Master(23.333, 21.2478, false);
        int buckets = 3;
        master.read("src/main/resources");
        master.seeAvailableStores(23.333, 21.2478, buckets);
        master.start();
    }
}