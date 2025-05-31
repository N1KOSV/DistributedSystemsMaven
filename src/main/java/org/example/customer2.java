package org.example;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class customer2 extends Thread {
    private double latitude;
    private double longitude;
    private int userId;
    private static int nrUsers = 0;
    private List<Order> myOrders = new ArrayList<>();
    private boolean isAdmin;
    private List<Store> myStores = new ArrayList<>();

    public customer2(double longitude, double latitude, boolean isAdmin) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.isAdmin = isAdmin;
        synchronized (customer2.class) {
            nrUsers++;
            userId = nrUsers;
        }
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        try (Socket socket = new Socket("127.0.0.11", 5012);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Στέλνουμε αρχικά latitude και longitude στον server
            out.writeObject("Lat::" + latitude);
            out.flush();
            out.writeObject("Lon::" + longitude);
            out.flush();

            // Κύριο loop για επιλογές χρήστη
            while (true) {
                System.out.println("\nWhat do you want to do?");
                System.out.println("1. See all the stores");
                System.out.println("2. Apply Filters");
                System.out.println("3. Clear Filters");
                System.out.println("4. Order");
                System.out.println("0. Exit");
                System.out.print("Your choice: ");

                String command = scanner.nextLine().trim();

                if (command.equals("0")) {
                    System.out.println("Exiting...");
                    break;
                }

                switch (command) {
                    case "1":
                        // Ζητάμε λίστα καταστημάτων
                        out.writeObject("send");
                        out.flush();
                        receiveAndPrintStores(in);
                        break;

                    case "2":
                        // Ζητάμε φίλτρα (προετοιμασία)
                        out.writeObject("send");
                        out.flush();
                        List<Store> storesForFilters = receiveStores(in);
                        if (storesForFilters != null && !storesForFilters.isEmpty()) {
                            String categories = getStoreCategories(storesForFilters);
                            String prices = getRanges(Arrays.asList("$", "$$", "$$$"));
                            String ratings = getRanges(Arrays.asList("0", "1", "2", "3", "4", "5"));
                            String filterMessage = "categories::" + categories + "::prices::" + prices + "::ratings::" + ratings;
                            out.writeObject(filterMessage);
                            out.flush();
                            // Μετά ζητάμε ξανά filtered stores
                            out.writeObject("send");
                            out.flush();
                            receiveAndPrintStores(in);
                        } else {
                            System.out.println("No stores to filter.");
                        }
                        break;

                    case "3":
                        // Καθαρίζουμε φίλτρα, ξαναστέλνουμε αρχικές συντεταγμένες
                        out.writeObject("Lat::" + latitude);
                        out.flush();
                        out.writeObject("Lon::" + longitude);
                        out.flush();
                        out.writeObject("send");
                        out.flush();
                        receiveAndPrintStores(in);
                        break;

                    case "4":
                        // Παραγγελία: παίρνουμε καταστήματα και επιλέγουμε
                        out.writeObject("send");
                        out.flush();
                        myStores = receiveStores(in);
                        if (myStores == null || myStores.isEmpty()) {
                            System.out.println("No stores available.");
                            break;
                        }
                        myStores.sort(Comparator.comparingInt(Store::getStoreID));
                        System.out.println("Stores:");
                        for (Store s : myStores) {
                            System.out.println(s.getStoreID() + ". " + s.getName());
                        }
                        System.out.print("Select store by number: ");
                        try {
                            int storeID = Integer.parseInt(scanner.nextLine());
                            Store selectedStore = getByStoreID(storeID, myStores);
                            if (selectedStore == null) {
                                System.out.println("Invalid store selection.");
                                break;
                            }
                            String orderStr = order(selectedStore);
                            out.writeObject(orderStr);
                            out.flush();
                            System.out.println("Order sent: " + orderStr);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input.");
                        }
                        break;

                    default:
                        System.out.println("Invalid command.");
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("Connection error or exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void receiveAndPrintStores(ObjectInputStream in) throws IOException, ClassNotFoundException {
        List<Store> stores = receiveStores(in);
        if (stores == null || stores.isEmpty()) {
            System.out.println("No stores received.");
            return;
        }
        System.out.println("STORE LIST:");
        for (Store s : stores) {
            System.out.println(s.getName());
        }
    }

    private List<Store> receiveStores(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj instanceof Map.Entry<?, ?>) {
            Map.Entry<Integer, List<?>> entry = (Map.Entry<Integer, List<?>>) obj;
            List<?> list = entry.getValue();
            if (!list.isEmpty() && list.get(0) instanceof Store) {
                //noinspection unchecked
                return (List<Store>) list;
            }
        }
        return Collections.emptyList();
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
        Scanner scanner = new Scanner(System.in);
        List<Integer> selectedIndexes = new ArrayList<>();

        System.out.println("Select ranges to filter (type number to toggle, 0 to finish):");

        int choice;
        do {
            for (int i = 0; i < options.size(); i++) {
                String prefix = selectedIndexes.contains(i) ? "✅ " : (i + 1) + ". ";
                System.out.println(prefix + options.get(i));
            }
            choice = readIntSafe(scanner);
            if (choice > 0 && choice <= options.size()) {
                if (selectedIndexes.contains(choice - 1)) {
                    selectedIndexes.remove(Integer.valueOf(choice - 1));
                } else {
                    selectedIndexes.add(choice - 1);
                }
            } else if (choice != 0) {
                System.out.println("Invalid choice.");
            }
        } while (choice != 0);

        List<String> selected = new ArrayList<>();
        for (int idx : selectedIndexes) {
            selected.add(options.get(idx));
        }
        return String.join(",", selected);
    }

    private static int readIntSafe(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public Store getByStoreID(int storeID, List<Store> stores) {
        for (Store store : stores) {
            if (store.getStoreID() == storeID) return store;
        }
        return null;
    }

    public String order(Store store) {
        Scanner scanner = new Scanner(System.in);
        Map<Product, Integer> cart = new LinkedHashMap<>();
        List<Product> products = new ArrayList<>(store.getProducts());

        // Remove products with zero stock
        products.removeIf(p -> p.getAmount() == 0);

        while (true) {
            System.out.println("\n--- Your Cart ---");
            if (cart.isEmpty()) {
                System.out.println("Cart is empty.");
            } else {
                double total = 0;
                for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                    Product product = entry.getKey();
                    int qty = entry.getValue();
                    double price = qty * product.getPrice();
                    System.out.println(qty + "x " + product.getName() + " - " + price + " €");
                    total += price;
                }
                System.out.println("-----\nTotal: " + total + " €");
            }

            System.out.println("\n--- Menu ---");
            for (int i = 0; i < products.size(); i++) {
                System.out.println((i + 1) + ". Add " + products.get(i).getName());
            }
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
                    int removeIdx = Integer.parseInt(scanner.nextLine());
                    if (removeIdx >= 1 && removeIdx <= products.size()) {
                        Product toRemove = products.get(removeIdx - 1);
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
                        int qty = Integer.parseInt(scanner.nextLine());
                        if (qty <= 0) {
                            System.out.println("Quantity must be positive.");
                            continue;
                        }
                        if (qty > selectedProduct.getAmount()) {
                            System.out.println("Only " + selectedProduct.getAmount() + " of " + selectedProduct.getName() + " available.");
                            continue;
                        }
                        cart.put(selectedProduct, cart.getOrDefault(selectedProduct, 0) + qty);
                        System.out.println(qty + "x " + selectedProduct.getName() + " added to cart.");
                    } else {
                        System.out.println("Invalid product number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                }
            }
        }

        StringBuilder sb = new StringBuilder("neworder::" + store.getStoreID());
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            sb.append("::").append(entry.getKey().getName())
              .append("::").append(entry.getValue());
        }
        myOrders.add(new Order(String.valueOf(store.getStoreID()), 4.3, userId, cart));
        return sb.toString();
    }

    public static void main(String[] args) {
        customer2 client = new customer2(23.76, 38.01, false);
        client.start();
    }
}