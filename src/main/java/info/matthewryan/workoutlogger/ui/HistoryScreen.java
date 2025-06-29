package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.List;

public class HistoryScreen {

    private static final Logger logger = LoggerFactory.getLogger(HistoryScreen.class);

    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;

    // Constructor accepts the ActivityDao, ExerciseDao, and ToolBar to interact with the database
    public HistoryScreen(ActivityDao activityDao, ExerciseDao exerciseDao, CustomToolBar toolBar) {
        this.activityDao = activityDao;
        this.exerciseDao = exerciseDao;
        this.toolBar = toolBar;  // Store the passed ToolBar instance
    }

    // Method to return the root layout (BorderPane)
    public BorderPane getRoot() {

        if (activityDao == null)
            logger.info("activityDao is null");
        else
            logger.info("activityDao is not null");

        // Create the TableView to display the history of activities
        TableView<ActivityRecord> tableView = new TableView<>();

        // Define columns
        TableColumn<ActivityRecord, String> activityColumn = new TableColumn<>("Activity");
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activity"));

        TableColumn<ActivityRecord, Integer> repsColumn = new TableColumn<>("Reps");
        repsColumn.setCellValueFactory(new PropertyValueFactory<>("reps"));

        TableColumn<ActivityRecord, Double> weightColumn = new TableColumn<>("Weight (kg)");
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        TableColumn<ActivityRecord, String> timestampColumn = new TableColumn<>("Timestamp");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // Format timestamp to readable date
        timestampColumn.setCellFactory(col -> {
            return new javafx.scene.control.TableCell<ActivityRecord, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        setText(sdf.format(getTableRow().getItem().getTimestamp()));
                    }
                }
            };
        });

        // Add columns to the TableView
        tableView.getColumns().addAll(activityColumn, repsColumn, weightColumn, timestampColumn);

        // Fetch activities from the database, ordered by timestamp descending
        List<ActivityRecord> activities = activityDao.getAllActivitiesOrderedByTimestamp();
        tableView.getItems().setAll(activities);  // Populate the table with activity records

        // Set up the layout
        BorderPane root = new BorderPane();
        root.setCenter(tableView);

        // Set the passed toolBar instead of creating a new one
        root.setBottom(toolBar);

        return root;  // Return the root layout for use in the "deck" or cards UI
    }

    // Method to start the HistoryScreen (open as a new window if needed)
    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRoot(), 400, 600);
        primaryStage.setTitle("Activity History");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
