package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SessionActivityLogScreen {

    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;
    private int currentSessionId;
    private ScreenStartup screenStartup;

    public SessionActivityLogScreen(ActivityDao activityDao, ExerciseDao exerciseDao, CustomToolBar toolBar, int currentSessionId, ScreenStartup screenStartup) {
        this.activityDao = activityDao;
        this.exerciseDao = exerciseDao;
        this.toolBar = toolBar;
        this.currentSessionId = currentSessionId;
        this.screenStartup = screenStartup;
    }

    public BorderPane getRoot() {
        // Create a new BorderPane for the screen
        BorderPane root = new BorderPane();

        // Create the toolbar with a Back button
        HBox toolbar = new HBox(10);
        Button btnBack = new Button("<<");
        btnBack.setStyle("-fx-font-size: 14px; -fx-background-color: lightgray;");
        btnBack.setOnMouseClicked(event -> {
            // Handle the back button click, go back to the ActivityScreen
            screenStartup.showActivityScreen((BorderPane) toolBar.getScene().getRoot());
        });
        toolbar.getChildren().add(btnBack);
        root.setTop(toolbar);  // Set the toolbar at the top

        // Fetch the activities for the current session
        List<ActivityRecord> activities = activityDao.getActivitiesForSession(currentSessionId);

        // Create a TableView for the activity records
        TableView<ActivityRecord> tableView = new TableView<>();
        tableView.setItems(javafx.collections.FXCollections.observableArrayList(activities));

        // Set up the columns for the TableView

        // Set Column: Activity list index + 1
        TableColumn<ActivityRecord, String> setColumn = new TableColumn<>("Set");
        setColumn.setCellValueFactory((cellData) -> {
            int index = tableView.getItems().indexOf(cellData.getValue()) + 1;  // List index + 1
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(index));
        });

        // Exercise Column: Fetch the exercise name using the activityRecord.exerciseId
        TableColumn<ActivityRecord, String> exerciseColumn = new TableColumn<>("Exercise");
        exerciseColumn.setCellValueFactory(cellData -> {
            String exerciseName = exerciseDao.getExerciseNameById(cellData.getValue().getExerciseId());
            return new javafx.beans.property.SimpleStringProperty(exerciseName);
        });

        // Weight Column
        TableColumn<ActivityRecord, String> unitColumn = new TableColumn<>("Weight");
        unitColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(Double.toString(cellData.getValue().getWeight())));

        // Reps Column
        TableColumn<ActivityRecord, Integer> repsColumn = new TableColumn<>("Reps");
        repsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getReps()));

        // Time Column (formerly Timestamp)
        TableColumn<ActivityRecord, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(cellData -> {
            // Format the timestamp into a readable time format (example: "HH:mm:ss")
            long timestamp = cellData.getValue().getTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            return new javafx.beans.property.SimpleStringProperty(sdf.format(new Date(timestamp)));
        });

        // Add columns to the TableView
        tableView.getColumns().addAll(setColumn, exerciseColumn, unitColumn, repsColumn, timeColumn);


        // Add the TableView to the center of the root BorderPane
        root.setCenter(tableView);

        return root;
    }
}
