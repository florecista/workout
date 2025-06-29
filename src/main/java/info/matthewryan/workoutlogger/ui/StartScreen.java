package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class StartScreen {

    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;
    private ScreenStartup screenStartup; // Reference to the existing ScreenStartup instance

    // Constructor accepts ActivityDao, ExerciseDao, ToolBar, and ScreenStartup
    public StartScreen(ActivityDao activityDao, ExerciseDao exerciseDao, CustomToolBar toolBar, ScreenStartup screenStartup) {
        this.activityDao = activityDao;
        this.exerciseDao = exerciseDao;
        this.toolBar = toolBar;
        this.screenStartup = screenStartup; // Initialize the ScreenStartup reference
    }

    public BorderPane getRoot() {
        Button btnStartSession = new Button("Start Session");

        // When clicked, show the Activity Screen
        btnStartSession.setOnAction(event -> {
            // Use the existing ScreenStartup instance to show ActivityScreen
            screenStartup.showActivityScreen((BorderPane) toolBar.getScene().getRoot());
        });

        BorderPane startPane = new BorderPane();
        startPane.setCenter(btnStartSession); // Place button in the center of BorderPane

        return startPane;
    }
}
