package info.matthewryan.workoutlogger.utils;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class CsvImporterTest {

    private ExerciseDao exerciseDao;
    private ActivityDao activityDao;
    private CsvImporter csvImporter;

    // Statistics variables
    private Set<String> exerciseSet;
    private Map<String, Integer> exerciseRecordCount;
    private long earliestTimestamp;
    private long latestTimestamp;

    @BeforeEach
    public void setUp() {
        exerciseDao = mock(ExerciseDao.class);
        activityDao = mock(ActivityDao.class);

        // Create an instance of CsvImporter with the mocked dao
        csvImporter = new CsvImporter(exerciseDao, activityDao);

        // Initialize statistics variables
        exerciseSet = new HashSet<>();
        exerciseRecordCount = new HashMap<>();
        earliestTimestamp = Long.MAX_VALUE;
        latestTimestamp = Long.MIN_VALUE;
    }

    @Test
    void testResourceLoading() throws Exception {
        // Path to your CSV file in the resources directory
        String filePath = "/data.csv";

        // Attempt to access the resource
        InputStream inputStream = getClass().getResourceAsStream(filePath);

        // Assert that the resource is not null, meaning the file was found
        assertNotNull(inputStream, "The resource file was not found!");

        // Read the file to verify
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = reader.readLine();
            System.out.println("First line of the file: " + line);  // Just to verify contents
        }
    }

    @Test
    void testImportCsvFromFile() {
        // Path to the CSV file in the resources directory
        String filePath = "/data.csv";
        int lineCounter = 0;  // Counter to track lines

        try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
            // Handle case where the file isn't found
            if (inputStream == null) {
                throw new FileNotFoundException("File not found: " + filePath);
            }

            // Read and process each line of the file
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineCounter++;  // Increment counter for each line

                    // Skip the header line
                    if (lineCounter == 1) {
                        continue;  // Skip header
                    }

                    try {
                        // Assuming CsvImporter processes each line of the CSV
                        csvImporter.importCsvLine(line);  // Process the line

                        // Update statistics
                        updateStatistics(line);
                    } catch (Exception e) {
                        System.err.println("Error processing line " + lineCounter + ": " + e.getMessage());
                        break;  // Stop processing on the first exception
                    }
                }
            }
        } catch (IOException e) {
            // Handle exceptions for file reading
            System.err.println("Error reading the file: " + e.getMessage());
        }

        // Verify that insertActivity was called the correct number of times
        verify(activityDao, times(lineCounter - 1)).insertActivity(Mockito.any(ActivityRecord.class)); // Adjusted to match lineCounter - 1 (because we skip header)

        // Output statistics
        outputStatistics();
    }

    // Method to update statistics while processing each line
    private void updateStatistics(String line) {
        // Split the CSV line
        String[] nextLine = line.split(",");

        if (nextLine.length < 5) {
            return;  // Skip invalid lines
        }

        String exerciseName = nextLine[2];  // Get the exercise name
        long timestamp = parseDateToTimestamp(nextLine[0]);

        // Track exercises and their counts
        exerciseSet.add(exerciseName);
        exerciseRecordCount.put(exerciseName, exerciseRecordCount.getOrDefault(exerciseName, 0) + 1);

        // Track earliest and latest timestamps
        if (timestamp < earliestTimestamp) {
            earliestTimestamp = timestamp;
        }
        if (timestamp > latestTimestamp) {
            latestTimestamp = timestamp;
        }
    }

    // Helper method to parse date to timestamp
    private long parseDateToTimestamp(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date parsedDate = sdf.parse(date);
            return parsedDate.getTime();
        } catch (java.text.ParseException e) {
            System.err.println("Date format error: " + e.getMessage());
            return System.currentTimeMillis(); // Default to current time if parsing fails
        }
    }

    // Output the statistics after processing
    private void outputStatistics() {
        // Output number of exercises
        System.out.println("Number of Exercises: " + exerciseSet.size());

        // Output number of records per exercise
        System.out.println("Number of records per exercise:");
        for (Map.Entry<String, Integer> entry : exerciseRecordCount.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        // Output earliest and latest records
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Earliest record date: " + sdf.format(new Date(earliestTimestamp)));
        System.out.println("Latest record date: " + sdf.format(new Date(latestTimestamp)));
    }
}
