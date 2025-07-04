package info.matthewryan.workoutlogger.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ExerciseProgressScreen {

    public BorderPane getRoot() {
        BorderPane root = new BorderPane();
        root.setCenter(new javafx.scene.control.Label("Exercise Progress Screen (placeholder)"));
        return root;
    }

    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRoot(), 400, 600);
        primaryStage.setTitle("Exercise Progress");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
