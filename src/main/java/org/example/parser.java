package org.example;
import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class parser {

    String[][] productArray;
    static String path;

    public parser(String path) {
        this.path = path;
    }

    public static String[][] getProducts() throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(path)));
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray products = jsonObject.getJSONArray("Products");

        if (!products.isEmpty()) {
            String[][] productData = new String[products.length()][4];

            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);
                productData[i][0] = product.getString("ProductName");
                productData[i][1] = product.getString("ProductType");
                productData[i][2] = String.valueOf(product.getInt("Available Amount"));
                productData[i][3] = String.valueOf(product.getDouble("Price"));
            }

            return productData;
        } else {
            return new String[][]{{"No products found in this store."}};
        }
    }
    
    public static String[] getStore() throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(path)));
        JSONObject Store = new JSONObject(jsonString);

        return new String[]{
                Store.getString("StoreName"),
                String.valueOf(Store.getDouble("Latitude")),
                String.valueOf(Store.getDouble("Longitude")),
                Store.getString("FoodCategory"),
                String.valueOf(Store.getDouble("Stars")),
                String.valueOf(Store.getInt("NoOfVotes")),
                Store.getString("StoreLogo")
        };
    }

    public static void main(String[] args) throws IOException {
    }
}