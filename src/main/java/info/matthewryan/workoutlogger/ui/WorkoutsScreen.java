package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.Session;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import info.matthewryan.workoutlogger.persistence.SessionDao;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkoutsScreen {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutsScreen.class);

    private SessionDao sessionDao;
    private CustomToolBar toolBar;
    private ScreenStartup screenStartup;

    public WorkoutsScreen(SessionDao sessionDao, CustomToolBar toolBar, ScreenStartup screenStartup) {
        this.sessionDao = sessionDao;
        this.toolBar = toolBar;
        this.screenStartup = screenStartup;
    }

    public BorderPane getRoot() {
        Button btnStartSession = new Button("Start Session");

        btnStartSession.setOnAction(event -> {
            System.out.println("Start Session clicked!");

            // Start a new session using the SessionManager
            SessionManager.getInstance().startSession();

            // Use the existing ScreenStartup instance to show ActivityScreen
            screenStartup.showActivityScreen((BorderPane) toolBar.getScene().getRoot());
        });


        BorderPane startPane = new BorderPane();
        startPane.setCenter(btnStartSession); // Place button in the center of BorderPane

        return startPane;
    }
}
