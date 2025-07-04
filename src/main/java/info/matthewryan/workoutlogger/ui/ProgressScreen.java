package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.Exercise;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class ProgressScreen {

    private ExerciseDao exerciseDao;
    private ScreenStartup screenStartup;

    public ProgressScreen(ExerciseDao exerciseDao, ScreenStartup screenStartup) {
        this.exerciseDao = exerciseDao;
        this.screenStartup = screenStartup;
    }

    public BorderPane getRoot() {
        BorderPane root = new BorderPane();

        // Create a ListView to display exercises
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

        // Add double-click event to navigate to ExerciseProgressScreen
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click
                Exercise selectedExercise = listView.getSelectionModel().getSelectedItem();
                if (selectedExercise != null) {
                    screenStartup.showExerciseProgressScreen(selectedExercise.getName());
                }
            }
        });

        root.setCenter(listView);

        return root;
    }

    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRoot(), 400, 600);
        primaryStage.setTitle("Progress");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
