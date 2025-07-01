package info.matthewryan.workoutlogger.persistence;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDao {

    private static final Logger logger = LoggerFactory.getLogger(ActivityDao.class);

    private final Connection connection;

    // Constructor that takes both a custom Connection and ApplicationSettings object
    public ActivityDao(Connection connection) {
        this.connection = connection;
    }

    public void createActivityTable() {
        String sql = "CREATE TABLE IF NOT EXISTS activity_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "activity TEXT NOT NULL, " +
                "reps INTEGER NOT NULL, " +
                "weight REAL NOT NULL, " +
                "timestamp INTEGER NOT NULL" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Activity records table created successfully or already exists.");
        } catch (SQLException e) {
            logger.error("Error creating activity_records table: {}", e.getMessage(), e);
        }
    }

    public void insertActivity(ActivityRecord activityRecord) {
        String sql = "INSERT INTO activity_records (activity, reps, weight, timestamp) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, activityRecord.getActivity());
            pstmt.setInt(2, activityRecord.getReps());
            pstmt.setDouble(3, activityRecord.getWeight());
            pstmt.setLong(4, activityRecord.getTimestamp());
            pstmt.executeUpdate();
            //logger.info("Inserted activity: {}", activityRecord);
        } catch (SQLException e) {
            logger.error("Error inserting activity: {}", e.getMessage(), e);
        }
    }

    public List<ActivityRecord> getAllActivities() {
        List<ActivityRecord> activityRecords = new ArrayList<>();
        String sql = "SELECT * FROM activity_records";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ActivityRecord record = new ActivityRecord(
                        rs.getString("activity"),
                        rs.getInt("reps"),
                        rs.getDouble("weight"),
                        rs.getLong("timestamp"));  // Pass settings to constructor
                record.setId(rs.getLong("id"));
                activityRecords.add(record);
            }
            logger.info("Retrieved {} activities", activityRecords.size());
        } catch (SQLException e) {
            logger.error("Error retrieving activities: {}", e.getMessage(), e);
        }
        return activityRecords;
    }

    public ActivityRecord getPersonalBest(String activity) {
        ActivityRecord bestRecord = null;
        String sql = "SELECT * FROM activity_records WHERE activity = ? ORDER BY weight DESC LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, activity);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bestRecord = new ActivityRecord(
                            rs.getString("activity"),
                            rs.getInt("reps"),
                            rs.getDouble("weight"),
                            rs.getLong("timestamp"));  // Pass settings to constructor
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

    // Retrieve all activities from the database, ordered by timestamp descending
    public List<ActivityRecord> getAllActivitiesOrderedByTimestamp() {
        List<ActivityRecord> activities = new ArrayList<>();
        String query = "SELECT * FROM activity_records ORDER BY timestamp DESC";  // Replace 'activities' with your table name

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String activity = rs.getString("activity");
                int reps = rs.getInt("reps");
                double weight = rs.getDouble("weight");
                long timestamp = rs.getLong("timestamp");

                // Create ActivityRecord object and add to the list
                ActivityRecord record = new ActivityRecord(activity, reps, weight, timestamp);
                activities.add(record);
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }

        return activities;
    }

    public List<ActivityRecord> getActivityDataByExercise(String exercise) {
        List<ActivityRecord> activities = new ArrayList<>();
        String query = "SELECT * FROM activity_records WHERE activity = ? ORDER BY timestamp DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, exercise);  // Set the exercise parameter
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String activity = rs.getString("activity");
                    int reps = rs.getInt("reps");
                    double weight = rs.getDouble("weight");
                    long timestamp = rs.getLong("timestamp");

                    // Create ActivityRecord object and add it to the list
                    ActivityRecord record = new ActivityRecord(activity, reps, weight, timestamp);
                    activities.add(record);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving activity data for exercise {}: {}", exercise, e.getMessage(), e);
        }

        return activities;
    }

}
