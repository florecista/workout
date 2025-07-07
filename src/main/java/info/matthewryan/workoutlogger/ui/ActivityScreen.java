package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.Exercise;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import info.matthewryan.workoutlogger.model.ActivityRecord;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.DataPointTooltip;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.dataset.spi.DefaultErrorDataSet;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
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
    private ScreenStartup screenStartup;

    private BorderPane graphPanel;
    private XYChart chart;

    private TextField setField;
    private TextField unitField;
    private TextField repsField;

    private DefaultNumericAxis xAxis1;

    public ActivityScreen(ActivityDao activityDao, ExerciseDao exerciseDao, CustomToolBar toolBar, ScreenStartup screenStartup) {
        this.activityDao = activityDao;
        this.exerciseDao = exerciseDao;
        this.toolBar = toolBar;
        this.screenStartup = screenStartup;
    }

    public BorderPane getRoot() {

        BorderPane root = new BorderPane();

        graphPanel = createGraphPanel();  // Initialize the graphPanel here

        BorderPane formPanel = createFormPanel();

        root.setTop(graphPanel);
        root.setBottom(formPanel);

        graphPanel.prefHeightProperty().bind(root.heightProperty().multiply(0.38));  // 50% of screen height
        formPanel.prefHeightProperty().bind(root.heightProperty().multiply(0.48));   // 50% of screen height

        return root;
    }

    private BorderPane createGraphPanel() {
        BorderPane graphPanel = new BorderPane();
        graphPanel.setStyle("-fx-background-color: lightgray;");  // Example style for graph panel

        chart = createGraphChart();  // Now we store the reference to the XYChart

        Text graphPlaceholderText = new Text("Select an Exercise to view data.");
        graphPanel.setCenter(graphPlaceholderText);  // Set the placeholder text initially

        return graphPanel;
    }

    private XYChart createGraphChart() {

        xAxis1 = new DefaultNumericAxis("Date", "iso");
        xAxis1.setOverlapPolicy(io.fair_acc.chartfx.axes.AxisLabelOverlapPolicy.SKIP_ALT);
        xAxis1.setAutoRangeRounding(false);
        xAxis1.setTimeAxis(true);
        DefaultNumericAxis yAxis1 = new DefaultNumericAxis("Total Volume", "kg");

        xAxis1.set("Date", "iso");

        XYChart chart = new XYChart(xAxis1, yAxis1);
        chart.setTitle("Exercise Volume by Date");
        chart.setLegendVisible(false);

        chart.getPlugins().add(new Zoomer());
        chart.getPlugins().add(new DataPointTooltip());

        return chart;
    }

    private BorderPane createFormPanel() {
        BorderPane formPanel = new BorderPane();
        formPanel.setStyle("-fx-background-color: white;");

        ComboBox<Exercise> exerciseComboBox = new ComboBox<>();
        exerciseComboBox.setStyle("-fx-font-size: 15px;");
        List<Exercise> exercises = exerciseDao.getAllExercises();

        // Add a "Select" placeholder as a string
        exerciseComboBox.getItems().add(new Exercise(-1, "Select an Exercise", false));  // Add the placeholder
        exerciseComboBox.getItems().addAll(exercises);  // Add real exercises

        exerciseComboBox.getSelectionModel().selectFirst();  // Select the "Select an Exercise" by default

        // Set up the ComboBox to display only the name of the exercise
        exerciseComboBox.setCellFactory(param -> new ListCell<Exercise>() {
            @Override
            protected void updateItem(Exercise item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getName());  // Display the exercise name only
                }
            }
        });

        exerciseComboBox.setConverter(new StringConverter<Exercise>() {
            @Override
            public String toString(Exercise exercise) {
                if (exercise == null) {
                    return null;
                }
                return exercise.getName();
            }

            @Override
            public Exercise fromString(String string) {
                return null;
            }
        });

        exerciseComboBox.setOnAction(e -> {
            Exercise selectedExercise = exerciseComboBox.getSelectionModel().getSelectedItem();
            if (selectedExercise != null && !"Select".equals(selectedExercise)) {
                updateGraphForExercise(selectedExercise.getName());
            }
        });


        exerciseComboBox.setPrefWidth(Double.MAX_VALUE);

        // Create the text fields for Set, Unit, Reps
        setField = new TextField();
        unitField = new TextField();
        repsField = new TextField();

        setField.setStyle("-fx-font-size: 20px;");
        unitField.setStyle("-fx-font-size: 20px;");
        repsField.setStyle("-fx-font-size: 20px;");

        setField.setPrefHeight(50);
        unitField.setPrefHeight(50);
        repsField.setPrefHeight(50);

        // Create numeric buttons for the second to fourth rows
        Button btn7 = createButton("7");
        Button btn8 = createButton("8");
        Button btn9 = createButton("9");
        Button btn4 = createButton("4");
        Button btn5 = createButton("5");
        Button btn6 = createButton("6");
        Button btn1 = createButton("1");
        Button btn2 = createButton("2");
        Button btn3 = createButton("3");
        Button btn0 = createButton("0");

        Button btnLog = new Button("See Log");
        btnLog.setFocusTraversable(false);

        btnLog.setOnMouseClicked(event -> {
            int currentSessionId = SessionManager.getInstance().getCurrentSession().getId();
            screenStartup.showSessionActivityLogScreen(currentSessionId);
        });

        // Create Delete and Save buttons with icons (without text)
        Button btnDelete = new Button();
        btnDelete.setFocusTraversable(false);
        btnDelete.setOnMouseClicked(event -> {
            TextField focusedField = getFocusedField(setField, unitField, repsField);
            if (focusedField != null && focusedField.getText().length() > 0) {
                String currentText = focusedField.getText();
                focusedField.setText(currentText.substring(0, currentText.length() - 1));
                focusedField.requestFocus();
            }
        });

        Button btnSave = new Button();

        Image deleteImage = new Image("delete_icon.png");  // Load delete icon
        Image saveImage = new Image("save_icon.png");  // Load save icon

        ImageView deleteImageView = new ImageView(deleteImage);
        deleteImageView.setFitWidth(30);  // Scale to 30px width
        deleteImageView.setFitHeight(30);  // Scale to 30px height
        deleteImageView.setPreserveRatio(true);  // Maintain aspect ratio

        ImageView saveImageView = new ImageView(saveImage);
        saveImageView.setFitWidth(30);  // Scale to 30px width
        saveImageView.setFitHeight(30);  // Scale to 30px height
        saveImageView.setPreserveRatio(true);  // Maintain aspect ratio

        btnDelete.setGraphic(deleteImageView);
        btnSave.setGraphic(saveImageView);

        btnLog.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-font-weight: bold;");
        //btnDelete.setStyle("-fx-background-color: #FF6F6F;");  // Softer red
        //btnSave.setStyle("-fx-background-color: #6DFF6D;");  // Softer green
        btnDelete.setStyle("-fx-background-color: #FFB6B6;");  // Pastel red
        btnSave.setStyle("-fx-background-color: #B6FFB6;");  // Pastel green

        Button btnExitSession = new Button();
        Image exitSessionImage = new Image("exit-session_icon.png");
        ImageView exitSessionImageView = new ImageView(exitSessionImage);
        exitSessionImageView.setFitWidth(30);  // Scale to 30px width
        exitSessionImageView.setFitHeight(30);  // Scale to 30px height
        btnExitSession.setGraphic(exitSessionImageView);
        btnExitSession.setOnMouseClicked(event -> {
            // Handle the action when the Exit Session button is clicked
            // For example, you can end the session, log out, or navigate to another screen
            System.out.println("Exit Session clicked!");
            // You can call your session end method here, like:
            SessionManager.getInstance().endSession();

            screenStartup.showStartScreen();
        });

        // Set up the GridPane layout
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        // Row 1: ComboBox for Exercise (spans 3 columns)
        grid.add(exerciseComboBox, 0, 0, 3, 1);  // ComboBox spans 3 columns

        grid.add(btnLog, 3, 0, 1, 1); // Log button spans rows 0-1

        // Row 2: Set, Unit, Reps, Empty
        grid.add(setField, 0, 1);
        grid.add(unitField, 1, 1);
        grid.add(repsField, 2, 1);
        grid.add(new Label(""), 3, 1); // Empty cell in the 4th column

        // Row 3: Buttons for 7, 8, 9, Delete
        grid.add(btn7, 0, 2);
        grid.add(btn8, 1, 2);
        grid.add(btn9, 2, 2);
        grid.add(new Label(""), 3, 2, 1, 2); // Delete button spans rows 2-3

        // Row 4: Buttons for 4, 5, 6, Empty
        grid.add(btn4, 0, 3);
        grid.add(btn5, 1, 3);
        grid.add(btn6, 2, 3);
        grid.add(new Label(""), 3, 3); // Empty cell

        // Row 5: Buttons for 1, 2, 3, Save
        grid.add(btn1, 0, 4);
        grid.add(btn2, 1, 4);
        grid.add(btn3, 2, 4);
        grid.add(new Label(""), 3, 4); // Empty cell

        // Row 6: Empty cell, Button for 0, Empty
        grid.add(btnDelete, 0, 5);  // Empty cell
        grid.add(btn0, 1, 5);
        grid.add(btnSave, 2, 5);
        grid.add(btnExitSession, 3, 5);

        // Ensure buttons grow to fill their respective cells
        GridPane.setHgrow(btn7, Priority.ALWAYS);
        GridPane.setHgrow(btn8, Priority.ALWAYS);
        GridPane.setHgrow(btn9, Priority.ALWAYS);
        GridPane.setHgrow(btn4, Priority.ALWAYS);
        GridPane.setHgrow(btn5, Priority.ALWAYS);
        GridPane.setHgrow(btn6, Priority.ALWAYS);
        GridPane.setHgrow(btn1, Priority.ALWAYS);
        GridPane.setHgrow(btn2, Priority.ALWAYS);
        GridPane.setHgrow(btn3, Priority.ALWAYS);
        GridPane.setHgrow(btn0, Priority.ALWAYS);
        GridPane.setHgrow(btnDelete, Priority.ALWAYS);
        GridPane.setHgrow(btnSave, Priority.ALWAYS);

        btnLog.setMinSize(85, 30);
        //btnDelete.setMinSize(85, 110);
        //btnSave.setMinSize(85, 110);
        btnExitSession.setMinSize(85, 50);

        // Set the preferred size for buttons to make them visually bigger
        btn7.setMinSize(90, 50);
        btn8.setMinSize(90, 50);
        btn9.setMinSize(90, 50);
        btn4.setMinSize(90, 50);
        btn5.setMinSize(90, 50);
        btn6.setMinSize(90, 50);
        btn1.setMinSize(90, 50);
        btn2.setMinSize(90, 50);
        btn3.setMinSize(90, 50);
        btnDelete.setMinSize(90, 50);
        btn0.setMinSize(90, 50);
        btnSave.setMinSize(90, 50);

        // Optional: Adjust column widths and row heights for better layout
        grid.setPrefWidth(400);
        grid.setPrefHeight(200);

        // Ensure the GridPane's content is laid out properly
        formPanel.setCenter(grid);

        // Add some padding to the formPanel
        formPanel.setPadding(new javafx.geometry.Insets(10));

        return formPanel;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setFocusTraversable(false);
        button.setOnMouseClicked(event -> handleButtonClick(setField, unitField, repsField, text));  // Pass the button text (number)
        return button;
    }

    private void handleButtonClick(TextField setField, TextField unitField, TextField repsField, String number) {
        TextField focusedField = getFocusedField(setField, unitField, repsField);
        if (focusedField != null) {
            focusedField.appendText(number);  // Append the number to the focused textfield
            focusedField.requestFocus();  // Ensure the focus remains on the textfield
        }
    }

    private TextField getFocusedField(TextField setField, TextField unitField, TextField repsField) {
        if (setField.isFocused()) {
            logger.info("setField has focus");
            return setField;
        } else if (unitField.isFocused()) {
            logger.info("unitField has focus");
            return unitField;
        } else if (repsField.isFocused()) {
            logger.info("repsField has focus");
            return repsField;
        }
        return null;
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
