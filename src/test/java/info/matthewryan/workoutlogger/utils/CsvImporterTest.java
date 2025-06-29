package info.matthewryan.workoutlogger.utils;

import info.matthewryan.workoutlogger.ApplicationSettings;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class CsvImporterTest {

    private ActivityDao dao;
    private CsvImporter csvImporter;
    private ApplicationSettings settings;

    // Number of lines to read for the test
    private static final int LINES_TO_READ = 30;

    @BeforeEach
    public void setUp() {
        // Mock the ActivityDao
        dao = mock(ActivityDao.class);

        // Create an instance of ApplicationSettings (e.g., prefer metric units as true or false)
        settings = new ApplicationSettings();  // Example, assuming 'true' means metric units

        // Create an instance of CsvImporter with the mocked dao
        csvImporter = new CsvImporter(dao, settings);
    }
//
//    @Test
//    @Ignore
//    public void testImportCsv() throws IOException {
//        // Create a temporary CSV file for testing
//        File tempCsvFile = File.createTempFile("test_import_", ".csv");
//        tempCsvFile.deleteOnExit();
//
//        // Write mock CSV data to the temporary file (two records in this example)
//        try (FileWriter writer = new FileWriter(tempCsvFile)) {
//            writer.write("Date,Workout Name,Exercise Name,Reps,Weight (kg),Weight (lb),Notes,Duration\n");
//            writer.write("2021-11-18 13:03:57,Weekly Day 2,Trapbar Deadlift,6,67.0,147.71,,0\n");
//            writer.write("2021-11-18 13:06:57,Weekly Day 2,Trapbar Deadlift,6,67.0,147.71,,0\n");
//        }
//
//        // Call importCsv method with the temp file path
//        csvImporter.importCsv(tempCsvFile.getAbsolutePath());
//
//        // Verify that insertActivity() is called for each record in the CSV file
//        verify(dao, times(2)).insertActivity(any(ActivityRecord.class));  // Adjusted to match 2 records
//
//        // Optionally, assert specific records were inserted
//        verify(dao).insertActivity(argThat(record ->
//                record.getActivity().equals("Trapbar Deadlift") &&
//                        record.getReps() == 6 &&
//                        record.getWeight() == 67.0));
//    }

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
                while ((line = reader.readLine()) != null && lineCounter < LINES_TO_READ) {
                    lineCounter++;  // Increment counter for each line

                    // Skip the header line
                    if (lineCounter == 1) {
                        continue;  // Skip header
                    }

                    try {
                        // Assuming CsvImporter processes each line of the CSV
                        csvImporter.importCsvLine(line);  // Process the line
                        System.out.println("Processed line " + lineCounter + ": " + line);
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
        verify(dao, times(lineCounter - 1)).insertActivity(Mockito.any(ActivityRecord.class)); // Adjusted to match lineCounter - 1 (because we skip header)
    }

}
