// File: StorageUtils.java
package com.example.ims2;

import java.io.File;

public class StorageUtils {
    public static void ensureDataFolder() {
        File folder = new File("data");
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("📁 'data' folder created.");
            } else {
                System.err.println("❌ Failed to create 'data' folder.");
            }
        }
    }
}
