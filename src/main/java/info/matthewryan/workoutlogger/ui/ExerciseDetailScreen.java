package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ExerciseDetailScreen {

    private ExerciseDao exerciseDao;
    private String existingExerciseName;
    private ScreenStartup screenStartup;

    // Constructor accepts ExerciseDao and optionally an existing exercise name for editing
    public ExerciseDetailScreen(ExerciseDao exerciseDao, ScreenStartup screenStartup, String existingExerciseName) {
        this.exerciseDao = exerciseDao;
        this.screenStartup = screenStartup; // Initialize the ScreenStartup reference
        this.existingExerciseName = existingExerciseName;
    }

    public BorderPane getRoot() {
        BorderPane root = new BorderPane();

        // Create a Label and TextField for the exercise name
        Label exerciseNameLabel = new Label("Exercise Name:");
        TextField exerciseNameField = new TextField();
        if (existingExerciseName != null) {
            exerciseNameField.setText(existingExerciseName);
        }

        // Create Save and Cancel buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        // Save button action
        saveButton.setOnAction(e -> {
            String exerciseName = exerciseNameField.getText();
            if (!exerciseName.isEmpty()) {
                exerciseDao.insertExercise(exerciseName);
                goBackToExercisesScreen();
            }
        });

        // Cancel button action
        cancelButton.setOnAction(e -> goBackToExercisesScreen());

        // Arrange the components in the form
        VBox formLayout = new VBox(10, exerciseNameLabel, exerciseNameField, saveButton, cancelButton);
        formLayout.setPadding(new Insets(20));

        // Set the form in the center of the screen
        root.setCenter(formLayout);

        return root;
    }

    private void goBackToExercisesScreen() {
        // Navigate back to ExercisesScreen using ScreenStartup
        screenStartup.showExercisesScreen();  // Call the method in ScreenStartup to show ExercisesScreen
    }

    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRoot(), 400, 600);
        primaryStage.setTitle(existingExerciseName != null ? "Edit Exercise" : "New Exercise");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
