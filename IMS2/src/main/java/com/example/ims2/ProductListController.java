package com.example.ims2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.*;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.event.ActionEvent;


public class ProductListController implements Initializable {


    private Button btnAddProduct, btnStockIn, btnStockOut, btnEdit, btnReturn, btnArchive, btnUndo;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private CheckBox selectAll;
    @FXML
    private TableColumn<Product, Boolean> selectColumn;
    @FXML
    private TableColumn<Product, String> barcodeColumn, nameColumn, unitColumn, statusColumn, usercolumn, categoryColumn;
    @FXML
    private TableColumn<Product, Integer> quantityColumn;
    @FXML
    private TableColumn<Product, Double> unitCostColumn, totalCostColumn;
    @FXML
    private TableColumn<Product, LocalDate> dateColumn;
    @FXML
    private TableColumn<Product, LocalTime> timeColumn;
    @FXML
    private Label time;
    @FXML
    private String username;
    @FXML
    private VBox hide1;
    @FXML
    private HBox hide;

    private MainController mainController;

    private String initialFilter = "ALL";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startClock();
        setupTable();
        username = Session.getLoggedInUser();
        ObservableList<Product> products = ProductStorageManager.getInstance().getProductList();
        FXCollections.sort(products, (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        productTable.setItems(products);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> onSearch());
        if (initialFilter != null) applyFilter(initialFilter);

        selectAll.selectedProperty().addListener((obs, oldVal, newVal) -> {
            for (Product product : productTable.getItems()) {
                product.setSelected(newVal);
            }
        });

        if ("Employee".equalsIgnoreCase(Session.getLoggedInRole())) {
            hideForViewer(hide1);
            hideForViewer(hide);
            hideColumnForViewer(selectColumn);
        }

    }

    private void hideColumnForViewer(TableColumn<?, ?> column) {
        if (column != null) {
            column.setVisible(false);
        }
    }

    private void hideForViewer(Node node) {
        if (node != null) {
            node.setVisible(false);
            node.setManaged(false);
        }
    }

    public void setInitialFilter(String filter) {
        this.initialFilter = filter;
    }


    private void applyFilter(String filter) {
        ObservableList<Product> allProducts = ProductStorageManager.getInstance().getProductList();

        if (filter == null || filter.equalsIgnoreCase("ALL")) {
            productTable.setItems(allProducts);
        } else {
            ObservableList<Product> filtered = allProducts.filtered(p -> {
                if (filter.equalsIgnoreCase("Low Stock")) {
                    return "Low Stock".equalsIgnoreCase(p.getStatus());
                } else if (filter.equalsIgnoreCase("Out of Stock")) {
                    return "Out of Stock".equalsIgnoreCase(p.getStatus());
                } else if (filter.equalsIgnoreCase("In Stock")) {
                    return "In Stock".equalsIgnoreCase(p.getStatus());
                }
                return true;
            });
            productTable.setItems(filtered);
        }
        productTable.refresh();
    }


    private void setupTable() {
        barcodeColumn.setCellValueFactory(cellData -> cellData.getValue().barcodeProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        usercolumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        unitColumn.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        unitCostColumn.setCellValueFactory(cellData -> cellData.getValue().unitCostProperty().asObject());
        totalCostColumn.setCellValueFactory(cellData -> cellData.getValue().totalCostProperty().asObject());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());




        selectColumn.setCellValueFactory(cellData ->
                cellData.getValue().selectedProperty()
        );

        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);
        productTable.setEditable(true);

        centerColumn(barcodeColumn);
        centerColumn(nameColumn);
        centerColumn(usercolumn);
        centerColumn(unitColumn);
        centerColumn(categoryColumn);
        centerColumn(quantityColumn);
        centerColumn(dateColumn);
        selectColumn.setStyle("-fx-alignment: CENTER;");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE - MMMM dd, yyyy");
        dateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
                setStyle("-fx-alignment: CENTER;");
            }
        });

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        timeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(timeFormatter));
                setStyle("-fx-alignment: CENTER;");
            }
        });

        unitCostColumn.setCellFactory(col -> currencyCell());
        totalCostColumn.setCellFactory(col -> currencyCell());

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
                    switch (item) {
                        case "Out of Stock" ->
                                setStyle(getStyle() + " -fx-background-color: #E76F51; -fx-text-fill: white;");
                        case "Low Stock" ->
                                setStyle(getStyle() + " -fx-background-color: #E9C46A; -fx-text-fill: white;");
                        case "In Stock" ->
                                setStyle(getStyle() + " -fx-background-color: #2A9D8F; -fx-text-fill: white;");
                    }
                }
            }
        });
    }

    private TableCell<Product, Double> currencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
                    formatter.setMinimumFractionDigits(2);
                    formatter.setMaximumFractionDigits(2);
                    setText("₱" + formatter.format(item));
                }
                setStyle("-fx-alignment: CENTER;");
            }
        };
    }


    private <T> void centerColumn(TableColumn<Product, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }


    @FXML
    private void onAddProduct() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter Product Details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        TextField unitField = new TextField();
        TextField categoryField = new TextField();
        TextField unitCostField = new TextField();
        TextField quantityField = new TextField();

        grid.add(new Label("Product Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Unit of Measure:"), 0, 1);
        grid.add(unitField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryField, 1, 2);
        grid.add(new Label("Unit Cost:"), 0, 3);
        grid.add(unitCostField, 1, 3);
        grid.add(new Label("Quantity:"), 0, 4);
        grid.add(quantityField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType resetButtonType = new ButtonType("Reset", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, resetButtonType, cancelButtonType);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        Button resetButton = (Button) dialog.getDialogPane().lookupButton(resetButtonType);

        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            // Basic validation
            String name = nameField.getText().trim();
            String unit = unitField.getText().trim();
            String category = categoryField.getText().trim();
            String unitCostText = unitCostField.getText().trim();
            String quantityText = quantityField.getText().trim();

            if (name.isEmpty() || unit.isEmpty() || category.isEmpty() ||
                    unitCostText.isEmpty() || quantityText.isEmpty()) {
                showAlert("Please fill in all fields.", Alert.AlertType.WARNING);
                event.consume();
                return;
            }

            double unitCost;
            int quantity;
            try {
                unitCost = Double.parseDouble(unitCostText);
                quantity = Integer.parseInt(quantityText);
                if (unitCost < 0 || quantity < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert("Please enter valid positive numbers.", Alert.AlertType.ERROR);
                event.consume();
                return;
            }

            ObservableList<Product> productList = ProductStorageManager.getInstance().getProductList();

            // Generate barcode using category prefix
            String prefix = category.substring(0, Math.min(3, category.length())).toUpperCase();
            long count = productList.stream().filter(p -> p.getBarcode().startsWith(prefix + "-")).count();
            String barcode = prefix + "-" + String.format("%03d", count + 1);

            // Check for duplicates
            boolean nameExists = productList.stream()
                    .anyMatch(p -> p.getName().equalsIgnoreCase(name));
            if (nameExists) {
                showAlert("A product with this name already exists.", Alert.AlertType.ERROR);
                event.consume();
                return;
            }

            // Create and add the new product
            String username = Session.getLoggedInUser();
            Product newProduct = new Product(barcode, name, unit, quantity, unitCost, username, category);
            newProduct.updateStatus();
            productList.add(newProduct);

            // Log activity
            MainController.getInstance().logActivity(null, newProduct, "Product Added", LocalDateTime.now(), username);

            // Refresh UI
            MainController.getInstance().refreshActivityLog();
            MainController.getInstance().updateProductCounts();
            MainController.getInstance().refreshComboBox();
            MainController.getInstance().refreshProductTable();

            // Sort and re-apply filter
            FXCollections. sort(productList, Comparator.comparing(p -> p.getName().toLowerCase()));
            productTable.setItems(productList);
            applyFilter(initialFilter);

            // Save data
            ProductStorage.saveToCSV(productList);

            // Refresh reports view if active
            Reports.refreshReportsView();
            Reports reports = Reports.getInstance();
            if (reports != null) {
                reports.updateDateComboBoxesToCurrent();
                reports.refreshCategoryComboBox();
                reports.refreshTable(); // Will reflect the new product
            }

            showAlert("Product added successfully.", Alert.AlertType.INFORMATION);
            event.consume(); // Prevent dialog from closing
        });

        resetButton.addEventFilter(ActionEvent.ACTION, event -> {
            nameField.clear();
            unitField.clear();
            categoryField.clear();
            unitCostField.clear();
            quantityField.clear();
            event.consume();
        });

        dialog.showAndWait();
    }




    @FXML
    private void onEditProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a product to edit.", Alert.AlertType.WARNING);
            return;
        }

        Product oldProduct = new Product(selected);
        Product edited = showEditDialog(selected);
        if (edited != null) {
            edited.setUsername(username);
            edited.updateStatus();
            productTable.refresh();
            ProductStorage.saveToCSV(ProductStorageManager.getInstance().getProductList());

            MainController.getInstance().logActivity(oldProduct, edited, "Edited", LocalDateTime.now(), Session.getLoggedInUser());
            MainController.getInstance().refreshActivityLog();

            showAlert("Edit successful.", Alert.AlertType.INFORMATION);
        }
    }

    private String formatCurrency(double amount) {
        return String.format("%,.2f", amount);
    }

    private Product showEditDialog(Product product) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Edit Product Details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(product.getName());
        TextField unitField = new TextField(product.getUnit());
        TextField unitCostField = new TextField(String.valueOf(product.getUnitCost()));
        TextField categoryField = new TextField(product.getCategory());

        grid.add(new Label("New Product Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("New Unit:"), 0, 1);
        grid.add(unitField, 1, 1);
        grid.add(new Label("New Unit Cost:"), 0, 2);
        grid.add(unitCostField, 1, 2);
        grid.add(new Label("New Category:"), 0, 3);
        grid.add(categoryField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                product.setName(nameField.getText().trim());
                product.setUnit(unitField.getText().trim());
                product.setUnitCost(Double.parseDouble(unitCostField.getText().trim()));
                product.setCategory(categoryField.getText().trim());
                return product;
            } catch (NumberFormatException e) {
                showAlert("Invalid unit cost value.", Alert.AlertType.ERROR);
            }
        }

        return null;
    }

    @FXML
    private void onStockInProduct() {
        List<Product> selectedProducts = productTable.getItems()
                .stream()
                .filter(Product::isSelected)
                .collect(Collectors.toList());

        if (selectedProducts.isEmpty()) {
            showAlert("Please select product(s) to stock in.", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Stock In Product");
        dialog.setHeaderText("Enter Quantity to Stock In:");
        dialog.setContentText("Quantity:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) throw new NumberFormatException();

                for (Product selected : selectedProducts) {
                    Product old = new Product(selected);

                    selected.stockIn(qty); // ✅ uses custom method
                    selected.addToTotalStockIn(qty); // NEW: track total stock in
                    selected.setUsername(username);
                    selected.updateStatus();

                    MainController.getInstance().logActivity(
                            old, selected,
                            "Stocked In (+" + qty + ")",
                            LocalDateTime.now(),
                            Session.getLoggedInUser()
                    );
                }

                productTable.refresh();
                Reports.refreshReportsView();
                Reports reports = Reports.getInstance();
                if (reports != null) {
                    reports.updateDateComboBoxesToCurrent();
                    reports.refreshCategoryComboBox();
                    reports.refreshTable();
                }
                ProductStorage.saveToCSV(ProductStorageManager.getInstance().getProductList());
                MainController.getInstance().updateProductCounts();
                MainController.getInstance().refreshActivityLog();

                showAlert("Stocked In Successfully.", Alert.AlertType.INFORMATION);

                productTable.setItems(ProductStorageManager.getInstance().getProductList());
                productTable.refresh(); // Make sure this is called after the table has been bound

                for (Product p : productTable.getItems()) {
                    p.setSelected(false);
                }
                selectAll.setSelected(false);

            } catch (NumberFormatException e) {
                showAlert("Invalid number entered.", Alert.AlertType.ERROR);
            }
        });

    }


    @FXML
    private void onStockOutProduct() {
        List<Product> selectedProducts = productTable.getItems()
                .stream()
                .filter(Product::isSelected)
                .collect(Collectors.toList());

        if (selectedProducts.isEmpty()) {
            showAlert("Please select product(s) to stock out.", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Stock Out Product");
        dialog.setHeaderText("Enter Quantity to Stock Out:");
        dialog.setContentText("Quantity:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) throw new NumberFormatException();

                for (Product selected : selectedProducts) {
                    Product old = new Product(selected); // snapshot before change

                    boolean success = selected.stockOut(qty); // this already subtracts
                    if (!success) {
                        showAlert("Not enough stock for product: " + selected.getName(), Alert.AlertType.WARNING);
                        continue;
                    }

                    selected.addToTotalStockOut(qty); // NEW: track total stock out
                    selected.setUsername(username);
                    selected.updateStatus();

                    MainController.getInstance().logActivity(
                            old, selected, "Stocked Out (-" + qty + ")", LocalDateTime.now(), Session.getLoggedInUser()
                    );
                }

                productTable.refresh();
                Reports.refreshReportsView();
                Reports reports = Reports.getInstance();
                if (reports != null) {
                    reports.updateDateComboBoxesToCurrent();
                    reports.refreshCategoryComboBox();
                    reports.refreshTable();
                }
                ProductStorage.saveToCSV(ProductStorageManager.getInstance().getProductList());
                MainController.getInstance().updateProductCounts();
                MainController.getInstance().refreshActivityLog();

                showAlert("Stocked Out Successfully.", Alert.AlertType.INFORMATION);

                productTable.setItems(ProductStorageManager.getInstance().getProductList());
                productTable.refresh(); // Make sure this is called after the table has been bound

                for (Product p : productTable.getItems()) {
                    p.setSelected(false);
                }
                selectAll.setSelected(false);

            } catch (NumberFormatException e) {
                showAlert("Invalid number entered.", Alert.AlertType.ERROR);
            }
        });
    }




    @FXML
    private void OnArchive() {
        List<Product> selectedProducts = productTable.getItems()
                .stream()
                .filter(Product::isSelected)
                .collect(Collectors.toList());

        if (selectedProducts.isEmpty()) {
            showAlert("Please select product(s) to archive.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Archive");
        confirm.setHeaderText("Archive Selected Products");
        confirm.setContentText("Are you sure you want to archive the selected products?");

        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                for (Product selectedProduct : selectedProducts) {
                    Product old = new Product(selectedProduct);

                    selectedProduct.setUsername(username); // ✅
                    selectedProduct.updateStatus();

                    // Archive the product
                    ProductStorageManager.getInstance().getProductList().remove(selectedProduct);
                    ProductStorageManager.getInstance().getArchivedList().add(selectedProduct);
                    productTable.getItems().remove(selectedProduct);

                    MainController.getInstance().logActivity(old, null, "Archived", LocalDateTime.now(), Session.getLoggedInUser());
                }

                productTable.refresh();
                Reports.refreshReportsView();
                Reports reports = Reports.getInstance();
                if (reports != null) {
                    reports.updateDateComboBoxesToCurrent();
                    reports.refreshCategoryComboBox();
                    reports.refreshTable();
                }
                ProductStorage.saveToCSV(ProductStorageManager.getInstance().getProductList());
                ProductStorage.saveArchivedToCSV(ProductStorageManager.getInstance().getArchivedList());
                MainController.getInstance().updateProductCounts();
                MainController.getInstance().refreshActivityLog(); // ✅

                showAlert("Archived Successfully!", Alert.AlertType.INFORMATION);

                ArchiveProdController controller = new ArchiveProdController();
                controller.refreshArchivedTable();

                for (Product p : productTable.getItems()) {
                    p.setSelected(false);
                }
                selectAll.setSelected(false);
            }
        });
    }


    @FXML
    private void onSearch() {
        String text = searchField.getText().toLowerCase().trim();
        ObservableList<Product> allProducts = ProductStorageManager.getInstance().getProductList();

        if (text.isEmpty()) {
            applyFilter(initialFilter);
            return;
        }

        ObservableList<Product> filtered = allProducts.stream()
                .filter(p -> safeLower(p.getName()).contains(text)
                        || safeLower(p.getBarcode()).contains(text)
                        || safeLower(p.getCategory()).contains(text))
                .collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);

        productTable.setItems(filtered);
    }

    private String safeLower(String input) {
        return input == null ? "" : input.toLowerCase();
    }


    @FXML
    private void OnCancel(ActionEvent event) {
        loadMainView(event);
    }

    @FXML
    private void OnReturn(ActionEvent event) {
        loadMainView(event);
    }

    private void loadMainView(ActionEvent event) {

        try {
            String fxmlPath = "main.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double width = screenBounds.getWidth();
            double height = screenBounds.getHeight();

            stage.setScene(new Scene(root, width, height));
            stage.setMaximized(true);

            Object controller = loader.getController();
            if (controller instanceof MainController) {
                ((MainController) controller).setMainController(this);
            }

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToArchive(ActionEvent event) {
        try {
            String fxmlPath = "/com/example/ims2/ArchivedProd.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();


            ArchiveProdController controller = loader.getController();
            controller.refreshArchivedTable();


            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();


            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double width = screenBounds.getWidth();
            double height = screenBounds.getHeight();

            stage.setScene(new Scene(root, width, height));
            stage.setMaximized(true);
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void OnUndo() {
        for (Product product : productTable.getItems()) {
            product.setSelected(false);
        }
        selectAll.setSelected(false);
    }

    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE - MMMM dd, yyyy\nhh:mm:ss a");


        time.setText(LocalDateTime.now().format(formatter));


        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            time.setText(LocalDateTime.now().format(formatter));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    public void refreshProductTable() {
        productTable.setItems(ProductStorageManager.getInstance().getProductList());
        productTable.refresh();
    }


    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Product Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

}
