package info.matthewryan.workoutlogger.model;

import info.matthewryan.workoutlogger.Application;
import info.matthewryan.workoutlogger.ApplicationSettings;

public class ActivityRecordTest {

    public static void main(String[] args) {
        int sessionId = 1; // Mock sessionId for testing

        // Initialize Application and load settings
        Application app = new Application();
        app.initialize();  // Loads settings

        // Create a new activity record
        ApplicationSettings settings = app.getSettings();
        Exercise exercise = new Exercise(1, "Bench Press", false);
        ActivityRecord record = new ActivityRecord(exercise.getId(), 5, 100.0, System.currentTimeMillis(), sessionId);

        // Print the rounded weight
        System.out.println("Weight: " + record.getWeight());
    }
}
