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

    public CustomToolBar(Stage primaryStage, ActivityDao activityDao, ExerciseDao exerciseDao) {
        super();
        btnRoutines = new Button("Routines");
        btnHistory = new Button("History");

        // Routines button action
        btnRoutines.setOnAction(e -> {
            // This will be handled by ScreenStartup
        });

        // History button action
        btnHistory.setOnAction(e -> {
            // This will be handled by ScreenStartup
        });

        getItems().addAll(btnRoutines, btnHistory);
    }

    // Setters to attach actions in ScreenStartup
    public void setOnRoutinesAction(Runnable action) {
        btnRoutines.setOnAction(e -> action.run());
    }

    public void setOnHistoryAction(Runnable action) {
        logger.info("History button clicked");
        btnHistory.setOnAction(e -> action.run());
    }
}
