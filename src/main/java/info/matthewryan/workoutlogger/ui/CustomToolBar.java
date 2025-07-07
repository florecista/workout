package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomToolBar extends ToolBar {

    private static final Logger logger = LoggerFactory.getLogger(CustomToolBar.class);

    private Button btnWorkouts;
    private Button btnProgress;
    private Button btnHistory;
    private Button btnExercises;
    private Button btnSettings; // New Settings button

    public CustomToolBar(Stage primaryStage, ActivityDao activityDao, ExerciseDao exerciseDao) {
        super();

        btnWorkouts = new Button("Workouts");
        btnProgress = new Button("Progress");
        btnHistory = new Button("History");
        btnExercises = new Button("Exercises");
        btnSettings = new Button("Settings");

        // Workouts button action
        btnWorkouts.setOnAction(e -> {
            // This will be handled by ScreenStartup
        });

        // History button action
        btnHistory.setOnAction(e -> {
            // This will be handled by ScreenStartup
        });

        // Exercises button action
        btnExercises.setOnAction(e -> {
            // This will be handled by ScreenStartup
        });

        // Progress button action
        btnProgress.setOnAction(e -> {
            // Navigate to ProgressScreen
            // This will be handled by ScreenStartup
        });

        // Settings button action
        btnSettings.setOnAction(e -> {
            // Action for Settings button, for example, show the settings screen
            logger.info("Settings button clicked");
            // Add code here to show a Settings screen or popup
        });

        // Add all buttons to the toolbar, with the Settings button after Exercises
        getItems().addAll(btnWorkouts, btnProgress, btnHistory, btnExercises, btnSettings);
    }

    // Setters to attach actions in ScreenStartup
    public void setOnWorkoutsAction(Runnable action) {
        btnWorkouts.setOnAction(e -> action.run());
    }

    public void setOnHistoryAction(Runnable action) {
        btnHistory.setOnAction(e -> action.run());
    }

    public void setOnExercisesAction(Runnable action) {
        btnExercises.setOnAction(e -> action.run());
    }

    public void setOnProgressAction(Runnable action) {
        btnProgress.setOnAction(e -> action.run());
    }

    public void setOnSettingsAction(Runnable action) {
        btnSettings.setOnAction(e -> action.run());
    }
}
