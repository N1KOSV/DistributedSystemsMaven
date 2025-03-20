package org.example;
import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

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

    public static String createProductText(String[] productInfo) {
        String productName = productInfo[0];
        String productType = productInfo[1];
        int availableAmount = Integer.parseInt(productInfo[2]);
        double price = Double.parseDouble(productInfo[3]);
        String result = "";
        result += ("{\n");
        result += ("\"ProductName\":\"") + (productName) + ("\",\n");
        result += ("\"Price\":") + (price) + (",\n");
        result += ("\"ProductType\":\"") + (productType) + ("\",\n");
        result += ("\"Available Amount\":") + (availableAmount+"\n");
        result += ("}\n");
        return result;
    }

    public static void addProductJson(String[] stringToInsert) throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(path)));
        // Find the last occurrence of "]"
        int lastClosingBracketIndex = jsonString.lastIndexOf("]");
        if (lastClosingBracketIndex != -1){
            // Insert the string and a newline before the closing bracket
            String modifiedJson = jsonString.substring(0, lastClosingBracketIndex - 1) + "," + "\n" + createProductText(stringToInsert) + jsonString.substring(lastClosingBracketIndex);
            // Write the modified JSON back to the file
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) { writer.write(modifiedJson);}
        } else System.out.println("Error: Closing bracket ']' not found.");
    }

    public static void createJsonFile(String filePath, String storeName, double latitude, double longitude,
                                      String foodCategory, double stars, int noOfVotes, String storeLogo,
                                      String[][] productData) throws IOException {

        StringBuilder jsonString = new StringBuilder("{\n");
        jsonString.append("\"StoreName\":\"").append(storeName).append("\",\n");
        jsonString.append("\"Latitude\":").append(latitude).append(",\n");
        jsonString.append("\"Longitude\":").append(longitude).append(",\n");
        jsonString.append("\"FoodCategory\":\"").append(foodCategory).append("\",\n");
        jsonString.append("\"Stars\":").append(stars).append(",\n");
        jsonString.append("\"NoOfVotes\":").append(noOfVotes).append(",\n");
        jsonString.append("\"StoreLogo\":\"").append(storeLogo).append("\",\n");

        jsonString.append("\"Products\": [\n");
        if (productData != null && productData.length > 0) {
            for (int i = 0; i < productData.length; i++) {
                String[] productInfo = productData[i];
                if (productInfo != null && productInfo.length == 4) {
                    jsonString.append(createProductText(productInfo));
                    if (i < productData.length - 1) jsonString.append(",\n");
                }
            }
        }
        jsonString.append("]\n}");

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonString.toString());
        }
    }

    public static void changeAvailableAmount(String filePath, String productName, int newAmount) throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(filePath)));
        // Find the product entry and modify its Available Amount
        String regex = "(\"ProductName\"\\s*:\\s*\"" + Pattern.quote(productName) + "\"\\s*,\\s*[^}]*?\"Available Amount\"\\s*:\\s*)\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsonString);
        if (matcher.find()) {
            // Replace the old Available Amount with the new value
            String updatedJson = matcher.replaceFirst(matcher.group(1) + newAmount);
            // Write back to file
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
                writer.write(updatedJson);
            }
        }
    }

    public static void decreaseAvailableAmount(String filePath, String productName) throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(filePath)));

        // Find the product entry and modify its Available Amount
        String regex = "(\"ProductName\"\\s*:\\s*\"" + Pattern.quote(productName) + "\"\\s*,\\s*[^}]*?\"Available Amount\"\\s*:\\s*)(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsonString);

        if (matcher.find()) {
            int currentAmount = Integer.parseInt(matcher.group(2));
            if (currentAmount > 0) {
                int newAmount = currentAmount - 1;
                String updatedJson = matcher.replaceFirst(matcher.group(1) + newAmount);

                // Write back to file
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
                    writer.write(updatedJson);
                }
            } else {
                System.out.println("Error: Available Amount is already zero.");
            }
        } else {
            System.out.println("Error: Product not found.");
        }
    }



    public static void main(String[] args) throws IOException {
        String filepath = "src/main/resources/Store" + "18" + ".json" ;
        String[][] productData = {{"aaaaa", "rrrr", "3", "3.6"}};
        String[] prodInfo = {"aaabaa", "rrdrrr", "33", "3.63"};
        createJsonFile(filepath,"Magazi",22.29292,22.8884,"Pizza",3,221,"f",productData);
    }
}