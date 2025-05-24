package org.example;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class customer2 extends Thread {
    double latitude;
    double longitude;
    int userId;
    static int nrUsers = 0;
    List<Order> myOrders = new ArrayList<>();
    boolean isAdmin;

    public customer2(double longitude, double latitude, boolean isAdmin) {
        this.longitude = longitude;
        this.latitude = latitude;
        nrUsers++;
        this.isAdmin = isAdmin;
        userId = nrUsers;}
    
    boolean allresults = true;
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        try (Socket socket = new Socket("127.0.0.11", 5012)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            

            // Step 2: Send acknowledgment to MasterServer to start processing
            out.writeObject("Lat::" + String.valueOf(latitude));
            out.flush();
            out.writeObject("Lon::" + String.valueOf(longitude));
            out.flush();
            Thread.sleep(300);

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
                                    System.out.println("STORE LIST: ");
                                    for (Store store : tempList) {
                                        System.out.println(store.name);
                                    }
                                }
                            }
                            else{
                            }
                        }
                        if (allresults) {
                            System.out.println("What do you want to do?");
                            System.out.println("1. See all the stores");
                            System.out.println("2. Apply Filters");
                            System.out.println("3. Clear Filters");
                            System.out.println("4. Order");
                            String command = scanner.nextLine();
                            System.out.println(command);
                            if (command.equals("1")) {out.writeObject("send");Thread.sleep(200);}
                            if (command.equals("2")) {out.writeObject("send");Thread.sleep(200);
                                int dataLength = dataIn.readInt();
                                byte[] data = new byte[dataLength];
                                dataIn.readFully(data);
                                ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data));
                                Object receivedObject = objIn.readObject();
                                if (receivedObject instanceof Map.Entry<?,?>) {
                                    Map.Entry<Integer, List<?>> entry = (Map.Entry<Integer, List<?>>) receivedObject;
                                    List<Store> tempList = (List<Store>) entry.getValue();
                                    tempList.sort(Comparator.comparingInt(Store::getStoreID));
                                    if (!tempList.isEmpty() && tempList.get(0) instanceof Store) {
                                        int i = 0;
                                        String storeCategories = "categories::" + getStoreCategories(tempList);
                                        String storePriceRanges = "prices::" + getRanges(List.of("$","$$","$$$"));
                                        String storeRatingRanges = "ratings::" + getRanges(List.of("0","1","2","3","4","5"));
                                        out.writeObject(storeCategories + "::" + storePriceRanges + "::" + storeRatingRanges);
                                        Thread.sleep(200);
                                        out.writeObject("send");
                                        Thread.sleep(200);
                                    }
                                }
                            }
                            else if (command.equals("3")) {
                                out.writeObject("Lat::" + String.valueOf(latitude));
                                out.flush();
                                out.writeObject("Lon::" + String.valueOf(longitude));
                                out.flush();
                                out.writeObject("send");
                                Thread.sleep(300);
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
                                String myOrder = order(myStore);
                                System.out.println(myOrder);
                            }
                        }
                    } catch (IOException e) {break;}}
            } catch (Exception e) {System.out.println("Connection closed or error: " + e.getMessage());}
        } catch (IOException e) {e.printStackTrace();} catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static List<Store> myStores = new ArrayList<Store>();

    public static String getStoreCategories(List<Store> myStores) {
        List<String> categories = new ArrayList<>();
        List<Integer> catPicks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        // Collect unique food categories
        for (Store store : myStores) {
            if (!categories.contains(store.foodCategory)) {
                categories.add(store.foodCategory);
            }
        }

        // Show instructions
        System.out.println("Which categories do you want to search for?");
        System.out.println("Type the number to toggle selection. Type 0 to finish.");

        int choice;
        do {
            // Print categories with checkmarks
            for (int i = 0; i < categories.size(); i++) {
                String prefix = catPicks.contains(i) ? "✅ " : (i + 1) + ". ";
                System.out.println(prefix + categories.get(i));
            }

            // Read input
            choice = scanner.nextInt();

            if (choice > 0 && choice <= categories.size()) {
                int index = choice - 1;
                if (catPicks.contains(index)) {
                    catPicks.remove(Integer.valueOf(index));
                } else {
                    catPicks.add(index);
                }
            } else if (choice != 0) {
                System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 0);

        // Build and return the comma-separated string of selected categories
        List<String> selectedCategories = new ArrayList<>();
        for (int index : catPicks) {
            selectedCategories.add(categories.get(index));
        }
        return String.join(",", selectedCategories);
    }

    public static String getRanges(List<String> options) {
        List<Integer> selectedIndexes = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Select the price ranges you are interested in:");
        System.out.println("Type the number to toggle selection. Type 0 to finish.");

        int choice;
        do {
            // Display options with current selection state
            for (int i = 0; i < options.size(); i++) {
                String prefix = selectedIndexes.contains(i) ? "✅ " : (i + 1) + ". ";
                System.out.println(prefix + options.get(i));
            }

            // Read user input
            choice = scanner.nextInt();

            if (choice > 0 && choice <= options.size()) {
                int index = choice - 1;
                if (selectedIndexes.contains(index)) {
                    selectedIndexes.remove(Integer.valueOf(index));
                } else {
                    selectedIndexes.add(index);
                }
            } else if (choice != 0) {
                System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 0);

        // Build and return comma-separated selected price ranges
        List<String> selectedPrices = new ArrayList<>();
        for (int index : selectedIndexes) {
            selectedPrices.add(options.get(index));
        }
        return String.join(",", selectedPrices);
    }



    public Store getByStoreID(int storeID, List<Store> stores) {
        for (Store store : stores) {if (store.storeID == storeID){return store;}}
        return null;
    }

    public String order(Store store) {
        Scanner scanner = new Scanner(System.in);
        Map<Product, Integer> cart = new LinkedHashMap<>();
        List<Product> products = store.getProducts();
        int choice = -1;

        while (true) {
            // Display cart
            System.out.println("\n--- Your Cart ---");
            double total = 0;
            if (cart.isEmpty()) {
                System.out.println("Cart is empty.");
            } else {
                for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                    Product product = entry.getKey();
                    int quantity = entry.getValue();
                    double price = quantity * product.getPrice();
                    System.out.println(quantity + "x " + product.getName() + " " + price + " €");
                    total += price;
                }
                System.out.println("-----\nTotal: " + total + " €");
            }

            System.out.println("\n--- Menu ---");
            for (int i = 0; i < products.size(); i++) {System.out.println((i + 1) + ". Add " + products.get(i).getName());}
            System.out.println("R. Remove product from cart");
            System.out.println("0. Submit order");
            System.out.print("Your choice: ");
            String input = scanner.nextLine().trim();
            if (input.equals("0")) {
                break;
            } else if (input.equalsIgnoreCase("R")) {
                System.out.println("Enter product number to remove:");
                for (int i = 0; i < products.size(); i++) {
                    System.out.println((i + 1) + ". " + products.get(i).getName());
                }
                try {
                    int removeChoice = Integer.parseInt(scanner.nextLine());
                    if (removeChoice >= 1 && removeChoice <= products.size()) {
                        Product toRemove = products.get(removeChoice - 1);
                        if (cart.containsKey(toRemove)) {
                            cart.remove(toRemove);
                            System.out.println(toRemove.getName() + " removed from cart.");
                        } else {
                            System.out.println("Product not in cart.");
                        }
                    } else {
                        System.out.println("Invalid product number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                }
            } else {
                try {
                    int productChoice = Integer.parseInt(input);
                    if (productChoice >= 1 && productChoice <= products.size()) {
                        Product selectedProduct = products.get(productChoice - 1);
                        System.out.print("Enter quantity: ");
                        int quantity = Integer.parseInt(scanner.nextLine());
                        if (quantity <= 0) {
                            System.out.println("Quantity must be positive.");
                            continue;
                        }
                        cart.put(selectedProduct, cart.getOrDefault(selectedProduct, 0) + quantity);
                        System.out.println(quantity + "x " + selectedProduct.getName() + " added to cart.");
                    } else {
                        System.out.println("Invalid product number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                }
            }
        }
        
        StringBuilder result = new StringBuilder("neworder::" + store.getStoreID());
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            result.append("::").append(entry.getKey().getName())
                    .append("::").append(entry.getValue());
        }
        myOrders.add(new Order( String.valueOf(store.storeID),4.3, userId, cart));
        System.out.println("\nFinal Order Submitted.");
        return result.toString();
    }



    public static void seeAvailableStores(double lon, double lat, int buckets) {
        // Calculate distance or apply filters based on lon and lat if needed
        for (int i = 0; i < myStores.size(); i++) {
            System.out.println((i + 1) + ". " + myStores.get(i).toString() + " bucket: " + myStores.get(i).storeID % buckets);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Welcome to the food ordering app!");
        System.out.println("Please enter your coordinates");
        System.out.println("What is your latitude?");
        Scanner scanner = new Scanner(System.in);
        Double latitude = Double.parseDouble(scanner.nextLine());
        System.out.println("What is your longitude?");
        Double longitude = Double.parseDouble(scanner.nextLine());
        customer2 manager = new customer2(longitude, latitude, false);
        manager.start();
    }
}