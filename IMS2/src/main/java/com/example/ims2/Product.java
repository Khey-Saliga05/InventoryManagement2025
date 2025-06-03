package com.example.ims2;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

import java.time.LocalDate;
import java.time.LocalTime;

public class Product {
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalTime> time = new SimpleObjectProperty<>(LocalTime.now());

    private final StringProperty barcode;
    private final StringProperty name;
    private final StringProperty unit;
    private final IntegerProperty quantity;
    private final DoubleProperty unitCost;
    private final DoubleProperty totalCost;
    private final StringProperty status = new SimpleStringProperty();
    private final BooleanProperty archived = new SimpleBooleanProperty(false);
    private final SimpleStringProperty category = new SimpleStringProperty("");
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty username = new SimpleStringProperty();
    private final IntegerProperty stockOnHand = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStockIn = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStockOut = new SimpleIntegerProperty(0);



    private Product(String barcode, String name, String unit, int quantity, double unitCost) {
        this.barcode = new SimpleStringProperty(barcode);
        this.name = new SimpleStringProperty(name);
        this.unit = new SimpleStringProperty(unit);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitCost = new SimpleDoubleProperty(unitCost);
        this.totalCost = new SimpleDoubleProperty(quantity * unitCost);
        this.archived.set(false);

        this.quantity.addListener((obs, oldVal, newVal) -> {
            updateTotalCost();
            updateStatus();
        });

        this.unitCost.addListener((obs, oldVal, newVal) -> updateTotalCost());

        updateStatus();
    }

    public Product(String barcode, String name, String unit, int quantity, double unitCost, LocalDate date, LocalTime time) {
        this(barcode, name, unit, quantity, unitCost);
        this.date.set(date);
        this.time.set(time);
    }

    public Product(Product other) {
        this.barcode = new SimpleStringProperty(other.getBarcode());
        this.name = new SimpleStringProperty(other.getName());
        this.unit = new SimpleStringProperty(other.getUnit());
        this.quantity = new SimpleIntegerProperty(other.getQuantity());
        this.unitCost = new SimpleDoubleProperty(other.getUnitCost());
        this.totalCost = new SimpleDoubleProperty(other.getTotalCost());
        this.date.set(other.getDate());
        this.time.set(other.getTime());
        this.status.set(other.getStatus());
        this.category.set(other.getCategory());
        this.archived.set(other.isArchived());
        this.username.set(other.getUsername());

        this.quantity.addListener((obs, oldVal, newVal) -> {
            updateTotalCost();
            updateStatus();
        });

        this.unitCost.addListener((obs, oldVal, newVal) -> updateTotalCost());
        updateStatus();
    }

    public Product(String barcode, String name, String unit, int quantity, double unitCost, String username, String category) {
        this(barcode, name, unit, quantity, unitCost);
        this.username.set(username);
        this.category.set(String.valueOf(category));
    }

    public Product(String username, StringProperty barcode, StringProperty name, StringProperty unit, IntegerProperty quantity, DoubleProperty unitCost, DoubleProperty totalCost) {
        this.barcode = barcode;
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.username.set(username);

        this.quantity.addListener((obs, oldVal, newVal) -> {
            updateTotalCost();
            updateStatus();
        });

        this.unitCost.addListener((obs, oldVal, newVal) -> updateTotalCost());

        updateStatus();
    }

    private void updateTotalCost() {
        this.totalCost.set(this.quantity.get() * this.unitCost.get());
    }

    public void updateStatus() {
        status.set(determineStatus(getQuantity()));
    }

    private String determineStatus(int quantity) {
        if (quantity == 0) return "Out of Stock";
        else if (quantity <= 5) return "Low Stock";
        else return "In Stock";
    }

    public StringProperty barcodeProperty() { return barcode; }
    public String getBarcode() { return barcode.get(); }
    public void setBarcode(String value) { barcode.set(value); }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }

    public StringProperty unitProperty() { return unit; }
    public String getUnit() { return unit.get(); }
    public void setUnit(String value) { unit.set(value); }

    public IntegerProperty quantityProperty() { return quantity; }
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int value) {
        quantity.set(value);
        updateStatus();
    }

    public DoubleProperty unitCostProperty() { return unitCost; }
    public double getUnitCost() { return unitCost.get(); }
    public void setUnitCost(double value) { unitCost.set(value); }

    public DoubleProperty totalCostProperty() { return totalCost; }
    public double getTotalCost() { return totalCost.get(); }

    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public LocalDate getDate() { return date.get(); }
    public void setDate(LocalDate value) { date.set(value); }

    public ObjectProperty<LocalTime> timeProperty() { return time; }
    public LocalTime getTime() { return time.get(); }
    public void setTime(LocalTime value) { time.set(value); }

    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }

    public double getPrice() {
        return this.unitCost.get();
    }

    public StringProperty categoryProperty() { return category; }
    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }

    public BooleanProperty archivedProperty() { return archived; }
    public boolean isArchived() { return archived.get(); }
    public void setArchived(boolean archived) { this.archived.set(archived); }

    public BooleanProperty selectedProperty() { return selected; }
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean selected) { this.selected.set(selected); }

    public StringProperty usernameProperty() { return username; }
    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }




    @Override
    public String toString() {
        return "Product{" +
                "barcode='" + getBarcode() + '\'' +
                ", name='" + getName() + '\'' +
                ", unit='" + getUnit() + '\'' +
                ", category='" + getCategory() + '\'' +
                ", quantity=" + getQuantity() +
                ", unitCost=" + getUnitCost() +
                ", totalCost=" + getTotalCost() +
                ", date=" + getDate() +
                ", time=" + getTime() +
                ", status='" + getStatus() + '\'' +
                ", totalStockIn=" + getTotalStockIn() +
                ", totalStockOut=" + getTotalStockOut() +
                ", username='" + getUsername() + '\'' +
                '}';
    }


    public String toCSV() {
        return String.join(",",
                escape(getBarcode()),
                escape(getName()),
                escape(getUnit()),
                escape(getCategory()),
                String.valueOf(getQuantity()),
                String.valueOf(getUnitCost()),
                String.valueOf(getTotalCost()),
                getDate().toString(),
                getTime().toString(),
                escape(getStatus()),
                String.valueOf(getTotalStockIn()),
                String.valueOf(getTotalStockOut())
        );
    }

    private String escape(String input) {
        if (input.contains(",") || input.contains("\"")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    public void stockIn(int amount) {
        setTotalStockIn(getTotalStockIn() + amount);
        setQuantity(getQuantity() + amount);
    }

    public boolean stockOut(int qty) {
        if (getQuantity() < qty) return false;
        setQuantity(getQuantity() - qty);
        setTotalStockOut(getTotalStockOut() + qty);
        return true;
    }

    public StringProperty productNameProperty() {
        return nameProperty();
    }

    public StringProperty userProperty() {
        return usernameProperty();
    }

    public String getProductName() {
        return productNameProperty().getName();
    }




    public int getTotalStockIn() {
        return totalStockIn.get();
    }

    public void setTotalStockIn(int value) {
        totalStockIn.set(value);
    }

    public IntegerProperty totalStockInProperty() {
        return totalStockIn;
    }

    public int getTotalStockOut() {
        return totalStockOut.get();
    }

    public void setTotalStockOut(int value) {
        totalStockOut.set(value);
    }

    public IntegerProperty totalStockOutProperty() {
        return totalStockOut;
    }

    public void addToTotalStockIn(int qty) {
        totalStockIn.set(totalStockIn.get() + qty);
    }

    public void addToTotalStockOut(int qty) {
        totalStockOut.set(totalStockOut.get() + qty);
    }

}
