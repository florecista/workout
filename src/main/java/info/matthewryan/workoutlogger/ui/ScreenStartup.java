package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import info.matthewryan.workoutlogger.utils.DatabaseConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScreenStartup extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ScreenStartup.class);

    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;

    private StartScreen startScreen;
    private ActivityScreen activityScreen;
    private HistoryScreen historyScreen;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database connection
        DatabaseConnection databaseConnection = new DatabaseConnection();

        // Initialize the ActivityDao and ExerciseDao with the database connection
        activityDao = new ActivityDao(databaseConnection.getConnection());
        exerciseDao = new ExerciseDao(databaseConnection.getConnection());

        // Create tables if they do not exist
        activityDao.createActivityTable();
        exerciseDao.createExerciseTable();


        List<ActivityRecord> activities = activityDao.getAllActivitiesOrderedByTimestamp();
        logger.info(activityDao.toString());

        // Initialize screens
        toolBar = new CustomToolBar(primaryStage, activityDao, exerciseDao);
        activityScreen = new ActivityScreen(activityDao, exerciseDao, toolBar);
        startScreen = new StartScreen(activityDao, exerciseDao, toolBar, this);
        historyScreen = new HistoryScreen(activityDao, exerciseDao, toolBar);

        // Set up a BorderPane to hold all the screens (Deck of screens)
        BorderPane deck = new BorderPane();
        deck.setCenter(startScreen.getRoot()); // Initially show the Start Screen

        // ToolBar Actions
        toolBar.setOnRoutinesAction(() -> showStartScreen(deck));
        toolBar.setOnHistoryAction(() -> showHistoryScreen(deck));

        // Set up the layout with the toolbar and the current screen
        deck.setBottom(toolBar); // Attach the toolbar to the bottom of BorderPane

        Scene scene = new Scene(deck, 400, 600);
        primaryStage.setTitle("Workout Logger");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Show the StartScreen (by removing others and showing StartScreen)
    private void showStartScreen(BorderPane deck) {
        deck.setCenter(startScreen.getRoot());
        deck.setBottom(toolBar); // Ensure toolbar stays visible at the bottom
    }

    // Show the ActivityScreen (by removing others and showing ActivityScreen)
    public void showActivityScreen(BorderPane deck) {
        deck.setCenter(activityScreen.getRoot());
        deck.setBottom(toolBar); // Ensure toolbar stays visible at the bottom
    }

    // Show the HistoryScreen (by removing others and showing HistoryScreen)
    private void showHistoryScreen(BorderPane deck) {
        logger.info("Show history screen");
        logger.info(activityDao.toString());

        deck.setCenter(historyScreen.getRoot());
        deck.setBottom(toolBar); // Ensure toolbar stays visible at the bottom
    }
}
