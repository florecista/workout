package info.matthewryan.workoutlogger.model;

import info.matthewryan.workoutlogger.Application;
import info.matthewryan.workoutlogger.ApplicationSettings;

public class ActivityRecordTest {

    public static void main(String[] args) {
        // Initialize Application and load settings
        Application app = new Application();
        app.initialize();  // Loads settings

        // Create a new activity record
        ApplicationSettings settings = app.getSettings();
        ActivityRecord record = new ActivityRecord("Bench Press", 5, 100.0, System.currentTimeMillis());

        // Print the rounded weight
        System.out.println("Weight: " + record.getWeight());
    }
}
