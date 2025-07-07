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

    public ExerciseDetailScreen(ExerciseDao exerciseDao, ScreenStartup screenStartup, String existingExerciseName) {
        this.exerciseDao = exerciseDao;
        this.screenStartup = screenStartup; // Initialize the ScreenStartup reference
        this.existingExerciseName = existingExerciseName;
    }

    public BorderPane getRoot() {
        BorderPane root = new BorderPane();

        Label exerciseNameLabel = new Label("Exercise Name:");
        TextField exerciseNameField = new TextField();
        if (existingExerciseName != null) {
            exerciseNameField.setText(existingExerciseName);
        }

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            String exerciseName = exerciseNameField.getText();
            if (!exerciseName.isEmpty()) {
                exerciseDao.insertExercise(exerciseName, false);
                goBackToExercisesScreen();
            }
        });

        cancelButton.setOnAction(e -> goBackToExercisesScreen());

        VBox formLayout = new VBox(10, exerciseNameLabel, exerciseNameField, saveButton, cancelButton);
        formLayout.setPadding(new Insets(20));

        root.setCenter(formLayout);

        return root;
    }

    private void goBackToExercisesScreen() {
        screenStartup.showExercisesScreen();  // Call the method in ScreenStartup to show ExercisesScreen
    }

    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRoot(), 400, 600);
        primaryStage.setTitle(existingExerciseName != null ? "Edit Exercise" : "New Exercise");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
