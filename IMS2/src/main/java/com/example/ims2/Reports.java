package com.example.ims2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class Reports implements Initializable {

    @FXML private Label totalstockin, totalstockout, totalstock, totalarchive, time;
    @FXML private TableColumn<Product, String> barcodeColumn, nameColumn, unitColumn, categoryColumn, statusColumn, usercolumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, Double> unitCostColumn, totalCostColumn;
    @FXML private TableColumn<Product, Integer> totalstockincolumn;
    @FXML private TableColumn <Product, Integer> totalstockoutcolumn;
    @FXML private ComboBox<String> categoryComboBox, yearCB, monthCB, weekCB, dayCB;
    @FXML private Button btnReturn, btnProductList, resetFiltersBtn, searchButton, recalculateButton;
    @FXML private TextField searchField;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, LocalDate> dateColumn;
    @FXML private TableColumn<Product, LocalTime> timeColumn;
    private ObservableList<Product> fullProductList = FXCollections.observableArrayList();

    private List<Product> loadedProducts = new ArrayList<>(); // Ensure this is done somewhere before use
    private ObservableList<Product> baseProductList;
    private FilteredList<Product> filteredProducts;

    private ObservableList<Activity> allActivities;

    private List<Product> archivedProducts;
    private int lastActivityCount = 0; // For polling

    public Reports() {
        // Required no-argument constructor
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        restoreSession();
        startClock();
        setupActivityTable();

        loadedProducts = ProductStorage.loadFromCSV();
        baseProductList = FXCollections.observableArrayList(loadedProducts);
        filteredProducts = new FilteredList<>(baseProductList, p -> true);
        productTable.setItems(filteredProducts);
        fullProductList = FXCollections.observableArrayList();

        archivedProducts = ProductStorage.loadArchivedFromCSV();
        allActivities = ActivityLogManager.getInstance().getAllActivities();

        lastActivityCount = allActivities.size();

        calculatePerProductStockMovements();
        populateComboBoxes();
        updateStatistics();
        updateReportLabels();
        filterActivities();

        attachListeners();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> checkForNewActivities()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void attachListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterActivities());
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterActivities());
        yearCB.valueProperty().addListener((obs, oldVal, newVal) -> filterActivities());
        monthCB.valueProperty().addListener((obs, oldVal, newVal) -> filterActivities());
        weekCB.valueProperty().addListener((obs, oldVal, newVal) -> filterActivities());
        dayCB.valueProperty().addListener((obs, oldVal, newVal) -> filterActivities());

        MainController.productList.addListener((ListChangeListener<Product>) change -> {
            loadedProducts = new ArrayList<>(MainController.productList);
            filteredProducts.setAll(loadedProducts);
            refreshData(); // full refresh ensures consistency
        });
    }

    private void checkForNewActivities() {
        ObservableList<Activity> current = ActivityLogManager.getInstance().getAllActivities();
        if (current.size() != lastActivityCount) {
            Platform.runLater(this::refreshData);
        }
    }


    private void restoreSession() {
        String lastUser = AccountManager.loadLastLoggedInUser();
        if (lastUser != null) {
            Session.setLoggedInUser(lastUser);
        }
    }

    private void setupActivityTable() {
        barcodeColumn.setCellValueFactory(data -> data.getValue().barcodeProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().productNameProperty());
        unitColumn.setCellValueFactory(data -> data.getValue().unitProperty());
        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        usercolumn.setCellValueFactory(data -> data.getValue().userProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());

        totalstockincolumn.setCellValueFactory(cellData -> cellData.getValue().totalStockInProperty().asObject());

        totalstockoutcolumn.setCellValueFactory(cellData -> cellData.getValue().totalStockOutProperty().asObject());

        quantityColumn.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());
        unitCostColumn.setCellValueFactory(data -> data.getValue().unitCostProperty().asObject());
        totalCostColumn.setCellValueFactory(data -> data.getValue().totalCostProperty().asObject());

        centerColumn(barcodeColumn);
        centerColumn(nameColumn);
        centerColumn(usercolumn);
        centerColumn(unitColumn);
        centerColumn(categoryColumn);
        centerColumn(quantityColumn);
        centerColumn(unitCostColumn);
        centerColumn(totalCostColumn);
        centerColumn(totalstockincolumn);
        centerColumn(totalstockoutcolumn);
        centerColumn(dateColumn);

        unitCostColumn.setCellFactory(col -> currencyCell());
        totalCostColumn.setCellFactory(col -> currencyCell());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        timeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(timeFormatter));
                setStyle("-fx-alignment: CENTER;");
            }
        });

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
        loadAllProducts(); // Load into fullProductList
        setupFiltering();

    }

    private void setupFiltering() {
        filteredProducts = new FilteredList<>(fullProductList, p -> true);
        productTable.setItems(filteredProducts);

        filteredProducts.addListener((ListChangeListener<Product>) change -> updateStockTotals());

        addStockChangeListeners();

        updateStockTotals();
    }

    private void addStockChangeListeners() {
        for (Product product : fullProductList) {
            product.totalStockInProperty().addListener((obs, oldVal, newVal) -> updateStockTotals());
            product.totalStockOutProperty().addListener((obs, oldVal, newVal) -> updateStockTotals());
        }
    }

    private void loadAllProducts() {
        fullProductList.clear();
        fullProductList.addAll(ProductStorage.loadFromCSV()); // Your own method
    }


    private <T> void centerColumn(TableColumn<Product, T> column) {
        column.setStyle("-fx-alignment: CENTER;");
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

    private void refreshData() {
        allActivities = ActivityLogManager.getInstance().getAllActivities();
        lastActivityCount = allActivities.size();
        updateStockTotals();
        calculatePerProductStockMovements();
        updateStatistics();
        updateReportLabels();
        filterActivities();
        productTable.refresh();
    }


    public void calculatePerProductStockMovements() {
        if (allActivities == null) return;

        Map<String, IntSummaryStatistics> stockInMap = allActivities.stream()
                .filter(a -> a.getStatus().equalsIgnoreCase("stocked in"))
                .collect(Collectors.groupingBy(Activity::getBarcode,
                        Collectors.summarizingInt(Activity::getQuantity)));

        Map<String, IntSummaryStatistics> stockOutMap = allActivities.stream()
                .filter(a -> a.getStatus().equalsIgnoreCase("stocked out"))
                .collect(Collectors.groupingBy(Activity::getBarcode,
                        Collectors.summarizingInt(Activity::getQuantity)));

        for (Product product : baseProductList) {
            int stockIn = (int) stockInMap.getOrDefault(product.getBarcode(), new IntSummaryStatistics()).getSum();
            int stockOut = (int) stockOutMap.getOrDefault(product.getBarcode(), new IntSummaryStatistics()).getSum();
            product.setTotalStockIn(stockIn);
            product.setTotalStockOut(stockOut);
        }

        productTable.refresh();
    }


    private void loadAllActivityData() {
        allActivities = ActivityLogManager.getInstance().getAllActivities();
        FilteredList<Activity> filteredList = new FilteredList<>(allActivities, p -> true);
        lastActivityCount = allActivities.size();
    }

    private void populateComboBoxes() {
        Set<String> categories = loadedProducts.stream()
                .map(Product::getCategory)
                .collect(Collectors.toCollection(TreeSet::new));

        Set<String> years = allActivities.stream().map(a -> a.getDate().substring(0, 4)).collect(Collectors.toCollection(TreeSet::new));
        Set<String> months = allActivities.stream().map(Activity::getMonth).collect(Collectors.toCollection(TreeSet::new));
        Set<String> weeks = allActivities.stream().map(Activity::getWeek).collect(Collectors.toCollection(TreeSet::new));
        Set<String> days = allActivities.stream().map(Activity::getDay).collect(Collectors.toCollection(TreeSet::new));

        categoryComboBox.setItems(FXCollections.observableArrayList(addAllOption(categories)));
        yearCB.setItems(FXCollections.observableArrayList(addAllOption(years)));
        monthCB.setItems(FXCollections.observableArrayList(addAllOption(months)));
        weekCB.setItems(FXCollections.observableArrayList(addAllOption(weeks)));
        dayCB.setItems(FXCollections.observableArrayList(addAllOption(days)));

        categoryComboBox.setValue("All");
        yearCB.setValue("All");
        monthCB.setValue("All");
        weekCB.setValue("All");
        dayCB.setValue("All");
    }

    private List<String> addAllOption(Set<String> items) {
        List<String> list = new ArrayList<>(items);
        Collections.sort(list);
        list.add(0, "All");
        return list;
    }

    public void updateStatistics() {
        int stockIn = allActivities.stream()
                .filter(a -> a.getStatus().equalsIgnoreCase("stocked in"))
                .mapToInt(Activity::getQuantity)
                .sum();

        int stockOut = allActivities.stream()
                .filter(a -> a.getStatus().equalsIgnoreCase("stocked out"))
                .mapToInt(Activity::getQuantity)
                .sum();

        int archivedCount = archivedProducts.size();

        totalstockin.setText(String.valueOf(stockIn));
        totalstockout.setText(String.valueOf(stockOut));
        totalarchive.setText(String.valueOf(archivedCount));
    }


    private void filterActivities() {
        String keyword = Optional.ofNullable(searchField.getText()).orElse("").toLowerCase().trim();
        String selectedCategory = Optional.ofNullable(categoryComboBox.getValue()).orElse("All");
        String selectedYear = Optional.ofNullable(yearCB.getValue()).orElse("All");
        String selectedMonth = Optional.ofNullable(monthCB.getValue()).orElse("All");
        String selectedWeek = Optional.ofNullable(weekCB.getValue()).orElse("All");
        String selectedDay = Optional.ofNullable(dayCB.getValue()).orElse("All");

        filteredProducts.setPredicate(product -> {
            // Basic product filters (search keyword & category)
            boolean matchesKeyword = keyword.isEmpty() ||
                    product.getBarcode().toLowerCase().contains(keyword) ||
                    product.getName().toLowerCase().contains(keyword) ||
                    product.getCategory().toLowerCase().contains(keyword);

            boolean matchesCategory = selectedCategory.equals("All") ||
                    product.getCategory().equalsIgnoreCase(selectedCategory);

            if (!(matchesKeyword && matchesCategory)) {
                return false; // no need to check activity if product doesn't match basic filters
            }

            List<Activity> relatedActivities = allActivities.stream()
                    .filter(a -> a.getBarcode().equals(product.getBarcode()))
                    .collect(Collectors.toList());

            if (selectedYear.equals("All") && selectedMonth.equals("All") &&
                    selectedWeek.equals("All") && selectedDay.equals("All")) {
                // No date filters, include product if it passed earlier filters
                return true;
            }

            // Check if any related activity matches all selected date filters
            boolean hasMatchingActivity = relatedActivities.stream().anyMatch(a -> {
                boolean yearMatch = selectedYear.equals("All") || a.getDate().startsWith(selectedYear);
                boolean monthMatch = selectedMonth.equals("All") || a.getMonth().equals(selectedMonth);
                boolean weekMatch = selectedWeek.equals("All") || a.getWeek().equals(selectedWeek);
                boolean dayMatch = selectedDay.equals("All") || a.getDay().equals(selectedDay);

                return yearMatch && monthMatch && weekMatch && dayMatch;
            });

            return hasMatchingActivity;
        });

    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        categoryComboBox.setValue("All");
        yearCB.setValue("All");
        monthCB.setValue("All");
        weekCB.setValue("All");
        dayCB.setValue("All");
        filterActivities();
    }

    @FXML
    private void onRecalculateTotalsClicked() {
        loadedProducts = ProductStorage.loadFromCSV();
        archivedProducts = ProductStorage.loadArchivedFromCSV();
        allActivities = ActivityLogManager.getInstance().getAllActivities();
        productTable.setItems(filteredProducts);
        productTable.refresh();

        baseProductList.setAll(loadedProducts);  // ✅ Update the source list
        calculatePerProductStockMovements();
        ProductStorage.saveToCSV(loadedProducts);

        productTable.refresh();

        populateComboBoxes();
        updateStatistics();
        updateReportLabels();
        filterActivities();

        System.out.println("✅ Totals recalculated and UI refreshed.");
    }


    @FXML
    private void goToProductList(ActionEvent event) {
        switchScene(event, "/com/example/ims2/Product-list.fxml");
    }

    @FXML
    private void OnReturn(ActionEvent event) {
        switchScene(event, "/com/example/ims2/main.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setScene(new Scene(root, screenBounds.getWidth(), screenBounds.getHeight()));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load " + fxmlPath, ButtonType.OK).showAndWait();
        }
    }

    public void setMainController(MainController mainController) {
    }

    @FXML
    private void onSearch() {
        String keyword = Optional.ofNullable(searchField.getText())
                .orElse("")
                .toLowerCase()
                .trim();

        filteredProducts.setPredicate(product -> {
            if (keyword.isEmpty()) {
                return true;
            }

            boolean matchesBarcode = product.getBarcode() != null &&
                    product.getBarcode().toLowerCase().contains(keyword);
            boolean matchesName = product.getProductName() != null &&
                    product.getProductName().toLowerCase().contains(keyword);
            boolean matchesCategory = product.getCategory() != null &&
                    product.getCategory().toLowerCase().contains(keyword);

            return matchesBarcode || matchesName || matchesCategory;
        });

        productTable.setItems(filteredProducts);
        productTable.setItems(ProductStorageManager.getInstance().getProductList());
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

    public void refreshTable() {
        ObservableList<Product> products = ProductStorageManager.getInstance().getProductList();
        loadedProducts = new ArrayList<>(products);
        filteredProducts = new FilteredList<>(products, p -> true);
        productTable.setItems(filteredProducts);
        filterActivities();
        productTable.setItems(FXCollections.observableArrayList(ProductStorageManager.getInstance().getProductList()));
        productTable.refresh();
    }

    private static Reports instance;

    public Reports(ObservableList<Product> fullProductList) {
        this.fullProductList = fullProductList;
        instance = this;
    }

    public static Reports getInstance() {
        return instance;
    }

    public static void refreshReportsView() {
        if (instance != null) {
            instance.loadAllActivityData();      // Reload activities
            instance.populateComboBoxes();       // Update filters
            instance.calculatePerProductStockMovements(); // Update per-product stock in/out
            instance.updateStatistics();         // Update totals (optional)
            instance.filterActivities();         // Reapply filters
            instance.productTable.refresh();     // Refresh table view display
            ProductStorageManager.getInstance().setProductList(ProductStorage.loadFromCSV());
        }
    }

    public void updateDateComboBoxesToCurrent() {
        LocalDate now = LocalDate.now();
        loadAllActivityData(); // reload all activities
        populateComboBoxes();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int currentDay = now.getDayOfMonth();

        // Calculate week of year:
        int currentWeek = now.get(WeekFields.ISO.weekOfWeekBasedYear());

        // Update ComboBoxes if not null
        if (yearCB != null) {
            yearCB.setValue(String.valueOf(currentYear));
        }
        if (monthCB != null) {
            monthCB.setValue(String.valueOf(currentMonth));
        }
        if (weekCB != null) {
            weekCB.setValue(String.valueOf(currentWeek));
        }
        if (dayCB != null) {
            dayCB.setValue(String.valueOf(currentDay));
        }
    }

    public void refreshCategoryComboBox() {
        if (loadedProducts == null) {
            loadedProducts = ProductStorage.loadFromCSV();
        }

        Set<String> categories = loadedProducts.stream()
                .map(Product::getCategory)
                .collect(Collectors.toCollection(TreeSet::new));

        List<String> categoryList = new ArrayList<>(categories);
        categoryList.add(0, "All");

        Platform.runLater(() -> {
            categoryComboBox.setItems(FXCollections.observableArrayList(categoryList));
            categoryComboBox.setValue("All");
        });
    }

    private void updateStockTotals() {
        int totalIn = 0;
        int totalOut = 0;

        for (Product product : filteredProducts) {
            totalIn += product.getTotalStockIn();
            totalOut += product.getTotalStockOut();
        }

        totalstockin.setText(String.valueOf(totalIn));
        totalstockout.setText(String.valueOf(totalOut));
    }

    private void updateReportLabels() {
        updateStockTotals();  // Ensures UI labels reflect filtered values
    }

}
