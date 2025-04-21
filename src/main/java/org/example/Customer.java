package org.example;

import java.io.*;
import java.util.*;

public class Customer extends Thread{
    
    double latitude;
    double longitude;
    int userId;
    static int nrUsers = 0;
	static List<String> filters;
	

    ObjectInputStream in;
	ObjectOutputStream out;
    
    
    public Customer(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        nrUsers++;
        userId = nrUsers;
    }

/*	public customer(Socket connection) {
		try {
			out = new ObjectOutputStream(connection.getOutputStream());
			in = new ObjectInputStream(connection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }*/
    
    public void run() {
		try {		
			Worker t =  (Worker)in.readObject();
			out.writeObject(t);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}



	public static void main(String[] args) throws IOException {
		Master.read("src/main/resources");

		Scanner scanner = new Scanner(System.in);
		/*
		System.out.println("Enter your latitude: ");
		double latitude = scanner.nextDouble();
		System.out.println("Enter your longitude: ");
		double longitude = scanner.nextDouble();*/
		double latitude = 38.02233;
		double longitude = 23.7479;
		Master master = new Master(latitude,longitude,false);
		//Customer currentCustomer = new Customer(longitude, latitude);
		
		System.out.print("What would you like to do?\n");
		System.out.print("1. See all the available stores\n");
		System.out.print("2. Add some filters\n");
		
		int choice = scanner.nextInt();
		while (choice != 5) {
			if (choice == 1) {Master.seeAvailableStores(longitude, latitude);}
			else if (choice == 2) {
				int filterChoice = -1;
				while (filterChoice != 0) {
					System.out.println("What would you like to filter by?");
					System.out.println("1. Store Category");
					System.out.println("2. Store Prices");
					System.out.println("3. Store Ratings");
					filterChoice = scanner.nextInt();
					ArrayList<Integer> catPicks = new ArrayList<>();
					ArrayList<Integer> priPicks = new ArrayList<>();
					ArrayList<Integer> ratPicks = new ArrayList<>();
					if (filterChoice == 1) {
						Set<String> categories = Master.getStoreCategories();

						int i = 1;
						for (String category : categories) {
							if (catPicks.contains(i - 1)) {
								System.out.println("✅. " + category);
							} else {
								System.out.println(i + ". " + category);
							}
							i++;
						}
						System.out.println("Which categories do you want to search for? Add a category by typing the number next to it and undo by typing the number again. Press 0 to go back to filters");
						choice = scanner.nextInt();
						while (choice != 0) {
							if (!catPicks.contains(choice - 1)) {
								catPicks.add(choice - 1);
							} else {
								catPicks.remove(choice - 1);
							}
							choice = scanner.nextInt();
						}
						i = 1;
						for (String category : categories) {
							if (catPicks.contains(i - 1)) {
								System.out.println("✅ " + category);
							} else {
								System.out.println(i + ". " + category);
							}
							i++;
						}
						continue;
					} else if (filterChoice == 2) {
						System.out.println("Press the numbers for the prices you want to keep: ");
						System.out.println("1. $");
						System.out.println("2. $$");
						System.out.println("3. $$$");
						System.out.println("Press 0 to finalize");
						choice = scanner.nextInt();
						while (choice != 0) {
							if (!priPicks.contains(choice)) {
								priPicks.add(choice);
							} else {
								priPicks.remove(choice);
							}
							choice = scanner.nextInt();
						}
						if (priPicks.contains(1)) {System.out.println("✅ $");} else {System.out.println("1. $");}
						if (priPicks.contains(2)) {System.out.println("✅ $$");} else {System.out.println("2. $$");}
						if (priPicks.contains(3)) {System.out.println("✅ $$$");} else {System.out.println("3. $$$");}
						continue;
					} else if (filterChoice == 3) {
						System.out.println("What is the lowest rating you want to keep: ");
						ratPicks.add(scanner.nextInt());
						System.out.println("What is the highest rating you want to keep: ");
						int highestRating = scanner.nextInt();
						while (ratPicks.getFirst() > highestRating) {
							System.out.println("The rating you provided is lower than the lowest rating. Please try again.");
							highestRating = scanner.nextInt();
						}
						continue;
					}
					filterChoice = scanner.nextInt();
				}
			System.out.println("Press 0 to exit filters.");
			choice = scanner.nextInt();
		}
	}
}
}
