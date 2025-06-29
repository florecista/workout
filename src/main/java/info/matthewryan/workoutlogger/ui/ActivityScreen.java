package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ActivityScreen {

    // Create a logger instance
    private static final Logger logger = LoggerFactory.getLogger(ActivityScreen.class);

    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;

    // Constructor accepts ActivityDao, ExerciseDao, and ToolBar
    public ActivityScreen(ActivityDao activityDao, ExerciseDao exerciseDao, CustomToolBar toolBar) {
        this.activityDao = activityDao;
        this.exerciseDao = exerciseDao;
        this.toolBar = toolBar;
    }

    // Implement getRoot to return the screen's root layout
    public BorderPane getRoot() {

        logger.info("Creating Activity Screen components");

        // Create the form elements
        Text exerciseLabel = new Text("Exercise:");
        ComboBox<String> exerciseComboBox = new ComboBox<>();

        // Fetch exercises from the database using ExerciseDao
        List<String> exercises = exerciseDao.getAllExercises(); // Get all exercises from the database
        exerciseComboBox.getItems().addAll(exercises); // Add the exercises to the ComboBox

        // Set the default selected exercise (optional, if you have a default exercise)
        if (!exercises.isEmpty()) {
            exerciseComboBox.getSelectionModel().selectFirst();
        }

        Text weightLabel = new Text("Weight (kg):");
        TextField weightField = new TextField();

        Text repsLabel = new Text("Reps:");
        TextField repsField = new TextField();

        // Create Save and Clear buttons
        Button saveButton = new Button("Save");
        Button clearButton = new Button("Clear");

        // Handle Save button click
        saveButton.setOnAction(event -> {
            // Collect the data from the form
            String exercise = exerciseComboBox.getValue();
            String weightText = weightField.getText();
            String repsText = repsField.getText();

            // Validate inputs (e.g., check if weight and reps are numbers)
            if (weightText.isEmpty() || repsText.isEmpty()) {
                // You might want to show a message here to inform the user to fill all fields
                System.out.println("Please fill in all fields.");
                return;
            }

            try {
                double weight = Double.parseDouble(weightText);
                int reps = Integer.parseInt(repsText);

                // Create a new ActivityRecord and insert it into the database
                ActivityRecord record = new ActivityRecord(exercise, reps, weight, System.currentTimeMillis());
                activityDao.insertActivity(record);  // Insert the activity into the database

                // Inform the user and clear the fields
                System.out.println("Saved activity: " + exercise + " " + weight + " kg x " + reps + " reps");
                clearButton.fire(); // Clear the fields after save

            } catch (NumberFormatException e) {
                // Handle invalid number format (e.g., user enters text instead of a number)
                System.out.println("Invalid input for weight or reps. Please enter valid numbers.");
            }
        });

        // Handle Clear button click
        clearButton.setOnAction(event -> {
            weightField.clear();
            repsField.clear();
        });

        // Set up the form layout using GridPane for better alignment
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        // Add padding to the GridPane
        grid.setPadding(new javafx.geometry.Insets(20));  // Set padding for the whole grid

        // Add labels and components to the GridPane
        grid.add(exerciseLabel, 0, 0);
        grid.add(exerciseComboBox, 1, 0);

        grid.add(weightLabel, 0, 1);
        grid.add(weightField, 1, 1);

        grid.add(repsLabel, 0, 2);
        grid.add(repsField, 1, 2);

        // Add Save and Clear buttons at the bottom of the form
        grid.add(saveButton, 0, 3);
        grid.add(clearButton, 1, 3);

        // Set up the root layout and add the form components
        BorderPane root = new BorderPane();
        root.setCenter(grid);

        // Create the toolbar and set it at the bottom
        root.setBottom(toolBar);

        return root;  // Return the root layout (BorderPane)
    }
}
