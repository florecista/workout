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

    private Button btnRoutines;
    private Button btnHistory;
    private Button btnExercises;

    public CustomToolBar(Stage primaryStage, ActivityDao activityDao, ExerciseDao exerciseDao) {
        super();
        btnRoutines = new Button("Routines");
        btnHistory = new Button("History");
        btnExercises = new Button("Exercises");  // Initialize the Exercises button

        btnRoutines.setOnAction(e -> {
            // This will be handled by ScreenStartup
        });

        btnHistory.setOnAction(e -> {
            // This will be handled by ScreenStartup
        });

        btnExercises.setOnAction(e -> {
            // This will be handled by ScreenStartup
        });

        getItems().addAll(btnRoutines, btnHistory, btnExercises);  // Add the new button to the toolbar
    }

    // Setters to attach actions in ScreenStartup
    public void setOnRoutinesAction(Runnable action) {
        btnRoutines.setOnAction(e -> action.run());
    }

    public void setOnHistoryAction(Runnable action) {
        btnHistory.setOnAction(e -> action.run());
    }

    public void setOnExercisesAction(Runnable action) {
        btnExercises.setOnAction(e -> action.run());
    }
}
