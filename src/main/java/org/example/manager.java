package org.example;

import java.io.IOException;
import java.util.Comparator;
import java.util.Scanner;

public class manager {
    
    static master Master = new master();
    
    public static void main(String[] args) throws IOException {
        Master.read("src/main/resources");


        Scanner scanner = new Scanner(System.in);

        System.out.print("What would you like to do?\n");
        System.out.print("1. See all the available stores\n");
        System.out.print("2. Add a new store\n");
        System.out.print("3. Edit a store\n");
        System.out.print("4. Mark a sale\n");

        int choice = Integer.parseInt(scanner.nextLine());
        while (choice != 5) {
        if (choice == 1) {Master.seeAvailableStores();}
        else if (choice == 2) {Master.newStore(scanner);}
        else if (choice == 3) {Master.editStore();}
        else if (choice == 4) {Master.sell();}
        choice = Integer.parseInt(scanner.nextLine());

    }
    }    
    
}
