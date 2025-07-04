package info.matthewryan.workoutlogger.persistence;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ActivityDao {

    private static final Logger logger = LoggerFactory.getLogger(ActivityDao.class);

    private final Connection connection;

    // Constructor that takes both a custom Connection and ApplicationSettings object
    public ActivityDao(Connection connection) {
        this.connection = connection;
    }

    // Create the activity table with exercise_id as a foreign key to exercises table
    public void createActivityTable() {
        String sql = "CREATE TABLE IF NOT EXISTS activity_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "exercise_id INTEGER NOT NULL, " +
                "reps INTEGER NOT NULL, " +
                "weight REAL NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "FOREIGN KEY (exercise_id) REFERENCES exercises(id)" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Activity records table created successfully or already exists.");
        } catch (SQLException e) {
            logger.error("Error creating activity_records table: {}", e.getMessage(), e);
        }
    }

    // Insert an activity record with exercise_id, reps, weight, and timestamp
    public void insertActivity(ActivityRecord activityRecord) {
        String sql = "INSERT INTO activity_records (exercise_id, reps, weight, timestamp) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, activityRecord.getExerciseId());  // Set the exercise_id
            pstmt.setInt(2, activityRecord.getReps());
            pstmt.setDouble(3, activityRecord.getWeight());
            pstmt.setLong(4, activityRecord.getTimestamp());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error inserting activity: {}", e.getMessage(), e);
        }
    }

    // Fetch all activities ordered by timestamp in descending order
    public List<ActivityRecord> getAllActivitiesOrderedByTimestamp() {
        List<ActivityRecord> activityRecords = new ArrayList<>();
        String sql = "SELECT * FROM activity_records ORDER BY timestamp DESC";  // Order by timestamp descending

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int exerciseId = rs.getInt("exercise_id");  // Get exercise_id
                String query = "SELECT name FROM exercises WHERE id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setInt(1, exerciseId);  // Fetch exercise name by exercise_id
                    ResultSet exerciseRs = pstmt.executeQuery();
                    String exerciseName = null;
                    if (exerciseRs.next()) {
                        exerciseName = exerciseRs.getString("name");
                    }

                    // Use the exerciseId and other fields to construct the ActivityRecord
                    ActivityRecord record = new ActivityRecord(exerciseId, rs.getInt("reps"), rs.getDouble("weight"), rs.getLong("timestamp"));
                    record.setId(rs.getLong("id"));
                    activityRecords.add(record);  // Add the record to the list
                }
            }
            logger.info("Retrieved {} activities ordered by timestamp.", activityRecords.size());
        } catch (SQLException e) {
            logger.error("Error retrieving activities ordered by timestamp: {}", e.getMessage(), e);
        }
        return activityRecords;
    }

    // Fetch activities for a specific date range (start of day to end of day)
    public List<ActivityRecord> getActivitiesByDate(LocalDate date) {
        List<ActivityRecord> activities = new ArrayList<>();
        long startOfDay = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfDay = date.atTime(23, 59, 59, 999999999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String query = "SELECT * FROM activity_records WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setLong(1, startOfDay);
            pstmt.setLong(2, endOfDay);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int exerciseId = rs.getInt("exercise_id");
                    String query2 = "SELECT name FROM exercises WHERE id = ?";
                    try (PreparedStatement pstmt2 = connection.prepareStatement(query2)) {
                        pstmt2.setInt(1, exerciseId);
                        ResultSet exerciseRs = pstmt2.executeQuery();
                        String exerciseName = null;
                        if (exerciseRs.next()) {
                            exerciseName = exerciseRs.getString("name");
                        }

                        ActivityRecord record = new ActivityRecord(exerciseId, rs.getInt("reps"), rs.getDouble("weight"), rs.getLong("timestamp"));
                        activities.add(record);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving activity data for date {}: {}", date, e.getMessage(), e);
        }

        return activities;
    }

    public List<ActivityRecord> getActivityDataByExercise(String exerciseName) {
        List<ActivityRecord> activities = new ArrayList<>();

        // Get the exercise_id for the given exerciseName
        int exerciseId = getExerciseIdByName(exerciseName);
        if (exerciseId == -1) {
            logger.warn("Exercise not found: {}", exerciseName);
            return activities; // Return empty list if the exercise doesn't exist
        }

        // Fetch activity records for the given exercise_id
        String query = "SELECT * FROM activity_records WHERE exercise_id = ? ORDER BY timestamp DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, exerciseId);  // Set the exercise_id

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Create an ActivityRecord using the data from the result set
                    ActivityRecord record = new ActivityRecord(
                            rs.getInt("exercise_id"),
                            rs.getInt("reps"),
                            rs.getDouble("weight"),
                            rs.getLong("timestamp")
                    );
                    record.setId(rs.getLong("id"));
                    activities.add(record);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving activity data for exercise {}: {}", exerciseName, e.getMessage(), e);
        }

        return activities;
    }


    // Fetch the personal best activity for a given exercise
    public ActivityRecord getPersonalBest(String activity) {
        ActivityRecord bestRecord = null;
        String sql = "SELECT * FROM activity_records WHERE exercise_id = ? ORDER BY weight DESC LIMIT 1";

        int exerciseId = getExerciseIdByName(activity);

        if (exerciseId == -1) {
            return null;  // If exercise doesn't exist, return null
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, exerciseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bestRecord = new ActivityRecord(
                            exerciseId,  // Use exerciseId
                            rs.getInt("reps"),
                            rs.getDouble("weight"),
                            rs.getLong("timestamp")
                    );
                    bestRecord.setId(rs.getLong("id"));
                }
            }
            if (bestRecord != null) {
                logger.info("Retrieved personal best for {}: {}", activity, bestRecord);
            } else {
                logger.info("No personal best found for {}", activity);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving personal best: {}", e.getMessage(), e);
        }
        return bestRecord;
    }

    // Helper method to get the exercise_id based on exercise name
    private int getExerciseIdByName(String exerciseName) {
        String sql = "SELECT id FROM exercises WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, exerciseName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving exercise ID for {}: {}", exerciseName, e.getMessage(), e);
        }
        return -1;  // Return -1 if the exercise does not exist
    }
}
