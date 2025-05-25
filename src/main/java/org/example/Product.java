package org.example;


import java.io.Serializable;

public class Product implements Serializable {

    private String name;
    private String type;
    private int amount;
    private Double price;
    private int sales;
    
    public Product(String name, String type, int amount , Double price) {
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.price = price;
        sales = 0;
    }
    
    public void sell(int ammount){
        amount -= ammount;
        sales += ammount;
    }
    
    public int getSales() {return sales;}

    public Double getPrice() {return price;}

    public String getName() {return name;}

    public String getType() {return type;}

    public int getAmount() {return amount;}
    
    public void setAmount(int newAmm) {amount =  newAmm;}
    
    public String toString() {
        return name + ": " + type + " " + price + " € " + " (" + amount + " available units) " ;
    }
    
    public String detailedToString() {
        return "\nNAME: " + name + 
        "\tPRICE: " + price +
        "\tTYPE: " + type +
        "\tTOTAL SALES: " + sales;
    }
}