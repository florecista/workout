package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import info.matthewryan.workoutlogger.model.ActivityRecord;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.DataPointTooltip;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.dataset.spi.DefaultErrorDataSet;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityScreen {

    private static final Logger logger = LoggerFactory.getLogger(ActivityScreen.class);

    private ActivityDao activityDao;
    private ExerciseDao exerciseDao;
    private CustomToolBar toolBar;

    private BorderPane graphPanel;  // Declare the graph panel here
    private XYChart chart;  // Use Chart FX's XYChart

    // Declare xAxis1 globally so we can set it in the updateGraphForExercise method
    private DefaultNumericAxis xAxis1;

    // Constructor accepts ActivityDao, ExerciseDao, and ToolBar
    public ActivityScreen(ActivityDao activityDao, ExerciseDao exerciseDao, CustomToolBar toolBar) {
        this.activityDao = activityDao;
        this.exerciseDao = exerciseDao;
        this.toolBar = toolBar;
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

        // Initialize the Chart FX XYChart and set it to the center of graphPanel
        chart = createGraphChart();  // Now we store the reference to the XYChart

        // Add the initial placeholder text
        Text graphPlaceholderText = new Text("Select an Exercise to view data.");
        graphPanel.setCenter(graphPlaceholderText);  // Set the placeholder text initially

        return graphPanel;
    }

    // Create a Chart FX XYChart to display the volume data
    private XYChart createGraphChart() {

        // Initialize the x-axis globally so we can modify it later
        xAxis1 = new DefaultNumericAxis("Date", "iso");
        xAxis1.setOverlapPolicy(io.fair_acc.chartfx.axes.AxisLabelOverlapPolicy.SKIP_ALT);
        xAxis1.setAutoRangeRounding(false);
        xAxis1.setTimeAxis(true);
        DefaultNumericAxis yAxis1 = new DefaultNumericAxis("Total Volume", "kg");

        xAxis1.set("Date", "iso");

        XYChart chart = new XYChart(xAxis1, yAxis1);
        chart.setTitle("Exercise Volume by Date");
        chart.setLegendVisible(false);

        // Add plugins for better usability (like zoom and tooltips)
        chart.getPlugins().add(new Zoomer());
        chart.getPlugins().add(new DataPointTooltip());

        return chart;
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
                chart.getDatasets().clear();  // Clear previous data
                Text graphPlaceholderText = new Text("Select an Exercise to view data.");
                graphPanel.setCenter(graphPlaceholderText);  // Set the placeholder text
            }
        });

        return formPanel;
    }

    private void updateGraphForExercise(String exercise) {

        List<ActivityRecord> activityRecords = activityDao.getActivityDataByExercise(exercise);

        logger.info("Fetched {} records for exercise: {}", activityRecords.size(), exercise);

        if (!activityRecords.isEmpty()) {
            // Sort the records based on timestamp (ascending order)
            activityRecords.sort(Comparator.comparingLong(ActivityRecord::getTimestamp));

            // Get the earliest and latest timestamps
            long earliestTimestamp = activityRecords.get(0).getTimestamp(); // First record has the earliest timestamp
            long latestTimestamp = activityRecords.get(activityRecords.size() - 1).getTimestamp(); // Last record has the latest timestamp

            // Log the earliest and latest timestamps
            logger.info("Earliest activity date: {}", new Date(earliestTimestamp));
            logger.info("Latest activity date: {}", new Date(latestTimestamp));

            // Create a new axis with the specific bounds and tick unit
            DefaultNumericAxis xAxis = new DefaultNumericAxis(earliestTimestamp, latestTimestamp, 2592000000L); // 30 days in milliseconds (1 month)
            xAxis1.set("Date", "iso");
            // Set time axis and format the date
            xAxis.setTimeAxis(true);
            xAxis.setAutoRangeRounding(false); // Prevent auto-ranging

            StringConverter<Number> converter = getNumberStringConverter();

            xAxis1.setTickLabelFormatter(converter);
            xAxis1.set("Date", "iso");
            
            // Set the X-axis to the chart
            chart.getXAxis().set(xAxis); // Link this new axis to the chart
        } else {
            logger.info("No records found for the selected exercise.");
        }

        if (!activityRecords.isEmpty()) {
            // Create a DataSet for the graph
            DefaultErrorDataSet dataSet = new DefaultErrorDataSet("Exercise Data");

            // Process the data to populate the graph
            for (ActivityRecord record : activityRecords) {
                double volume = record.getReps() * record.getWeight();

                // Add the data point to the DataSet
                dataSet.add(new Date(record.getTimestamp()).getTime(), volume);
            }

            // Clear previous chart data and add the new data series
            chart.getDatasets().clear();
            chart.getDatasets().add(dataSet);

            // Ensure the chart is displayed in the center of the graphPanel
            graphPanel.setCenter(chart);
        } else {
            // If no data is available, show the "No data" message
            logger.info("No data found for the selected exercise.");
            Text graphPlaceholderText = new Text("No data available for this exercise.");
            graphPanel.setCenter(graphPlaceholderText);
        }
    }

    private static @NotNull StringConverter<Number> getNumberStringConverter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");

        StringConverter<Number> converter = new StringConverter<Number>() {
            @Override
            public String toString(Number value) {
                if (value == null) {
                    return "";
                }
                // Convert the number (Double) to a long timestamp and format it
                long timestamp = value.longValue();
                Date date = new Date(timestamp);
                return dateFormat.format(date);  // Use your dateFormat here
            }

            @Override
            public Number fromString(String string) {
                // You can leave this unimplemented if you're only formatting, not parsing input
                return null;
            }
        };
        return converter;
    }
}
