package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import info.matthewryan.workoutlogger.model.ActivityRecord;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ActivityScreen {

    private static final Logger logger = LoggerFactory.getLogger(ActivityScreen.class);

    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;
    private NumberAxis xAxis; // X-axis for the graph
    private NumberAxis yAxis; // Y-axis for the graph

    private BorderPane graphPanel;  // Declare the graph panel here
    private LineChart<Number, Number> lineChart;

    // Constructor accepts ActivityDao, ExerciseDao, and ToolBar
    public ActivityScreen(ActivityDao activityDao, ExerciseDao exerciseDao, CustomToolBar toolBar) {
        this.activityDao = activityDao;
        this.exerciseDao = exerciseDao;
        this.toolBar = toolBar;
        this.xAxis = new NumberAxis();
        this.yAxis = new NumberAxis();
    }

    // Implement getRoot to return the screen's root layout
    public BorderPane getRoot() {
        // Create the main BorderPane for ActivityScreen layout
        BorderPane root = new BorderPane();

        // Create the graphPanel for the top part (BorderLayout.NORTH)
        graphPanel = createGraphPanel();  // Initialize the graphPanel here

        // Create the formPanel for the bottom part (BorderLayout.SOUTH)
        BorderPane formPanel = createFormPanel();

        // Set the graphPanel at the top (BorderLayout.NORTH) and the formPanel at the bottom (BorderLayout.SOUTH)
        root.setTop(graphPanel);
        root.setBottom(formPanel);

        // Set a fixed height for the panels (50% each)
        graphPanel.prefHeightProperty().bind(root.heightProperty().multiply(0.5));  // 50% of screen height
        formPanel.prefHeightProperty().bind(root.heightProperty().multiply(0.5));   // 50% of screen height

        return root;  // Return the root layout (BorderPane)
    }

    // Method to create the graph panel (at BorderLayout.NORTH)
    private BorderPane createGraphPanel() {
        BorderPane graphPanel = new BorderPane();
        graphPanel.setStyle("-fx-background-color: lightgray;");  // Example style for graph panel

        // Initialize the LineChart and set it to the center of graphPanel
        lineChart = createGraphChart();  // Now we store the reference to the LineChart

        // Add the initial placeholder text
        Text graphPlaceholderText = new Text("Select an Exercise to view data.");
        graphPanel.setCenter(graphPlaceholderText);  // Set the placeholder text initially

        return graphPanel;
    }

    // Create a LineChart to display the volume data
    private LineChart<Number, Number> createGraphChart() {
        // X Axis represents the date
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Date");

        // Convert timestamps to readable dates
        xAxis.setTickLabelFormatter(new javafx.scene.chart.NumberAxis.DefaultFormatter(xAxis) {
            @Override
            public String toString(Number object) {
                // Convert the timestamp to a date
                long timestamp = object.longValue();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                return sdf.format(new java.util.Date(timestamp));
            }
        });

        // Y Axis represents the total volume (weight)
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Volume (kg)");

        // Create the LineChart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Exercise Volume by Date");

        return lineChart;
    }

    // Method to create the form panel (at BorderLayout.SOUTH)
    private BorderPane createFormPanel() {
        BorderPane formPanel = new BorderPane();
        formPanel.setStyle("-fx-background-color: white;");  // Example style for form panel

        // Create the form elements
        Text exerciseLabel = new Text("Exercise:");
        ComboBox<String> exerciseComboBox = new ComboBox<>();
        List<String> exercises = exerciseDao.getAllExercises();  // Get exercises from the database

        Collections.sort(exercises);  // This will sort the exercises in ascending alphabetical order

        exerciseComboBox.getItems().add("Select");  // Add "Select" as the first option
        exerciseComboBox.getItems().addAll(exercises);

        exerciseComboBox.getSelectionModel().select("Select");  // Select the "Select" option by default

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

        // Ensure the GridPane's content is laid out properly
        formPanel.setCenter(grid);

        // Add some padding to the formPanel
        formPanel.setPadding(new javafx.geometry.Insets(10));

        // Optional: Add some margin to the form panel (if needed)
        formPanel.setMinHeight(Region.USE_PREF_SIZE);

        // Add functionality for exerciseComboBox change to update graph
        exerciseComboBox.setOnAction(e -> {
            String selectedExercise = exerciseComboBox.getSelectionModel().getSelectedItem();
            if (!"Select".equals(selectedExercise)) {
                // Update the graph with data for the selected exercise
                updateGraphForExercise(selectedExercise);
            } else {
                // Show placeholder text when "Select" is chosen
                lineChart.getData().clear();  // Clear previous data
                Text graphPlaceholderText = new Text("Select an Exercise to view data.");
                graphPanel.setCenter(graphPlaceholderText);  // Set the placeholder text
            }
        });

        return formPanel;
    }

    // Method to update the graph based on the selected exercise
    // Method to update the graph based on the selected exercise
    private void updateGraphForExercise(String exercise) {
        // Fetch data for the selected exercise from the database
        List<ActivityRecord> activityRecords = activityDao.getActivityDataByExercise(exercise);

        // Log the data to see if we're getting the correct records
        logger.info("Fetched {} records for exercise: {}", activityRecords.size(), exercise);

        if (!activityRecords.isEmpty()) {
            // Initialize variables for the earliest and latest timestamps
            long earliestTimestamp = Long.MAX_VALUE;
            long latestTimestamp = Long.MIN_VALUE;

            // Loop through all activity records to find the earliest and latest timestamps
            for (ActivityRecord record : activityRecords) {
                long timestamp = record.getTimestamp();
                if (timestamp < earliestTimestamp) {
                    earliestTimestamp = timestamp;
                }
                if (timestamp > latestTimestamp) {
                    latestTimestamp = timestamp;
                }
            }

            // Log the first and last date
            logger.info("First activity date: {}", new Date(earliestTimestamp));
            logger.info("Last activity date: {}", new Date(latestTimestamp));

            // Set the time range for the X-axis to start from the first date and end on the last date
            xAxis.setLowerBound(earliestTimestamp);
            xAxis.setUpperBound(latestTimestamp);

            // Set tick unit to months (adjust as needed)
            xAxis.setTickUnit(2592000000L);  // 30 days in milliseconds

            // Create a series for the graph
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(exercise);

            // Process the data to populate the graph
            for (ActivityRecord record : activityRecords) {
                double volume = record.getReps() * record.getWeight();
                // Log the data for debugging purposes
                logger.info("Adding data point: Timestamp = {}, Volume = {}", record.getTimestamp(), volume);

                // Add the data point to the series
                series.getData().add(new XYChart.Data<>(record.getTimestamp(), volume));
            }

            // Now update the graphPanel with either the new graph or a placeholder message
            if (series.getData().isEmpty()) {
                // If no data is available, show the "No data" message
                logger.info("No data found for the selected exercise.");
                Text graphPlaceholderText = new Text("No data available for this exercise.");
                graphPanel.setCenter(graphPlaceholderText);
            } else {
                // Clear previous chart data and add the new data series
                lineChart.getData().clear();
                lineChart.getData().add(series);

                // Ensure the chart is displayed in the center of the graphPanel
                graphPanel.setCenter(lineChart);
            }
        } else {
            // If no data is available, show the "No data" message
            logger.info("No data found for the selected exercise.");
            Text graphPlaceholderText = new Text("No data available for this exercise.");
            graphPanel.setCenter(graphPlaceholderText);
        }
    }

}
