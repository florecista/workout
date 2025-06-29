package info.matthewryan.workoutlogger;

import info.matthewryan.workoutlogger.services.WorkoutService;

public class Main {
    public static void main(String[] args) {
        // Create ApplicationSettings instance with preferMetricUnits set to true
        ApplicationSettings settings = new ApplicationSettings();  // true for metric units

        // Create WorkoutService with the settings
        WorkoutService workoutService = new WorkoutService(settings);

        // Record an activity
        workoutService.recordActivity("Bench Press", 5, 100.0);

        // Print session history
        workoutService.getSessionHistory().forEach(record -> {
            System.out.println(record.getActivity() + ": " + record.getWeight() + " kg");
        });
    }
}
