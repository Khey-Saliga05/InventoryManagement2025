package com.example.ims2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MainActivityStorage {
    private static final String FILE_NAME = "data/main_activities.csv";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    public static void saveActivities(ObservableList<Activity> activities) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))) {

            writer.write("DateTime,Barcode,Name,Unit,Category,Quantity,Unit Cost,Total Cost,Status,User");
            writer.newLine();


            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Activity activity : activities) {
                String line = String.join(",",
                        escape(dateTimeFormat.format(activity.getDateTime())),
                        escape(activity.getBarcode()),
                        escape(activity.getName()),
                        escape(activity.getUnit()),
                        escape(activity.getCategory()),
                        String.valueOf(activity.getQuantity()),
                        formatDouble(activity.getUnitCost()),
                        formatDouble(activity.getTotalCost()),
                        escape(activity.getStatus()),
                        escape(activity.getUser())
                );
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving activity list: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ObservableList<Activity> loadActivities() {
        ObservableList<Activity> list = FXCollections.observableArrayList();

        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No activity file found. A new one will be created.");
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("DateTime")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length == 10) {
                    try {
                        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime dateTime = LocalDateTime.parse(unescape(parts[0]), dateTimeFormat);

                        String status = unescape(parts[8]); // ✅ Correct index for status
                        if (status.contains("Edited") && status.contains("Price")) {
                            status = status.replaceAll("Price '.*?' to '.*?'", "Edited (Price changed)");
                        }

                        Activity activity = new Activity(
                                dateTime,
                                unescape(parts[1]),  // barcode
                                unescape(parts[2]),  // name
                                unescape(parts[3]),  // unit
                                unescape(parts[4]),  // category
                                Integer.parseInt(parts[5]), // quantity
                                Double.parseDouble(parts[6]), // unitCost
                                Double.parseDouble(parts[7]), // totalCost
                                unescape(parts[8]), // actionType (status)
                                "",                 // details (CSV doesn't store this yet, so default to "")
                                "",                 // remarks
                                unescape(parts[9])  // user
                        );


                        list.add(activity);
                    } catch (Exception e) {
                        System.err.println("Skipping malformed activity entry: " + line);
                    }
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading activity file: " + e.getMessage());
        }

        return list;
    }


    public static boolean clearAllActivities() {
        File file = new File(FILE_NAME);
        return file.delete() && initializeCSV();
    }

    private static boolean initializeCSV() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))) {
            writer.write("DateTime,Barcode,Name,Unit,Quantity,Unit Cost,Total Cost,Status,User");
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error initializing CSV file: " + e.getMessage());
            return false;
        }
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace(",", " ");
    }

    private static String unescape(String value) {
        return value == null ? "" : value.trim();
    }

    private static String formatDouble(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
