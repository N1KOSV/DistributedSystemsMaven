package org.example;

import java.io.Serializable;
import java.util.*;

public class Order implements Serializable {

    protected String storeID;
    protected int customerID;
    protected String foodCategory;
    Double stars;
    protected Map<Product, Integer> products;
    protected int orderID;

    public Order(String storeID, Double stars, int orderID, int customerID, Map<Product, Integer> products) {
        this.storeID = storeID;
        this.stars = stars;
        this.products = products;
        this.customerID = customerID;
        this.orderID = orderID;
    }

    public Map<Product, Integer> getProducts() {
        return products;
    }

    public double getTotalPrice(Map<Product, Integer> cart) {
        double sum = 0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            sum += entry.getKey().getPrice() * entry.getValue();
        }
        return sum;
    }


    public void printCart(Map<Product, Integer> cart) {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        double total = 0;
        System.out.println("\n--- Your Cart ---");
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            double lineTotal = product.getPrice() * quantity;
            total += lineTotal;

            System.out.printf("%dx %s %.2f € each → %.2f €%n",
                    quantity,
                    product.getName(),
                    product.getPrice(),
                    lineTotal
            );
        }
        System.out.println("-----");
        System.out.printf("Total: %.2f €%n", total);
    }


    
}