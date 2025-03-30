package org.example;


import java.util.*;

public class store{

    protected String name;
    private Double latitude;
    private Double longitude;
    protected String foodCategory;
    private Double stars;
    private int votes;
    private String logo;
    protected List<product> products;
    protected int storeID;

    public store(String name, Double latitude, Double longitude, String foodCategory, Double stars, int votes, String logo, int storeID) {
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

    public void addProduct(String name, String type, int amount ,Double price){
        products.add(new product(name,type,amount,price));
    }

    public void addProduct(product p){
        products.add(p);
    }

    public int getStoreID() {
        return storeID;
    }

    public List<product> getProducts() {
        return products;
    }
    
    public String getAvgPrice(){
        int sum = 0;
        for (product product : products) {
            sum += product.getPrice();
        }
        if (sum/products.size() < 5){return "$";}
        else if (sum/products.size() < 10){return "$$";}
        return "$$$";
    }
    
    public int getTotalSales(){
            int sales = 0;
            for (product product : products) sales += product.getSales();
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
        for (product p : products) {
            i++;
            System.out.println(i + ". " + p.getName() + " " + p.getType() +" ("+ p.getPrice() + " € )");
        }
    }
    
    public void sell(int p){
        products.get(p).sell();
        }

    @Override
    public String toString() {
        return   name  + ": " + foodCategory + " ( " + getAvgPrice() + " ) rating: " + stars + "/5 (" + votes + ") " + "total sales: " + getTotalSales();
    }
}