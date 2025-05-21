package org.example;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class customer2 extends Thread {
    double latitude;
    double longitude;
    int userId;
    static int nrUsers = 0;
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
                            System.out.println("2. Apply Filters");
                            System.out.println("3. Clear Filters");
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
                        }
                    } catch (IOException e) {break;}}
            } catch (Exception e) {System.out.println("Connection closed or error: " + e.getMessage());}
        } catch (IOException e) {e.printStackTrace();} catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static List<Store> myStores = new ArrayList<Store>();
    

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