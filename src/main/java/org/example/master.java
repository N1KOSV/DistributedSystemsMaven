package org.example;

import java.io.*;
import java.nio.file.Files;
import java.sql.SQLOutput;
import java.util.*;

public class master {


    static List<store> myStores = new ArrayList<store>();

    public void refresh(String path) throws IOException {
        myStores.clear();
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
                parser parser = new parser("src/main/resources/Store"+ (myStores.size() + 1 )+".json");
                parser.createJsonFile("src/main/resources/Store"+ (myStores.size() + 1 )+".json",storeName,Double.valueOf(storeLat),Double.valueOf(storeLon),storeType,Double.valueOf(storeStars),Integer.parseInt(storeRatings),storeLogo,new String[][] {{productName, productType, productAmount, productPrice}});
            }
            else{
                parser.addProductJson(new String[] {productName, productType, productAmount, productPrice});
            }
            if (scanner.nextLine().equals("N")) {moreProducts = false;}
        }
        int i =0;
        refresh("src/main/resources");
        for (store store : myStores) {
            i++;
            System.out.println(i + ". " + store.toString());
        }
    }

    //Παραλαβή objects product & store
    //Διεπαφή (Console) με τις δυνατότητες του manager
    //Ανοιχτό κανάλι για επικοινωνία από χρήστες
    //Επεξεργασία αιτημάτων χρήστη και προσθήκη φίλτρων
    //Επιστροφή καταστημάτων στον χρήστη βάσει της τοποθεσίας του

    public static void main(String[] args) throws IOException {

        master JJJ = new master();
        JJJ.refresh("src/main/resources");
        System.out.println(myStores.get(0).isWithin5km(38.01,23.74));

        Scanner scanner = new Scanner(System.in);

        System.out.print("What would you like to do?\n");
        System.out.print("1. See all the available stores\n");
        System.out.print("2. Add a new store\n");
        System.out.print("3. Edit a store\n");
        int choice = Integer.parseInt(scanner.nextLine());
        if (choice == 1) {
            int i = 1;
            for (store store : myStores) {
                System.out.println(i + ". " + myStores.get(i).toString());
                i++;
            }
        }
        else if (choice == 2) {JJJ.newStore(scanner);}
        else if (choice == 3) {
            int i = 1;
            myStores.sort(Comparator.comparingInt(store::getStoreID));
            for (store store : myStores) {
                System.out.println(i + ". " + store.name);
                i++;
            }
            System.out.println("Which store would you like to edit?");
            int answer = Integer.parseInt(scanner.nextLine());
            parser parser = new parser("src/main/resources/Store"+answer+".json");
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
                parser.addProductJson(new String[] {productName, productType, productPrice, productAmount});
            }
            else{
                for (product product : myStores.get(answer - 1).getProducts()) {
                    System.out.println(product.getName());
                }
                answer2 = Integer.parseInt(scanner.nextLine());
                System.out.println("Was one item sold? \n Y: Yes\n N: No");
                String sold = scanner.nextLine();
                if (Objects.equals(sold, "Y")){
                    parser.decreaseAvailableAmount("src/main/resources/Store"+answer+".json",myStores.get(answer-1).products.get(answer2 - 1).getName());
                    System.out.println("The product has been sold");
                }
                else {
                    System.out.println("How much of this item is in stock?");
                    int answer3 = Integer.parseInt(scanner.nextLine());
                    parser.changeAvailableAmount("src/main/resources/Store" + answer + ".json", myStores.get(answer - 1).products.get(answer2 - 1).getName(), answer3);

                }
            }
        }
    }
}