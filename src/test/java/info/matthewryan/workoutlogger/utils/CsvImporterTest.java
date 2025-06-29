package info.matthewryan.workoutlogger.utils;

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

    // Number of lines to read for the test
    private static final int LINES_TO_READ = 30;

    @BeforeEach
    public void setUp() {
        // Mock the ActivityDao
        dao = mock(ActivityDao.class);

        // Create an instance of CsvImporter with the mocked dao
        csvImporter = new CsvImporter(dao);
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