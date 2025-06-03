package com.example.ims2;

import java.io.*;

public class Session {
    private static String loggedInUser;
    private static String loggedInFullname;
    private static String loggedInRole;
    private static String creatingRole; // 👈 For holding role during account creation

    static {
        loadSession();
    }

    public static void setLoggedInUser(String username) {
        loggedInUser = username;
        saveSessionToFile(username);
    }

    public static String getLoggedInUser() {
        return loggedInUser;
    }

    public static void setLoggedInFullname(String fullname) {
        loggedInFullname = fullname;
    }

    public static String getLoggedInFullname() {
        return loggedInFullname;
    }

    public static void setLoggedInRole(String role) {
        loggedInRole = role;
    }

    public static String getLoggedInRole() {
        return loggedInRole;
    }

    // ✅ Store the selected role (Admin or Viewer) before account creation
    public static void setCreatingRole(String role) {
        creatingRole = role;
    }

    public static String getCreatingRole() {
        return creatingRole;
    }

    private static void saveSessionToFile(String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("session.txt"))) {
            writer.write(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadSession() {
        File sessionFile = new File("session.txt");
        if (sessionFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(sessionFile))) {
                String username = reader.readLine();
                if (username != null && !username.trim().isEmpty()) {
                    loggedInUser = username.trim();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
