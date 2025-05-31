package org.example;

import java.io.Serializable;
import java.util.*;

public class Store implements Serializable {

    protected String name;
    private Double latitude;
    private Double longitude;
    protected String foodCategory;
    Double stars;
    private int votes;
    private String logo;
    protected List<Product> products;
    protected int storeID;

    public Store(String name, Double latitude, Double longitude, String foodCategory, Double stars, int votes, String logo, int storeID) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.foodCategory = foodCategory;
        this.stars = stars;
        this.votes = votes;
        this.logo = logo;
        this.products = new ArrayList<>();
        this.storeID = storeID;
    }
    
    public String getName(){
        return name;
    }

    public void addProduct(String name, String type, int amount ,Double price){
        products.add(new Product(name,type,amount,price));
    }

    public void addProduct(Product p){
        products.add(p);
    }

    public int getStoreID() {
        return storeID;
    }

    public List<Product> getProducts() {
        return products;
    }
    
    public void registerSale(String productName, int productAmmount){
        for (Product product : products) {
            if (product.getName().equals(productName)) {
                product.sell(productAmmount);
            }
        }
    }
    
    public String getAvgPrice(){
        int sum = 0;
        for (Product product : products) {
            sum += product.getPrice();
        }
        if (sum/products.size() < 5){return "$";}
        else if (sum/products.size() < 10){return "$$";}
        return "$$$";
    }
    
    public int getTotalSales(){
            int sales = 0;
            for (Product product : products) sales += product.getSales();
         return sales;}

    public boolean isWithin5km(double lat, double lon) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(latitude - lat);
        double lonDistance = Math.toRadians(longitude - lon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distance in km
        return distance <= 5;
    }
    
    public void printProducts(){
        int i = 0;
        for (Product p : products) {
            i++;
            System.out.println(i + ". " + p.getName() + " " + p.getType() +" ("+ p.getPrice() + " € )");
        }
    }
    
    public void sell(int p){
        products.get(p).sell(1);
        }

    @Override
    public String toString() {
        return  storeID + ". " + name  + ": " + foodCategory + " ( " + getAvgPrice() + " ) rating: " + stars + "/5 (" + votes + ") " + "total sales: " + getTotalSales();
    }

    public String detailedToString() {
        String answer = "========================================================================================\nSTORE ID: " + storeID +
                "\nSTORE NAME: " + name + 
                "\nSTORE AVERAGE PRICE: " + getAvgPrice() + 
                "\nSTORE TYPE: " + foodCategory + 
                "\nSTORE RATINGS: " + stars + " (" + votes + ")\n"
                +"========================================================================================\nPRODUCTS:";
                for(Product p: getProducts()){ answer += p.detailedToString();}
                return answer +"\n========================================================================================" + "\nSTORE TOTAL SALES: " + getTotalSales() + "\n========================================================================================";

    }
}