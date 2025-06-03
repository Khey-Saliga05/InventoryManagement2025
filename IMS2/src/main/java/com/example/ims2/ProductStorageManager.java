package com.example.ims2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.ims2.ProductStorage.loadFromCSV;

public class ProductStorageManager {

    private static final ProductStorageManager instance = new ProductStorageManager();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Product> archivedList = FXCollections.observableArrayList();



    private ProductStorageManager() {
        load();
    }

    public static ProductStorageManager getInstance() {
        return instance;
    }

    public ObservableList<Product> getProductList() {
        return productList;
    }

    public ObservableList<Product> getArchivedList() {
        return archivedList;
    }


    public void setProductList(List<Product> products) {
        this.productList.setAll(products); // replaces contents without breaking bindings
    }



    public void addProduct(Product product) {
        if (isBarcodeDuplicate(product.getBarcode())) {
            System.out.println("Duplicate barcode detected: " + product.getBarcode());
            return;
        }
        if (isNameDuplicate(product.getName())) {
            System.out.println("Duplicate product name detected: " + product.getName());
            return;
        }
        productList.add(product);
        ActivityData.logActivity(product, "", "Product Added");
        save();
    }

    public void removeProduct(Product product) {
        productList.remove(product);
        ActivityData.logActivity(product, "", "Deleted");
        save();
    }

    public void updateProduct(Product oldProduct, Product newProduct) {
        int index = productList.indexOf(oldProduct);
        if (index >= 0) {
            productList.set(index, newProduct);
            String details = generateEditDetails(oldProduct, newProduct);
            ActivityData.logActivity(newProduct, details, "Edited");
            save();
        }
    }

    public void stockIn(Product product, int amount) {
        if (amount > 0) {
            product.setQuantity(product.getQuantity() + amount);
            ActivityData.logActivity(product, "Amount: " + amount, "Stocked In");
            save();
        }
    }

    public void stockOut(Product product, int amount) {
        if (amount > 0 && product.getQuantity() >= amount) {
            product.setQuantity(product.getQuantity() - amount);
            ActivityData.logActivity(product, "Amount: " + amount, "Stocked Out");
            save();
        }
    }

    public boolean isBarcodeDuplicate(String barcode) {
        return productList.stream().anyMatch(p -> p.getBarcode().equalsIgnoreCase(barcode));
    }

    public boolean isNameDuplicate(String name) {
        return productList.stream().anyMatch(p -> p.getName().equalsIgnoreCase(name));
    }

    public void save() {
        ProductStorage.saveToCSV(productList);
        ProductStorage.saveArchivedToCSV(archivedList);
    }

    private void load() {
        List<Product> loadedProducts = loadFromCSV();
        productList.setAll(loadedProducts);

        List<Product> loadedArchived = ProductStorage.loadArchivedFromCSV(); // Make sure this method exists
        archivedList.setAll(ProductStorage.loadArchivedFromCSV());
        archivedList.setAll(loadedArchived);
    }

    private String generateEditDetails(Product oldP, Product newP) {
        StringBuilder sb = new StringBuilder();

        if (!oldP.getName().equals(newP.getName())) sb.append("Name changed, ");
        if (!oldP.getUnit().equals(newP.getUnit())) sb.append("Unit changed, ");
        if (oldP.getUnitCost() != newP.getUnitCost()) sb.append("Cost changed, ");
        if (oldP.getQuantity() != newP.getQuantity()) sb.append("Quantity changed, ");

        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }


    public ObservableList<Product> getStockedInProducts() {
        ObservableList<Product> stockedInProducts = FXCollections.observableArrayList();
        for (Product product : productList) {
            if (product.getQuantity() > 0) {
                stockedInProducts.add(product);
            }
        }
        return stockedInProducts;
    }

    public ObservableList<Product> getStockedOutProducts() {
        ObservableList<Product> stockedOutProducts = FXCollections.observableArrayList();
        for (Product product : productList) {
            if (product.getQuantity() == 0) {
                stockedOutProducts.add(product);
            }
        }
        return stockedOutProducts;
    }

    public ObservableList<Product> getLowStockProducts() {
        ObservableList<Product> lowStockProducts = FXCollections.observableArrayList();
        for (Product product : productList) {
            if (product.getQuantity() > 0 && product.getQuantity() <= 10) {
                lowStockProducts.add(product);
            }
        }
        return lowStockProducts;
    }

    public List<String> getAllCategories() {
        return productList.stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

}
