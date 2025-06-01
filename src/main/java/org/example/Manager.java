package org.example;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Manager extends Thread {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 5012;

    private double latitude;
    private double longitude;
    private boolean isAdmin;
    private int userId;

    private static int nrUsers = 0;
    private static List<Store> myStores = new ArrayList<>();

    public Manager(double longitude, double latitude, boolean isAdmin) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.isAdmin = isAdmin;
        userId = ++nrUsers;
    }

    @Override
    public void run() {
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in)
        ) {
            out.flush();
            
            for (Store store : myStores) {
                out.writeObject(store);
                out.flush();
                System.out.println("Sent store: " + store.name);
            }
            
            out.writeObject("admin");
            out.flush();

            while (true) {
                showMenu();
                String command = scanner.nextLine();

                switch (command) {
                    case "1":
                        requestAllStores(out);
                        receiveStores(in);
                        break;
                    case "2":
                        Store newStore = createNewStore(scanner);
                        out.writeObject(newStore);
                        out.flush();
                        break;
                    case "3":
                        editStoreProcess(scanner, out, in);
                        break;
                    case "4":
                        viewStoreDetails(scanner, out, in);
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private void showMenu() {
        System.out.println("\nWhat do you want to do?");
        System.out.println("1. See all the stores");
        System.out.println("2. Add a new store");
        System.out.println("3. Edit a store");
        System.out.println("4. View store details");
        System.out.print("> ");
    }

    private void requestAllStores(ObjectOutputStream out) throws IOException {
        out.writeObject("send");
        out.flush();
    }

    private void receiveStores(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object receivedObject = in.readObject();
        if (receivedObject instanceof Map.Entry<?, ?> entry) {
            List<?> list = (List<?>) entry.getValue();
            if (!list.isEmpty() && list.get(0) instanceof Store) {
                List<Store> storeList = (List<Store>) list;
                storeList.sort(Comparator.comparingInt(Store::getStoreID));
                myStores = storeList;
                storeList.forEach(s -> System.out.println(s.storeID + ". " + s.name));
            }
        }
    }

    private void editStoreProcess(Scanner scanner, ObjectOutputStream out, ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        requestAllStores(out);
        receiveStores(in);

        System.out.print("Select store ID to edit: ");
        int storeID = Integer.parseInt(scanner.nextLine());
        Store store = getByStoreID(storeID);

        if (store == null) {
            System.out.println("Store not found.");
            return;
        }

        String command = editStore(scanner, store);
        if (command != null) {
            out.writeObject(command);
            out.flush();
        }
    }

    private void viewStoreDetails(Scanner scanner, ObjectOutputStream out, ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        requestAllStores(out);
        receiveStores(in);

        System.out.print("Select store ID to view details: ");
        int storeID = Integer.parseInt(scanner.nextLine());
        Store store = getByStoreID(storeID);

        if (store != null) {
            System.out.println(store.detailedToString());
        } else {
            System.out.println("Store not found.");
        }
    }

    private Store getByStoreID(int id) {
        return myStores.stream()
            .filter(s -> s.getStoreID() == id)
            .findFirst()
            .orElse(null);
    }

    private String editStore(Scanner scanner, Store store) {
        System.out.println("What do you want to edit?");
        System.out.println("1. Add a new product");
        System.out.println("2. Edit a product quantity");
        System.out.print("> ");
        int choice = Integer.parseInt(scanner.nextLine());

        if (choice == 1) {
            System.out.print("Product name: ");
            String name = scanner.nextLine();
            System.out.print("Product type: ");
            String type = scanner.nextLine();
            System.out.print("Price: ");
            String price = scanner.nextLine();
            System.out.print("Amount: ");
            String amount = scanner.nextLine();

            System.out.println("Add this product to " + store.name + "? (Y/N)");
            if (scanner.nextLine().equalsIgnoreCase("Y")) {
                return "newProd::" + store.storeID + "::" + name + "::" + type + "::" + price + "::" + amount;
            }
        } else if (choice == 2) {
            for (int i = 0; i < store.getProducts().size(); i++) {
                System.out.println((i + 1) + ". " + store.getProducts().get(i).getName());
            }

            System.out.print("Select product: ");
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            Product product = store.getProducts().get(index);

            System.out.print("New quantity for " + product.getName() + ": ");
            int qty = Integer.parseInt(scanner.nextLine());

            return "changeAvailability::" + store.storeID + "::" + product.getName() + "::" + qty;
        }

        return null;
    }

    private Store createNewStore(Scanner scanner) {
        System.out.print("Store name: ");
        String name = scanner.nextLine();
        System.out.print("Type: ");
        String type = scanner.nextLine();
        System.out.print("Latitude: ");
        double lat = Double.parseDouble(scanner.nextLine());
        System.out.print("Longitude: ");
        double lon = Double.parseDouble(scanner.nextLine());
        System.out.print("Stars: ");
        double stars = Double.parseDouble(scanner.nextLine());
        System.out.print("Ratings: ");
        int ratings = Integer.parseInt(scanner.nextLine());
        System.out.print("Logo: ");
        String logo = scanner.nextLine();

        Store store = new Store(name, lat, lon, type, stars, ratings, logo, myStores.size() + 1);

        while (true) {
            System.out.print("Add a product? (Y/N): ");
            if (!scanner.nextLine().equalsIgnoreCase("Y")) break;

            System.out.print("Product name: ");
            String pname = scanner.nextLine();
            System.out.print("Type: ");
            String ptype = scanner.nextLine();
            System.out.print("Price: ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Amount: ");
            int amount = Integer.parseInt(scanner.nextLine());

            store.addProduct(pname, ptype, amount, price);
        }

        myStores.add(store);
        return store;
    }

    public static void readStoresFromFolder(String path) throws IOException {
        File folder = new File(path);
        if (!folder.isDirectory()) return;

        File[] files = folder.listFiles((dir, name) -> name.startsWith("Store") && name.endsWith(".json"));
        if (files == null) return;

        Arrays.sort(files, Comparator.comparingInt(file ->
                Integer.parseInt(file.getName().replaceAll("[^0-9]", "")))
        );

        int id = 1;
        for (File file : files) {
            Parser parser = new Parser(file.getPath());
            String[] data = parser.getStore();
            String[][] products = parser.getProducts();

            Store store = new Store(data[0], Double.parseDouble(data[1]), Double.parseDouble(data[2]),
                    data[3], Double.parseDouble(data[4]), Integer.parseInt(data[5]), data[6], id++);

            for (String[] p : products) {
                store.addProduct(new Product(p[0], p[1], Integer.parseInt(p[2]), Double.parseDouble(p[3])));
            }

            myStores.add(store);
        }
    }

    public static void main(String[] args) throws IOException {
        Manager manager = new Manager(23.333, 21.2478, true);
        readStoresFromFolder("src/main/resources");
        manager.start();
    }
}
