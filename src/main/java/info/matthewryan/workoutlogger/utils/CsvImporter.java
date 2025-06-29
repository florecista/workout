package info.matthewryan.workoutlogger.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;

public class CsvImporter {

    private static final Logger logger = LoggerFactory.getLogger(CsvImporter.class);

    private final ActivityDao dao;

    // Constructor to allow dependency injection for easier testing
    public CsvImporter(ActivityDao dao) {
        this.dao = dao;
    }

    // Method to import CSV data from a file
    public void importCsv(String csvFilePath) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            // Skip the header row
            reader.readNext(); // Skipping the header

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                // Ensure insertActivity is only called once for each valid record
                if (nextLine.length > 4) {  // Ensure the line has enough columns
                    // Parse and create the ActivityRecord object, pass the settings
                    ActivityRecord record = new ActivityRecord(nextLine[2], Integer.parseInt(nextLine[3]),
                            Double.parseDouble(nextLine[4]), parseDateToTimestamp(nextLine[0]));
                    dao.insertActivity(record);  // This should only be called once per record
                    logger.info("Inserted record: {}", record);
                }
            }
        } catch (IOException | NumberFormatException | CsvValidationException e) {
            logger.error("Error processing CSV file: {}", e.getMessage(), e);
        }
    }

    // Method to import a single CSV line
    public void importCsvLine(String line) {
        try {
            // Process the single CSV line
            String[] nextLine = line.split(",");
            if (nextLine.length < 5) {
                logger.warn("Skipping invalid line: {}", line);
                return;
            }

            String date = nextLine[0];
            String exerciseName = nextLine[2];
            int reps = Integer.parseInt(nextLine[3]);
            double weightKg = Double.parseDouble(nextLine[4]);
            long timestamp = parseDateToTimestamp(date);

            // Create ActivityRecord object, pass the settings
            ActivityRecord record = new ActivityRecord(exerciseName, reps, weightKg, timestamp);

            // Insert into database using DAO
            dao.insertActivity(record);
            logger.info("Inserted record: {}", record);

        } catch (Exception e) {
            logger.error("Error processing CSV line: {}", e.getMessage(), e);
        }
    }

    // Helper method to parse date to timestamp
    private long parseDateToTimestamp(String date) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date parsedDate = sdf.parse(date);
            return parsedDate.getTime();
        } catch (java.text.ParseException e) {
            logger.error("Date format error: {}", e.getMessage(), e);
            return System.currentTimeMillis(); // Default to current time if parsing fails
        }
    }
}
