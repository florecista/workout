package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.Exercise;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

public class ExercisesScreen {

    private ExerciseDao exerciseDao;
    private ScreenStartup screenStartup;

    public ExercisesScreen(ExerciseDao exerciseDao, ScreenStartup screenStartup) {
        this.exerciseDao = exerciseDao;
        this.screenStartup = screenStartup;
    }

    public BorderPane getRoot() {
        BorderPane root = new BorderPane();

        // Create the top toolbar with a "+" button
        HBox topToolbar = createTopToolbar();

        // Create a ListView to display the exercises
        ListView<Exercise> listView = new ListView<>();
        List<Exercise> exercises = exerciseDao.getAllExercises();
        listView.getItems().addAll(exercises);

        // Set up a cell factory to display exercise names in the ListView
        listView.setCellFactory(param -> new ListCell<Exercise>() {
            @Override
            protected void updateItem(Exercise item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getName());  // Display the exercise name
                }
            }
        });

        // Add double-click event to navigate to ExerciseDetailScreen
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click
                Exercise selectedExercise = listView.getSelectionModel().getSelectedItem();
                if (selectedExercise != null) {
                    screenStartup.showExerciseDetailScreen(selectedExercise.getName());
                }
            }
        });

        // Set the toolbar at the top and ListView in the center
        root.setTop(topToolbar);
        root.setCenter(listView);

        return root;
    }

    private HBox createTopToolbar() {
        HBox toolbar = new HBox();
        toolbar.setSpacing(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT); // Align the button to the right

        // Create the "+" button
        Button btnAddExercise = new Button("+");
        btnAddExercise.setStyle("-fx-font-size: 16px; -fx-padding: 5 10;");

        // Add action to the "+" button
        btnAddExercise.setOnAction(e -> {
            // Navigate to the ExerciseDetailScreen for creating a new exercise
            screenStartup.showExerciseDetailScreen(null); // Pass null for new exercise
        });

        toolbar.getChildren().add(btnAddExercise); // Add the button to the toolbar
        return toolbar;
    }

    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRoot(), 400, 600);
        primaryStage.setTitle("Exercises");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
