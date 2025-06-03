package com.example.ims2;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LastActionManager {
    private static final String FILE_NAME = "last_actions.csv";

    public static void saveLastAction(String username, String actionDescription) {
        Map<String, String> actions = loadAllActions();
        actions.put(username, actionDescription);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, String> entry : actions.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLastAction(String username) {
        Map<String, String> actions = loadAllActions();
        return actions.getOrDefault(username, "No previous actions found.");
    }

    private static Map<String, String> loadAllActions() {
        Map<String, String> actions = new HashMap<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) return actions;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    actions.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return actions;
    }
}
