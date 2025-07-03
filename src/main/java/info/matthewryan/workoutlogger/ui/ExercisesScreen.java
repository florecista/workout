package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class ExercisesScreen {

    private ExerciseDao exerciseDao;

    public ExercisesScreen(ExerciseDao exerciseDao) {
        this.exerciseDao = exerciseDao;
    }

    public BorderPane getRoot() {
        BorderPane root = new BorderPane();

        // Create a ListView to display the exercises
        ListView<String> listView = new ListView<>();
        List<String> exercises = exerciseDao.getAllExercises();
        listView.getItems().addAll(exercises);

        root.setCenter(listView);

        return root;
    }

    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRoot(), 400, 600);
        primaryStage.setTitle("Exercises");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
