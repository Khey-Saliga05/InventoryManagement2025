package com.example.ims2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    private static MainController instance;
    private final ProductStorageManager productStorageManager = ProductStorageManager.getInstance();
    private final ObservableList<Activity> activityList = FXCollections.observableArrayList();
    public static final ObservableList<Product> productList = ProductStorageManager.getInstance().getProductList();

    @FXML
        private Button btnProductList, btnAddProduct, btnStockIn, btnLogout, btnReports;
        @FXML
        private Label totalProductsLabel, lowStockLabel, outOfStockLabel, inStockLabel, archivedProductsLabel, time, currentUserLabel;
        @FXML
        private TableView<Activity> activityTable;
        @FXML
        private TableColumn<Activity, String> dateColumn, timeColumn, barcodeColumn, nameColumn, unitColumn, categoryColumn, statusColumn, usercolumn;
        @FXML
        private TableColumn<Activity, Integer> quantityColumn;
        @FXML
        private TableColumn<Activity, Double> unitCostColumn, totalCostColumn;
        @FXML
        private ComboBox<String> activityFilterComboBox;
        @FXML
        private Button clearButton, refreshButton, btnArchive;
        @FXML
        private TableView<Product> productTable;

        private final IntegerProperty totalProducts = new SimpleIntegerProperty();
        private final IntegerProperty lowStock = new SimpleIntegerProperty();
        private final IntegerProperty outOfStock = new SimpleIntegerProperty();
        private final IntegerProperty inStock = new SimpleIntegerProperty();

        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

        @FXML
        public void initialize() {
            String lastUser = AccountManager.loadLastLoggedInUser();
            if (lastUser != null) {
                Session.setLoggedInUser(lastUser);
                System.out.println("Restored session for: " + lastUser);
            }

            String fullname = Session.getLoggedInFullname();
            String role = Session.getLoggedInRole();

            if (fullname != null && role != null) {
                String roleLabel = role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Administrator") ? "Admin" : "Employee";
                currentUserLabel.setText(fullname + " (" + roleLabel + ")");
            } else {
                currentUserLabel.setText("Guest");
            }

            instance = this;
            startClock();
            setupActivityTable();

            totalProductsLabel.textProperty().bind(totalProducts.asString());
            lowStockLabel.textProperty().bind(lowStock.asString());
            outOfStockLabel.textProperty().bind(outOfStock.asString());
            inStockLabel.textProperty().bind(inStock.asString());

            totalProductsLabel.setOnMouseClicked(e -> loadProductListView("ALL"));
            inStockLabel.setOnMouseClicked(e -> loadProductListView("AVAILABLE"));
            lowStockLabel.setOnMouseClicked(e -> loadProductListView("LOW"));
            outOfStockLabel.setOnMouseClicked(e -> loadProductListView("OUT"));

            activityList.setAll(MainActivityStorage.loadActivities());
            updateFilteredActivities();
            setupFilterComboBox();

            productList.addListener((ListChangeListener<Product>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(this::attachQuantityListener);
                    }
                }
                updateProductCounts();
                refreshProductTable();
                refreshComboBox();
            });

            productList.forEach(this::attachQuantityListener);
            updateProductCounts();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                productStorageManager.save();
                MainActivityStorage.saveActivities(activityList);
            }));

            activityList.addListener((ListChangeListener<Activity>) change -> {
                updateFilteredActivities();
                activityTable.refresh();
            });

            if (activityFilterComboBox != null) {
                setupFilterComboBox();
            } else {
                System.err.println("activityFilterComboBox is NULL");
            }

        }

        private void setupActivityTable() {
            dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
            barcodeColumn.setCellValueFactory(cellData -> cellData.getValue().barcodeProperty());
            nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            unitColumn.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
            categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
            quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
            unitCostColumn.setCellValueFactory(cellData -> cellData.getValue().unitCostProperty().asObject());
            totalCostColumn.setCellValueFactory(cellData -> cellData.getValue().totalCostProperty().asObject());
            usercolumn.setCellValueFactory(cellData -> cellData.getValue().userProperty());
            statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDisplayActivity()));
            timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());

            centerTextInColumn(dateColumn);
            centerTextInColumn(barcodeColumn);
            centerTextInColumn(nameColumn);
            centerTextInColumn(unitColumn);
            centerTextInColumn(categoryColumn);
            centerTextInColumn(quantityColumn);
            centerTextInColumn(usercolumn);
            centerTextInColumn(statusColumn);
            centerTextInColumn(timeColumn);


            unitCostColumn.setCellFactory(col -> pesoCellFactory());
            totalCostColumn.setCellFactory(col -> pesoCellFactory());

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



        activityTable.setItems(activityList);
        activityTable.getSortOrder().clear();
    }

    private <T> void centerTextInColumn(TableColumn<Activity, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-alignment: CENTER;");
                setText(empty || item == null ? null : item.toString());
            }
        });
    }

    private TableCell<Activity, Double> pesoCellFactory() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-alignment: CENTER;");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("₱" + String.format("%,.2f", item));
                }
            }
        };
    }


    private void attachQuantityListener(Product product) {
        product.quantityProperty().addListener((obs, oldVal, newVal) -> updateProductCounts());
    }

    public void logActivity(Product oldProduct, Product newProduct, String actionType, LocalDateTime timestamp, String username) {
        if ((newProduct == null && oldProduct == null) || actionType == null || actionType.isEmpty()) return;

        Product product = newProduct != null ? newProduct : oldProduct;

        String status = Arrays.stream(actionType.toLowerCase().split(" "))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));

        int quantity = product.getQuantity();
        double unitCost = product.getUnitCost();

        if ("stocked in".equalsIgnoreCase(actionType) && oldProduct != null && newProduct != null) {
            quantity = newProduct.getQuantity() - oldProduct.getQuantity();
            status = quantity <= 0 ? "Stocked In (No quantity change)" : "Stocked In (+" + quantity + ")";
        }

        if ("stocked out".equalsIgnoreCase(actionType) && oldProduct != null && newProduct != null) {
            quantity = oldProduct.getQuantity() - newProduct.getQuantity();
            status = quantity <= 0 ? "Stocked Out (No quantity change)" : "Stocked Out (-" + quantity + ")";
        }

        if ("edited".equalsIgnoreCase(actionType) && oldProduct != null && newProduct != null) {
            String editDetails = generateEditDetails(oldProduct, newProduct);
            if (!editDetails.isEmpty()) status += " (" + editDetails + ")";
        }

        String category = product.getCategory();
        if (category == null || category.trim().isEmpty()) {
            category = "Uncategorized";
        }

        Activity activity = new Activity(
                timestamp != null ? timestamp : LocalDateTime.now(),
                product.getBarcode(),
                product.getName(),
                product.getUnit(),
                category,
                quantity,
                unitCost,
                quantity * unitCost,
                status,
                "",
                "",
                username
        );

        activityList.addFirst(activity);
        MainActivityStorage.saveActivities(activityList);
        updateProductCounts();
        refreshProductTable();
    }


    private String generateEditDetails(Product oldProduct, Product newProduct) {
        StringBuilder details = new StringBuilder();
        if (!oldProduct.getName().equals(newProduct.getName())) {
            details.append("Name '").append(oldProduct.getName()).append("' to '").append(newProduct.getName()).append("', ");
        }
        if (oldProduct.getPrice() != newProduct.getPrice()) {
            details.append("Price '").append(oldProduct.getPrice()).append("' to '").append(newProduct.getPrice()).append("', ");
        }
        if (!oldProduct.getUnit().equals(newProduct.getUnit())) {
            details.append("Unit '").append(oldProduct.getUnit()).append("' to '").append(newProduct.getUnit()).append("', ");
        }
        if (oldProduct.getUnitCost() != newProduct.getUnitCost()) {
            details.append("Unit Cost '").append(oldProduct.getUnitCost()).append("' to '").append(newProduct.getUnitCost()).append("', ");
        }
        if (details.length() > 2) details.setLength(details.length() - 2);
        return details.toString();
    }

    private void updateFilteredActivities() {
        filterActivities();
    }

    private void setupFilterComboBox() {
        ObservableList<String> filters = FXCollections.observableArrayList(
                "All Activities", "Products Added", "Stocked In", "Stocked Out", "Edited", "Archived", "Restored", "Deleted"
        );
        activityFilterComboBox.setItems(filters);
        activityFilterComboBox.getSelectionModel().selectFirst();
        activityFilterComboBox.setOnAction(event -> filterActivities());
        filterActivities();
    }

    private void filterActivities() {
        String selected = activityFilterComboBox.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equalsIgnoreCase("All Activities")) {
            activityTable.setItems(FXCollections.observableArrayList(activityList));
        } else {
            ObservableList<Activity> filtered = activityList.filtered(activity ->
                    activity.getFilterKey() != null &&
                            activity.getFilterKey().toLowerCase().contains(selected.toLowerCase()));
            activityTable.setItems(filtered);
        }
    }

    @FXML
    private void onClearButtonClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Activities");
        alert.setHeaderText("Are you sure you want to clear all activity logs?");
        alert.setContentText("This action will remove all records from the activity log.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (MainActivityStorage.clearAllActivities()) {
                activityList.clear();
                filterActivities();
                new Alert(Alert.AlertType.INFORMATION, "All activities have been cleared.").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to clear activity logs.").showAndWait();
            }
        }
    }

    @FXML
    private void onRefreshButtonClick() {
        activityList.setAll(MainActivityStorage.loadActivities());
        filterActivities();
        activityTable.refresh();
    }

    void updateProductCounts() {
        int total = productList.size();
        int low = 0, out = 0, in = 0;
        for (Product product : productList) {
            int qty = product.getQuantity();
            if (qty == 0) out++;
            else {
                in++;
                if (qty <= 5) low++;
            }
        }
        totalProducts.set(total);
        lowStock.set(low);
        outOfStock.set(out);
        inStock.set(in);
    }

    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE - MMMM dd, yyyy\nhh:mm:ss a");
        time.setText(LocalDateTime.now().format(formatter));
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event ->
                time.setText(LocalDateTime.now().format(formatter))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void loadProductListView(String filter) {
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("/com/example/ims2/Product-list.fxml"));
            Parent root = loader.load();
            ProductListController controller = loader.getController();
            if (controller != null) {
                controller.setInitialFilter(filter);
                controller.setMainController(this);
            }

            Stage stage = (Stage) totalProductsLabel.getScene().getWindow();

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

    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();


            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double width = screenBounds.getWidth();
            double height = screenBounds.getHeight();

            stage.setScene(new Scene(root, width, height));
            stage.setMaximized(true);

            Object controller = loader.getController();
            if (controller instanceof ProductListController) {
                ((ProductListController) controller).setMainController(this);
            }

            stage.show();
        } catch (IOException e) {
            showAlert("Failed to load " + fxmlPath);
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    public void goToProductList(ActionEvent event) {
        loadScene("/com/example/ims2/Product-list.fxml", event);
    }

    public void goToReports(ActionEvent event) {
        loadReport("/com/example/ims2/Reports.fxml", event);

        productTable.setItems(ProductStorageManager.getInstance().getProductList());
        productTable.refresh(); // Make sure this is called after the table has been bound
    }

    private void loadReport(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get the current stage from the event
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double width = screenBounds.getWidth();
            double height = screenBounds.getHeight();

            // Set the new scene with full screen size
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setMaximized(true);

            // Link back to main controller if applicable
            Object controller = loader.getController();
            if (controller instanceof Reports reportsController) {
                reportsController.setMainController(this);
            }

            stage.show();

        } catch (IOException e) {
            e.printStackTrace(); // For debugging
            showAlert("Failed to load: " + fxmlPath);
        }
    }


    public void goToLogin(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Are you sure you want to Log Out?");
        alert.setContentText("If yes, choose 'OK'.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String currentUser = Session.getLoggedInUser();
            if (currentUser != null) {
                AccountManager.saveLastLoggedInUser(currentUser);
            }
            loadScene("/com/example/ims2/login.fxml", event);
        }
    }


    public static MainController getInstance() {
        return instance;
    }

    public ObservableList<Activity> getActivityList() {
        return activityList;
    }

    public void refreshActivityLog() {
        FXCollections.sort(activityList, Comparator.comparing(Activity::getDate).reversed());
        activityTable.refresh();
    }

    public void refreshProductTable() {
        if (productTable != null) {
            productTable.setItems(productList);
            productTable.refresh();
        }
    }

    public void refreshComboBox() {
        ObservableList<String> filters = FXCollections.observableArrayList(
                "All Activities", "Products Added", "Stocked In", "Stocked Out", "Edited", "Deleted"
        );
        activityFilterComboBox.setItems(filters);
        activityFilterComboBox.getSelectionModel().selectFirst();
    }


    public void refreshTableView() {
        productTable.setItems(FXCollections.observableArrayList(productList));
    }


    public void logActivity(Activity activity) {
        if (activity == null) {
            return;
        }

        if (activity.getDate() == null || activity.getBarcode() == null || activity.getStatus() == null) {
            System.err.println("Attempted to log an incomplete activity.");
            return;
        }

        activityList.add(0, activity);


        MainActivityStorage.saveActivities(activityList);

        updateFilteredActivities();
        activityTable.refresh();
        refreshProductTable();
        refreshComboBox();
    }

    private ProductListController mainController;

    public void setMainController(ProductListController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void showArchivedProducts() {
        loadProductListView("ARCHIVED");
    }

    @FXML
    private void showRestoredProducts() {
        loadProductListView("RESTORED");
    }

    public void logActivity(Object o, Product newProduct, String productAdded, LocalDateTime now) {
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

}