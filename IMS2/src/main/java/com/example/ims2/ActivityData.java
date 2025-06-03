package com.example.ims2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;

public class ActivityData {
    private static final ObservableList<Activity> activityList = FXCollections.observableArrayList();

    public static ObservableList<Activity> getActivityList() {
        return activityList;
    }

    public static void logActivity(Product product, String remarks, String status) {
        if (product == null) return;

        String category = product.getCategory();
        if (category == null || category.isBlank()) {
            category = "Uncategorized";
        }

        Activity activity = new Activity(
                LocalDateTime.now(),
                product.getBarcode(),
                product.getName(),
                product.getUnit(),
                product.getCategory(),
                product.getQuantity(),
                product.getUnitCost(),
                product.getQuantity() * product.getUnitCost(),
                status,
                remarks,
                Session.getLoggedInUser()
        );
        activityList.add(0, activity);
    }

    public static void logActivity(Product product, String remarks, String status, LocalDateTime time) {
        if (product == null || status == null || status.isEmpty()) return;

        String category = product.getCategory();
        if (category == null || category.isBlank()) {
            category = "Uncategorized";
        }

        Activity activity = new Activity(
                time,
                product.getBarcode(),
                product.getName(),
                product.getUnit(),
                product.getCategory(),
                product.getQuantity(),
                product.getUnitCost(),
                product.getQuantity() * product.getUnitCost(),
                status,
                remarks,
                Session.getLoggedInUser()
        );
        activityList.add(0, activity);
    }

    public static void clearAll() {
        activityList.clear();
    }
}
