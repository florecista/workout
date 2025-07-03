package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import em.libs.jfxcalendar.JFXCalendar;
import em.libs.jfxcalendar.JFXCalendarData;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

        BorderPane calendarPanel = createCalendarPickerPanel();
        BorderPane historyPanel = createHistoryPanel();

        BorderPane root = new BorderPane();
        root.setTop(calendarPanel);
        root.setBottom(historyPanel);

        return root;  // Return the root layout for use in the "deck" or cards UI
    }

    // Create the Calendar Picker Panel
    private BorderPane createCalendarPickerPanel() {
        BorderPane panel = new BorderPane();

        // Create JFXCalendar for the top half
        JFXCalendar<ActivityRecord> calendar = new JFXCalendar<>();
        calendar.setMultipleSelection(true);  // Allow multiple date selection if needed

        // Add listener to handle date selection
        calendar.selectedDateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isPresent()) {
                LocalDate selectedDate = newValue.get();
                // Filter activities by the selected date
                List<ActivityRecord> filteredActivities = activityDao.getActivitiesByDate(selectedDate);
                updateTable(filteredActivities);
            }
        });

        // Add calendar to the panel
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
        TableColumn<ActivityRecord, String> activityColumn = new TableColumn<>("Activity");
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activity"));

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

        // Set the passed toolBar instead of creating a new one
        historyPanel.setBottom(toolBar);

        return historyPanel;
    }

    // Method to update the TableView with filtered activities
    private void updateTable(List<ActivityRecord> activities) {
        TableView<ActivityRecord> tableView = new TableView<>();
        tableView.getItems().setAll(activities);  // Populate the table with filtered activity records
    }

    // Method to start the HistoryScreen (open as a new window if needed)
    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRoot(), 400, 600);
        primaryStage.setTitle("Activity History");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
