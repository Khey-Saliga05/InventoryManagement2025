// File: ActivityLogManager.java
package com.example.ims2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ActivityLogManager {
    private static ActivityLogManager instance;
    private final ObservableList<Activity> activityList;

    private ActivityLogManager() {
        activityList = FXCollections.observableArrayList();
    }

    public static ActivityLogManager getInstance() {
        if (instance == null) {
            instance = new ActivityLogManager();
        }
        return instance;
    }

    public ObservableList<Activity> getAllActivities() {
        return activityList;
    }

    public void logActivity(Activity activity) {
        activityList.add(0, activity); // Add to top
    }

    public void clearActivities() {
        activityList.clear();
    }
}
