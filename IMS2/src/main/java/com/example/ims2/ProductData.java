package com.example.ims2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductData {
    private static final ObservableList<Product> productList = FXCollections.observableArrayList();

    public static void addProduct(Product product) {

        if (!isBarcodeDuplicate(product.getBarcode())) {
            productList.add(product); // Local list
            MainController.productList.add(product); // Shared list
        }
    }

    public static ObservableList<Product> getProductList() {
        return productList;
    }

    public static boolean isBarcodeDuplicate(String barcode) {
        String trimmed = barcode.trim();
        return productList.stream()
                .anyMatch(product -> product.getBarcode().trim().equalsIgnoreCase(trimmed));
    }

    public static boolean isNameDuplicate(String name) {
        String trimmed = name.trim();
        return productList.stream()
                .anyMatch(product -> product.getName().trim().equalsIgnoreCase(trimmed));
    }
}
