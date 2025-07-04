package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import em.libs.jfxcalendar.JFXCalendar;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

public class HistoryScreen {

    private static final Logger logger = LoggerFactory.getLogger(HistoryScreen.class);

    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;

    public HistoryScreen(ActivityDao activityDao, ExerciseDao exerciseDao, CustomToolBar toolBar) {
        this.activityDao = activityDao;
        this.exerciseDao = exerciseDao;
        this.toolBar = toolBar;  // Store the passed ToolBar instance
    }

    public BorderPane getRoot() {

        if (activityDao == null)
            logger.info("activityDao is null");
        else
            logger.info("activityDao is not null");

        BorderPane calendarPanel = createCalendarPickerPanel();
        BorderPane historyPanel = createHistoryPanel();

        BorderPane root = new BorderPane();
        root.setTop(calendarPanel);
        root.setBottom(historyPanel);

        //calendarPanel.prefHeightProperty().bind(root.heightProperty().multiply(0.20));  // 50% of screen height
        historyPanel.prefHeightProperty().bind(root.heightProperty().multiply(0.45));   // 50% of screen height


        return root;
    }

    private BorderPane createCalendarPickerPanel() {
        BorderPane panel = new BorderPane();

        JFXCalendar<ActivityRecord> calendar = new JFXCalendar<>();
        calendar.setMultipleSelection(true);  // Allow multiple date selection if needed

        calendar.selectedDateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isPresent()) {
                LocalDate selectedDate = newValue.get();
                // Filter activities by the selected date
                List<ActivityRecord> filteredActivities = activityDao.getActivitiesByDate(selectedDate);
                updateTable(filteredActivities);
            }
        });

        StackPane calendarPane = new StackPane(calendar);
        calendarPane.setAlignment(Pos.CENTER);
        //calendarPane.setPadding(new Insets(10));

        panel.setCenter(calendarPane);
        return panel;
    }

    // Create the History Panel with TableView
    private BorderPane createHistoryPanel() {
        BorderPane historyPanel = new BorderPane();

        // Create the TableView to display the history of activities
        TableView<ActivityRecord> tableView = new TableView<>();

        // Define columns
        TableColumn<ActivityRecord, String> activityColumn = new TableColumn<>("Exercise");
        activityColumn.setCellValueFactory(param -> {
            // Use the exerciseId to fetch the exercise name from the database
            ActivityRecord record = param.getValue();
            String exerciseName = exerciseDao.getExerciseNameById(record.getExerciseId());  // Fetch exercise name by ID
            return new SimpleStringProperty(exerciseName);  // Set the exercise name as the cell value
        });

        TableColumn<ActivityRecord, Integer> repsColumn = new TableColumn<>("Reps");
        repsColumn.setCellValueFactory(new PropertyValueFactory<>("reps"));

        TableColumn<ActivityRecord, Double> weightColumn = new TableColumn<>("Weight (kg)");
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        TableColumn<ActivityRecord, Long> timestampColumn = new TableColumn<>("Timestamp");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // Format timestamp to readable date
        timestampColumn.setCellFactory(col -> {
            return new javafx.scene.control.TableCell<ActivityRecord, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        setText(sdf.format(item)); // Format the Long timestamp as a String
                    }
                }
            };
        });

        // Add columns to the TableView
        tableView.getColumns().addAll(activityColumn, repsColumn, weightColumn, timestampColumn);

        // Fetch activities from the database, ordered by timestamp descending
        List<ActivityRecord> activities = activityDao.getAllActivitiesOrderedByTimestamp();
        tableView.getItems().setAll(activities);  // Populate the table with activity records
        historyPanel.setCenter(tableView);

        return historyPanel;
    }

    private void updateTable(List<ActivityRecord> activities) {
        TableView<ActivityRecord> tableView = new TableView<>();
        tableView.getItems().setAll(activities);  // Populate the table with filtered activity records
    }

    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRoot(), 400, 600);
        primaryStage.setTitle("Activity History");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
