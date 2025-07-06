package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import info.matthewryan.workoutlogger.persistence.SessionDao;
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

    private SessionDao sessionDao;
    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;

    private WorkoutsScreen workoutsScreen;
    private ActivityScreen activityScreen;
    private HistoryScreen historyScreen;
    private ExercisesScreen exercisesScreen;
    private ProgressScreen progressScreen;

    private CsvImporter csvImporter;

    private boolean loadExistingData = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        DatabaseConnection databaseConnection = new DatabaseConnection();

        sessionDao = new SessionDao(databaseConnection.getConnection());
        activityDao = new ActivityDao(databaseConnection.getConnection());
        exerciseDao = new ExerciseDao(databaseConnection.getConnection());

        sessionDao.createSessionTable();

        exerciseDao.createExerciseTable();
        activityDao.createActivityTable();
        exerciseDao.createVolumeGroupTable();
        preloadVolumeGroups();
        preloadDefaultExercises();

        csvImporter = new CsvImporter(exerciseDao, activityDao);

        if (loadExistingData) {
            loadExistingDataFromCSV();
        }

        List<ActivityRecord> activities = activityDao.getAllActivitiesOrderedByTimestamp();
        logger.info("Loaded activities: {}", activities.size());

        // Initialize screens
        toolBar = new CustomToolBar(primaryStage, activityDao, exerciseDao);
        activityScreen = new ActivityScreen(activityDao, exerciseDao, toolBar);
        workoutsScreen = new WorkoutsScreen(sessionDao, toolBar, this);
        historyScreen = new HistoryScreen(activityDao, exerciseDao, toolBar);
        exercisesScreen = new ExercisesScreen(exerciseDao, this);
        progressScreen = new ProgressScreen(exerciseDao, this);  // Initialize ProgressScreen


        // Set up a BorderPane to hold all the screens (Deck of screens)
        BorderPane deck = new BorderPane();
        deck.setCenter(workoutsScreen.getRoot()); // Initially show the Start Screen

        // ToolBar Actions
        toolBar.setOnWorkoutsAction(() -> showStartScreen(deck));
        toolBar.setOnHistoryAction(() -> showHistoryScreen(deck));
        toolBar.setOnExercisesAction(() -> showExercisesScreen());
        toolBar.setOnProgressAction(() -> showProgressScreen());

        // Set up the layout with the toolbar and the current screen
        deck.setBottom(toolBar); // Attach the toolbar to the bottom of BorderPane

        Scene scene = new Scene(deck, 400, 600);
        primaryStage.setTitle("Workout Logger");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showStartScreen(BorderPane deck) {
        deck.setCenter(workoutsScreen.getRoot());
        deck.setBottom(toolBar);
    }

    void showProgressScreen() {
        BorderPane deck = (BorderPane) toolBar.getScene().getRoot();
        deck.setCenter(progressScreen.getRoot());
        deck.setBottom(toolBar);
    }

    void showExerciseProgressScreen(String exerciseName) {
        ExerciseProgressScreen progressScreen = new ExerciseProgressScreen();
        BorderPane deck = (BorderPane) toolBar.getScene().getRoot();
        deck.setCenter(progressScreen.getRoot());
    }

    public void showActivityScreen(BorderPane deck) {
        deck.setCenter(activityScreen.getRoot());
        deck.setBottom(toolBar);
    }

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
        deck.setCenter(detailScreen.getRoot());
    }

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
