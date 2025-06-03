package com.example.ims2;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static final String ACCOUNTS_CSV = "accounts.csv";
    private static final String LAST_LOGIN_FILE = "lastlogin.txt";

    public static boolean validateLogin(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(username) && parts[2].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean isUsernameTaken(String username) {
        File file = new File(ACCOUNTS_CSV);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] account = line.split(",", -1);
                if (account.length >= 1 && account[0].equalsIgnoreCase(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean saveNewAccount(String username, String fullname, String password, String role) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("accounts.csv", true))) {
            writer.write(username + "," + fullname + "," + password + "," + role);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static String getFullnameByUsername(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNTS_CSV))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] account = line.split(",", -1);
                if (account.length >= 3 && account[0].equals(username)) {
                    return account[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRoleByUsername(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNTS_CSV))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] account = line.split(",", -1);
                if (account.length >= 4 && account[0].equals(username)) {
                    return account[3];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveLastLoggedInUser(String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LAST_LOGIN_FILE))) {
            writer.write(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadLastLoggedInUser() {
        File file = new File(LAST_LOGIN_FILE);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String username = reader.readLine();
            return username != null ? username.trim() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String[]> getAllAccounts() {
        List<String[]> accounts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNTS_CSV))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] account = line.split(",", -1);
                if (account.length >= 4) {
                    accounts.add(account);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return accounts;
    }
}
