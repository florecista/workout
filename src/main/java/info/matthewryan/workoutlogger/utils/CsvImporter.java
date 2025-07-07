package info.matthewryan.workoutlogger.utils;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class CsvImporter {

    private static final Logger logger = LoggerFactory.getLogger(CsvImporter.class);

    private final ExerciseDao exerciseDao;
    private final ActivityDao activityDao;

    // Constructor to allow dependency injection for easier testing
    public CsvImporter(ExerciseDao exerciseDao, ActivityDao activityDao) {
        this.exerciseDao = exerciseDao;
        this.activityDao = activityDao;
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

            String date = nextLine[0];  // Assuming the first column is the date
            String exerciseName = nextLine[2];
            int reps = Integer.parseInt(nextLine[3]);
            double weightKg = Double.parseDouble(nextLine[4]);
            long timestamp = parseDateToTimestamp(date);

            // Get or create exercise ID
            int exerciseId = exerciseDao.getOrCreateExerciseId(exerciseName);

            // Extract the date part of the timestamp as the session ID
            long sessionId = getSessionIdFromTimestamp(timestamp);

            // Create ActivityRecord object, pass the exercise ID, timestamp, and sessionId
            ActivityRecord record = new ActivityRecord(exerciseId, reps, weightKg, timestamp, sessionId);

            // Insert into database using DAO
            activityDao.insertActivity(record);
            //logger.info("Inserted record: {}", record);

        } catch (Exception e) {
            logger.error("Error processing CSV line: {}", e.getMessage(), e);
        }
    }

    private long getSessionIdFromTimestamp(long timestamp) {
        // Extract the date part from the timestamp (ignoring the time)
        LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        return date.toEpochDay();  // Use the epoch day as the sessionId (a long)
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
