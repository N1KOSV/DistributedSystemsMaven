package org.example;


public class product {

    private String name;
    private String type;
    private int amount;
    private Double price;
    
    public product(String name, String type, int amount , Double price) {
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.price = price;
    }

    public Double getPrice() {return price;}

    public String getName() {return name;}

    public String getType() {return type;}

    public int getAmount() {return amount;}
}