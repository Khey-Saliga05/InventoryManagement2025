package com.example.ims2;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ProductStorage {

    private static final String FILE_PATH = "data/products.csv";

    public static void saveToCSV(List<Product> products) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH)))) {
            for (Product p : products) {
                writer.println(String.join(",",
                        escape(p.getBarcode()),
                        escape(p.getName()),
                        escape(p.getUsername()), // 3rd column: username
                        escape(p.getUnit()),
                        String.valueOf(p.getQuantity()),
                        String.valueOf(p.getUnitCost()),
                        String.valueOf(p.getTotalCost()),
                        p.getDate().toString(),
                        p.getTime().toString(),
                        escape(p.getStatus()),
                        escape(p.getCategory()),
                        String.valueOf(p.getTotalStockIn()),
                        String.valueOf(p.getTotalStockOut())
                ));
            }
            System.out.println("✅ Saved " + products.size() + " products to CSV.");
        } catch (IOException e) {
            System.err.println("❌ Error saving products to CSV:");
            e.printStackTrace();
        }
    }

    public static List<Product> loadFromCSV() {
        List<Product> products = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("🆕 Created empty products.csv");
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to create CSV file:");
                e.printStackTrace();
            }
            return products;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] parts = parseCSVLine(line);

                if (parts.length < 8) {
                    System.err.println("⚠️ Skipping malformed line " + lineNumber + ": " + line);
                    continue;
                }

                try {
                    String barcode = unescape(parts[0]);
                    String name = unescape(parts[1]);
                    String username = unescape(parts[2]); // Read username (3rd column)
                    String unit = unescape(parts[3]);
                    int quantity = Integer.parseInt(parts[4]);
                    double unitCost = Double.parseDouble(parts[5]);
                    double totalCost = Double.parseDouble(parts[6]);
                    LocalDate date = LocalDate.parse(parts[7]);
                    LocalTime time = parts.length >= 9 ? LocalTime.parse(parts[8]) : LocalTime.now();
                    String status = parts.length >= 10 ? unescape(parts[9]) : "";
                    String category = parts.length >= 11 ? unescape(parts[10]) : "";
                    int totalStockIn = parts.length > 11 ? Integer.parseInt(parts[11]) : 0;
                    int totalStockOut = parts.length > 12 ? Integer.parseInt(parts[12]) : 0;

                    Product p = new Product(barcode, name, unit, quantity, unitCost, date, time);

                    p.setUsername(username); // Set username
                    p.setCategory(category);
                    p.setTotalStockIn(totalStockIn);
                    p.setTotalStockOut(totalStockOut);
                    if (!status.isEmpty()) {
                        p.statusProperty().set(status);
                    }

                    products.add(p);
                } catch (Exception e) {
                    System.err.println("⚠️ Error parsing product at line " + lineNumber + ": " + line);
                    e.printStackTrace();
                }
            }

            System.out.println("✅ Loaded " + products.size() + " products from CSV.");
        } catch (IOException e) {
            System.err.println("❌ Error reading products from CSV:");
            e.printStackTrace();
        }

        return products;
    }

    public static void saveArchivedToCSV(List<Product> archivedList) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("data/archived_products.csv"))) {
            for (Product p : archivedList) {
                writer.println(String.join(",",
                        escape(p.getBarcode()),
                        escape(p.getName()),
                        escape(p.getUsername()),
                        escape(p.getUnit()),
                        String.valueOf(p.getQuantity()),
                        String.valueOf(p.getUnitCost()),
                        String.valueOf(p.getTotalCost()),
                        p.getDate().toString(),
                        p.getTime().toString(),
                        escape(p.getStatus()),
                        escape(p.getCategory()),
                        String.valueOf(p.getTotalStockIn()),
                        String.valueOf(p.getTotalStockOut())
                ));

            }
            System.out.println("✅ Saved archived products to CSV.");
        } catch (IOException e) {
            System.err.println("❌ Error saving archived products:");
            e.printStackTrace();
        }
    }

    public static List<Product> loadArchivedFromCSV() {
        List<Product> archived = new ArrayList<>();
        File file = new File("data/archived_products.csv");

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("🆕 Created empty archived_products.csv");
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to create archived CSV file:");
                e.printStackTrace();
            }
            return archived;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] parts = parseCSVLine(line);

                try {
                    String barcode = unescape(parts[0]);
                    String name = unescape(parts[1]);
                    String username = unescape(parts[2]); // Read username (3rd column)
                    String unit = unescape(parts[3]);
                    int quantity = Integer.parseInt(parts[4]);
                    double unitCost = Double.parseDouble(parts[5]);
                    double totalCost = Double.parseDouble(parts[6]);
                    LocalDate date = LocalDate.parse(parts[7]);
                    LocalTime time = LocalTime.parse(parts[8]);
                    String status = parts.length > 9 ? unescape(parts[9]) : "Archived";
                    String category = parts.length > 10 ? unescape(parts[10]) : "";
                    int totalStockIn = parts.length > 11 ? Integer.parseInt(parts[11]) : 0;
                    int totalStockOut = parts.length > 12 ? Integer.parseInt(parts[12]) : 0;

                    Product product = new Product(barcode, name, unit, quantity, unitCost, date, time);
                    product.setUsername(username); // Set username
                    product.setCategory(category);
                    product.setTotalStockIn(totalStockIn); // ✅ ADD THIS
                    product.setTotalStockOut(totalStockOut); // ✅ AND THIS
                    product.statusProperty().set(status);
                    archived.add(product);
                } catch (Exception e) {
                    System.err.println("⚠️ Error parsing archived product at line " + lineNumber + ": " + line);
                    e.printStackTrace();
                }
            }

            System.out.println("✅ Loaded " + archived.size() + " archived products from CSV.");
        } catch (IOException e) {
            System.err.println("❌ Error reading archived products:");
            e.printStackTrace();
        }

        return archived;
    }

    private static String escape(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    private static String unescape(String input) {
        if (input == null) return "";
        if (input.startsWith("\"") && input.endsWith("\"")) {
            return input.substring(1, input.length() - 1).replace("\"\"", "\"");
        }
        return input;
    }

    private static String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean insideQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                insideQuote = !insideQuote;
            } else if (c == ',' && !insideQuote) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }
}
