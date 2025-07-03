package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import info.matthewryan.workoutlogger.utils.CsvImporter;
import info.matthewryan.workoutlogger.utils.DatabaseConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ScreenStartup extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ScreenStartup.class);

    private final String[] defaultExercises = {
            "Abdominal Twists", "Abmat Crunches", "Back Extension", "Back Squat",
            "Barbell Chest Squat", "Bench Press", "Bicep Curl", "Bulgarian Split Squat",
            "Cable Side Deltoid Pulls", "Deadlift", "Dumbell Bench Press", "Dumbell Chin Row",
            "Dumbell Side Raises", "Dumbell Shoulder Press", "Lat Pulldown", "Leg Press",
            "Parallel Dips", "Pull-Up", "Push-Up", "Rope Pulls To Face", "Seated Row", "Trapbar Deadlift", "Triceps Extension"
    };

    private final String[] volumeGroups = {
            "Arms", "Back", "Biceps", "Chest", "Hamstrings", "Legs", "Quads", "Triceps"
    };

    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;

    private StartScreen startScreen;
    private ActivityScreen activityScreen;
    private HistoryScreen historyScreen;
    private ExercisesScreen exercisesScreen;

    private CsvImporter csvImporter;

    private boolean loadExistingData = true; // Set to true if you want to load existing data

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
        exerciseDao.createVolumeGroupTable(); // Create Volume Group table
        preloadVolumeGroups(); // Preload volume groups
        preloadDefaultExercises(); // Preload default exercises

        csvImporter = new CsvImporter(activityDao);

        // If loadExistingData is true, load the existing data from the CSV file
        if (loadExistingData) {
            loadExistingDataFromCSV();
        }

        List<ActivityRecord> activities = activityDao.getAllActivitiesOrderedByTimestamp();
        logger.info("Loaded activities: {}", activities.size());

        // Initialize screens
        toolBar = new CustomToolBar(primaryStage, activityDao, exerciseDao);
        activityScreen = new ActivityScreen(activityDao, exerciseDao, toolBar);
        startScreen = new StartScreen(activityDao, exerciseDao, toolBar, this);
        historyScreen = new HistoryScreen(activityDao, exerciseDao, toolBar);
        exercisesScreen = new ExercisesScreen(exerciseDao, this);

        // Set up a BorderPane to hold all the screens (Deck of screens)
        BorderPane deck = new BorderPane();
        deck.setCenter(startScreen.getRoot()); // Initially show the Start Screen

        // ToolBar Actions
        toolBar.setOnRoutinesAction(() -> showStartScreen(deck));
        toolBar.setOnHistoryAction(() -> showHistoryScreen(deck));
        toolBar.setOnExercisesAction(() -> showExercisesScreen());

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
        deck.setBottom(toolBar);
    }

    // Show the ActivityScreen (by removing others and showing ActivityScreen)
    public void showActivityScreen(BorderPane deck) {
        deck.setCenter(activityScreen.getRoot());
        deck.setBottom(toolBar);
    }

    // Show the HistoryScreen (by removing others and showing HistoryScreen)
    private void showHistoryScreen(BorderPane deck) {
        deck.setCenter(historyScreen.getRoot());
        deck.setBottom(toolBar);
    }

    void showExercisesScreen() {
        BorderPane deck = (BorderPane) toolBar.getScene().getRoot();
        deck.setCenter(exercisesScreen.getRoot());
        deck.setBottom(toolBar);
    }

    public void showExerciseDetailScreen(String exerciseName) {
        ExerciseDetailScreen detailScreen = new ExerciseDetailScreen(exerciseDao, this, exerciseName);
        BorderPane deck = (BorderPane) toolBar.getScene().getRoot();
        deck.setCenter(detailScreen.getRoot());  // Navigate to ExerciseDetailScreen
    }

    // Method to load existing data from a CSV file
    private void loadExistingDataFromCSV() {
        String filePath = "/data.csv";
        int lineCounter = 0;  // Counter to track lines

        try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
            // Handle case where the file isn't found
            if (inputStream == null) {
                throw new FileNotFoundException("File not found: " + filePath);
            }

            // Read and process each line of the file
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null && lineCounter < 1000) {  // Limit to 100 lines
                    lineCounter++;  // Increment counter for each line

                    // Skip the header line
                    if (lineCounter == 1) {
                        continue;  // Skip header
                    }

                    try {
                        // Assuming CsvImporter processes each line of the CSV
                        csvImporter.importCsvLine(line);  // Process the line
                        //logger.info("Processed line {}: {}", lineCounter, line);
                    } catch (Exception e) {
                        logger.error("Error processing line {}: {}", lineCounter, e.getMessage());
                        break;  // Stop processing on the first exception
                    }
                }
            }
        } catch (IOException e) {
            // Handle exceptions for file reading
            logger.error("Error reading the file: {}", e.getMessage());
        }
    }

    private void preloadVolumeGroups() {
        // Preload the volume group reference data
        for (String group : volumeGroups) {
            exerciseDao.insertVolumeGroup(group);
        }
    }

    private void preloadDefaultExercises() {
        for (String exercise : defaultExercises) {
            exerciseDao.insertExercise(exercise);
        }
    }
}
