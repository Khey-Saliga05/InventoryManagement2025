package com.example.ims2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class ArchiveProdController {

    @FXML
    private Button btnRestore, btnPermanentDelete, ButtonCancel, btnUndo, ButtonBackToMain;
    @FXML
    private TableView<Product> archivedTable;
    @FXML
    private TextField searchField;
    @FXML
    private CheckBox selectAll;
    @FXML
    private TableColumn<Product, Boolean> selectColumn;
    @FXML
    private TableColumn<Product, String> barcodeColumn, nameColumn, unitColumn, categoryColumn, statusColumn, usercolumn;
    @FXML
    private TableColumn<Product, Integer> quantityColumn;
    @FXML
    private TableColumn<Product, Double> unitCostColumn, totalCostColumn;
    @FXML
    private TableColumn<Product, LocalDate> dateColumn;
    @FXML
    private TableColumn<Product, LocalTime> timeColumn;
    @FXML
    private VBox hide;
    @FXML
    private HBox hide1;

    @FXML
    private Label time;
    private String initialFilter = "ALL";

    @FXML
    public void initialize() {
        setupTable();
        refreshArchivedTable();

        startClock();
        ObservableList<Product> archivedProducts = ProductStorageManager.getInstance().getArchivedList();
        FXCollections.sort(archivedProducts, (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        archivedTable.setItems(archivedProducts);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> onSearch());
        if (initialFilter != null) applyFilter(initialFilter);

        selectAll.selectedProperty().addListener((obs, oldVal, newVal) -> {
            for (Product product : archivedTable.getItems()) {
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
            archivedTable.setItems(allProducts);
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
            archivedTable.setItems(filtered);
        }
        archivedTable.refresh();
    }

    private void setupTable() {
        barcodeColumn.setCellValueFactory(cellData -> cellData.getValue().barcodeProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        usercolumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Admin"));
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
        archivedTable.setEditable(true);

        centerColumn(barcodeColumn);
        centerColumn(nameColumn);
        centerColumn(usercolumn);
        centerColumn(unitColumn);
        centerColumn(quantityColumn);
        centerColumn(dateColumn);
        centerColumn(quantityColumn);
        selectColumn.setStyle("-fx-alignment: CENTER;");

        usercolumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-alignment: CENTER;");
                setText(empty || item == null ? null : item);
            }
        });

        categoryColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-alignment: CENTER;");
            }
        });

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
    private void onRestoreProduct(ActionEvent event) {
        ObservableList<Product> selectedProducts = archivedTable.getItems().filtered(Product::isSelected);

        if (selectedProducts.isEmpty()) {
            showAlert("Please select at least one product to restore.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Restore");
        confirm.setHeaderText("Restore Selected Products");
        confirm.setContentText("Are you sure you want to restore the selected products to active inventory?");

        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                for (Product selectedProduct : selectedProducts) {

                    Product oldProduct = new Product(selectedProduct);

                    ProductStorageManager.getInstance().getArchivedList().remove(selectedProduct);
                    ProductStorageManager.getInstance().getProductList().add(selectedProduct);

                    MainController.getInstance().logActivity(oldProduct, selectedProduct, "Restored", LocalDateTime.now(), Session.getLoggedInUser());
                }

                refreshArchivedTable();
                MainController.getInstance().updateProductCounts();
                MainController.getInstance().refreshActivityLog();
                ProductStorage.saveToCSV(ProductStorageManager.getInstance().getProductList());
                ProductStorage.saveArchivedToCSV(ProductStorageManager.getInstance().getArchivedList());

                showAlert("Selected products restored successfully!", Alert.AlertType.INFORMATION);
            }
        });
    }


    @FXML
    private void onPermanentDelete(ActionEvent event) {
        ObservableList<Product> selectedProducts = archivedTable.getItems().filtered(Product::isSelected);

        if (selectedProducts.isEmpty()) {
            showAlert("Please select at least one product to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Permanent Delete");
        confirm.setHeaderText("Delete Selected Products");
        confirm.setContentText("Are you sure you want to permanently delete the selected products? This action is Irreversible!");

        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                for (Product product : selectedProducts) {
                    Product oldProduct = new Product(product); // for logging
                    ProductStorageManager.getInstance().getArchivedList().remove(product);
                    MainController.getInstance().logActivity(oldProduct, product, "Deleted", LocalDateTime.now(), Session.getLoggedInUser());
                }

                archivedTable.getItems().removeAll(selectedProducts);
                archivedTable.refresh();
                ProductStorage.saveArchivedToCSV(ProductStorageManager.getInstance().getArchivedList());
                MainController.getInstance().updateProductCounts();
                MainController.getInstance().refreshActivityLog();
                showAlert("Selected products permanently deleted.", Alert.AlertType.INFORMATION);
            }
        });
    }


    @FXML
    private void onUndo(){
        for (Product product : archivedTable.getItems()) {
            product.setSelected(false);
        }
        selectAll.setSelected(false);
    }

    @FXML
    private void onCancel(ActionEvent event) {
        loadMainView(event);
    }

    @FXML
    private void onReturn(ActionEvent event) {
        loadMainView(event);
    }

    private void loadMainView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ims2/Product-list.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            stage.setScene(new Scene(root, screenBounds.getWidth(), screenBounds.getHeight()));
            stage.setMaximized(true);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackToMain(ActionEvent event){
        loadMainMenu(event);
    }

    private void loadMainMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ims2/main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            stage.setScene(new Scene(root, screenBounds.getWidth(), screenBounds.getHeight()));
            stage.setMaximized(true);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Product Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }


    public void setArchivedList(List<Product> archivedProducts) {
        if (archivedTable != null) {
            archivedTable.setItems(FXCollections.observableArrayList(archivedProducts));
        } else {
            System.err.println("archivedTable is null!");
        }
    }

    public void refreshArchivedTable() {
        ObservableList<Product> archivedProducts = FXCollections.observableArrayList(
                ProductStorageManager.getInstance().getArchivedList()
        );
        archivedTable.setItems(archivedProducts);
        archivedTable.refresh();
    }

    @FXML
    private void onSearch() {
        String text = searchField.getText().toLowerCase().trim();
        ObservableList<Product> archivedProducts = ProductStorageManager.getInstance().getArchivedList();

        if (text.isEmpty()) {
            refreshArchivedTable();
            return;
        }

        ObservableList<Product> filtered = archivedProducts.stream()
                .filter(p -> safeLower(p.getName()).contains(text)
                        || safeLower(p.getBarcode()).contains(text)
                        || safeLower(p.getCategory()).contains(text))
                .collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);

        archivedTable.setItems(filtered);
    }


    private String safeLower(String input) {
        return input == null ? "" : input.toLowerCase();
    }

}
