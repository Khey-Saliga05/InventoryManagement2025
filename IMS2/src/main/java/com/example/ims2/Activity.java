package com.example.ims2;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.time.temporal.WeekFields;
import java.util.stream.Collectors;

public class Activity {
    private final LocalDateTime dateTime;
    private final StringProperty date;
    private final StringProperty barcode;
    private final StringProperty name;
    private final StringProperty unit;
    private final StringProperty category;
    private final IntegerProperty quantity;
    private final DoubleProperty unitCost;
    private final DoubleProperty totalCost;
    private final StringProperty actionType;
    private final StringProperty user;
    private final StringProperty details;
    private final StringProperty remarks;
    private final String filterKey;


    // Reporting and UI Summary Properties
    private final StringProperty time = new SimpleStringProperty();
    private final StringProperty productName = new SimpleStringProperty();
    private final IntegerProperty totalStockIn = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStockOut = new SimpleIntegerProperty(0);
    private final IntegerProperty stockOnHand = new SimpleIntegerProperty(0);

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE - MMMM dd, yyyy");

    public Activity(LocalDateTime dateTime, String barcode, String name, String unit, String category,
                    int quantity, double unitCost, double totalCost,
                    String actionType, String details, String remarks, String user) {
        this.dateTime = dateTime;
        this.date = new SimpleStringProperty(dateTime.format(formatter));
        this.barcode = new SimpleStringProperty(barcode);
        this.name = new SimpleStringProperty(name);
        this.unit = new SimpleStringProperty(unit);
        this.category = new SimpleStringProperty(category != null ? category : "");
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitCost = new SimpleDoubleProperty(unitCost);
        this.totalCost = new SimpleDoubleProperty(totalCost);
        this.actionType = new SimpleStringProperty(actionType != null ? actionType : "");
        this.details = new SimpleStringProperty(details != null ? details : "");
        this.remarks = new SimpleStringProperty(remarks != null ? remarks : "");
        this.user = new SimpleStringProperty(user != null ? user : "Unknown");
        this.filterKey = computeFilterKey(actionType);

        // Set formatted time and bind name to productName
        this.time.set(dateTime.format(timeFormatter));
        this.productName.bind(this.name);
    }

    public Activity(LocalDateTime dateTime, String barcode, String name, String unit, String category,
                    int quantity, double unitCost, double totalCost,
                    String actionType, String remarks, String user) {
        this(dateTime, barcode, name, unit, category, quantity, unitCost, totalCost,
                actionType, "", remarks, user);
    }

    public Activity(LocalDateTime dateTime, String barcode, String name, String unit, String category,
                    int quantity, double unitCost, double totalCost,
                    String actionType, String user) {
        this(dateTime, barcode, name, unit, category, quantity, unitCost, totalCost,
                actionType, "", "", user);
    }

    private String computeFilterKey(String actionType) {
        if (actionType == null) return "Other";
        String lower = actionType.toLowerCase();
        if (lower.contains("added")) return "Products Added";
        if (lower.contains("stocked in")) return "Stocked In";
        if (lower.contains("stocked out")) return "Stocked Out";
        if (lower.contains("edited")) return "Edited";
        if (lower.contains("deleted")) return "Deleted";
        if (lower.contains("archived")) return "Archived";
        if (lower.contains("restored")) return "Restored";
        return "Other";
    }

    public String getFilterKey() { return filterKey; }

    public String getDisplayActivity() {
        String action = getActionType();
        return switch (action) {
            case "Stocked In" -> "Stocked In (+" + getQuantity() + ")";
            case "Stocked Out" -> "Stocked Out (-" + getQuantity() + ")";
            case "Product Added" -> "Product Added";
            case "Edited" -> details.get().isEmpty() ? "Edited" : "Edited (" + details.get() + ")";
            case "Deleted" -> "Deleted";
            case "Product Archived" -> "Archived";
            case "Restored" -> "Restored";
            default -> action.isEmpty() ? "No Activity Yet" : action;
        };
    }

    public String getFormattedTime() { return dateTime.format(timeFormatter); }

    public LocalDateTime getDateTime() { return dateTime; }

    public String getDate() { return date.get(); }

    public String getFormattedDate() { return date.get(); }

    public String getBarcode() { return barcode.get(); }

    public String getName() { return name.get(); }

    public String getProductName() {return productName.get();}

    public String getUnit() { return unit.get(); }

    public String getCategory() { return category.get(); }

    public int getQuantity() { return quantity.get(); }

    public double getUnitCost() { return unitCost.get(); }

    public double getTotalCost() { return totalCost.get(); }

    public String getActionType() { return actionType.get(); }

    public String getUser() { return user.get(); }

    public String getDetails() { return details.get(); }

    public String getRemarks() { return remarks.get(); }

    public String getStatus() { return actionType.get(); }

    public String getMonth() {
        return String.format("%02d", dateTime.getMonthValue());
    }

    public String getWeek() {
        return String.valueOf(dateTime.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()));
    }

    public String getDay() {
        return String.format("%02d", dateTime.getDayOfMonth());
    }

    public StringProperty dateProperty() { return date; }

    public StringProperty barcodeProperty() { return barcode; }

    public StringProperty nameProperty() { return name; }

    public StringProperty unitProperty() { return unit; }

    public StringProperty categoryProperty() { return category; }

    public IntegerProperty quantityProperty() { return quantity; }

    public DoubleProperty unitCostProperty() { return unitCost; }

    public DoubleProperty totalCostProperty() { return totalCost; }

    public StringProperty statusProperty() { return actionType; }

    public StringProperty userProperty() { return user; }

    public StringProperty detailsProperty() { return details; }

    public StringProperty remarksProperty() { return remarks; }

    public StringProperty timeProperty() { return time; }

    public StringProperty productNameProperty() { return productName; }

    public IntegerProperty totalStockInProperty() { return totalStockIn; }

    public void setTotalStockIn(int value) { this.totalStockIn.set(value); }

    public IntegerProperty totalStockOutProperty() { return totalStockOut; }

    public void setTotalStockOut(int value) { this.totalStockOut.set(value); }

    public IntegerProperty stockOnHandProperty() { return stockOnHand; }

    public void setStockOnHand(int value) { this.stockOnHand.set(value); }

    public static boolean isValidActivity(Activity activity) {
        if (activity == null) return false;
        if (activity.getBarcode() == null || activity.getBarcode().isEmpty()) return false;
        if (activity.getName() == null || activity.getName().isEmpty()) return false;
        if (activity.getActionType() == null || activity.getActionType().isEmpty()) return false;
        if (activity.getQuantity() < 0 || activity.getUnitCost() < 0 || activity.getTotalCost() < 0) return false;
        if (activity.getActionType().equalsIgnoreCase("Deleted")) return false;
        return true;
    }

    public static void logActivityIfValid(Activity activity) {
        if (isValidActivity(activity)) {
            MainController.getInstance().logActivity(activity);
        } else {
            System.out.println("Skipping malformed activity entry: " + activity);
        }
    }

    public static List<Activity> filterByActionType(ObservableList<Activity> activities, String type) {
        return activities.stream()
                .filter(a -> a.getActionType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public static List<Activity> filterByCategory(ObservableList<Activity> activities, String category) {
        return activities.stream()
                .filter(a -> a.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public static List<Activity> filterByUser(ObservableList<Activity> activities, String username) {
        return activities.stream()
                .filter(a -> a.getUser().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public static List<Activity> filterByDateRange(ObservableList<Activity> activities, LocalDateTime start, LocalDateTime end) {
        return activities.stream()
                .filter(a -> !a.getDateTime().isBefore(start) && !a.getDateTime().isAfter(end))
                .collect(Collectors.toList());
    }

    public static int getTotalQuantity(ObservableList<Activity> activities) {
        return activities.stream().mapToInt(Activity::getQuantity).sum();
    }

    public static double getTotalCost(ObservableList<Activity> activities) {
        return activities.stream().mapToDouble(Activity::getTotalCost).sum();
    }

    public static int countByActionType(ObservableList<Activity> activities, String type) {
        return (int) activities.stream()
                .filter(a -> a.getActionType().equalsIgnoreCase(type))
                .count();
    }

    @Override
    public String toString() {
        return "[" + dateTime + "] " + actionType.get() + " - " + name.get() + " x" + quantity.get();
    }
}
