package info.matthewryan.workoutlogger;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityDaoTest extends UnitTestBase {

    private static final Logger logger = LoggerFactory.getLogger(ActivityDaoTest.class);

    private ActivityDao dao;
    private ApplicationSettings settings;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp(); // Call the parent class's setUp method to delete the database and set up the connection

        dao = new ActivityDao(connection);  // Use the connection inherited from UnitTestBase
        settings = new ApplicationSettings();  // Initialize ApplicationSettings

        dao.createActivityTable();  // Create necessary tables
        clearActivityTable();  // Clear the table before starting the tests
    }

    private void clearActivityTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM activity_records");
            logger.info("Cleared the activity records table");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testInsertActivity() {
        // Insert exercise into exercises table first
        String exerciseName = "Bench Press";
        int exerciseId = insertExerciseIfNotExists(exerciseName);

        // Create an ActivityRecord with exerciseId instead of activity name
        ActivityRecord record = new ActivityRecord(exerciseId, 5, 100.0, System.currentTimeMillis());

        // Insert the activity record
        dao.insertActivity(record);

        // Retrieve activities from the database
        List<ActivityRecord> activities = dao.getAllActivitiesOrderedByTimestamp();

        // Verify the activity was inserted
        assertNotNull(activities, "Activities list should not be null");
        assertEquals(1, activities.size(), "There should be one activity");
        assertEquals(exerciseId, activities.get(0).getExerciseId(), "Exercise ID should match the inserted record");
    }

    @Test
    void testGetAllActivities() {
        // Insert exercise into exercises table first
        int exerciseId1 = insertExerciseIfNotExists("Bench Press");
        int exerciseId2 = insertExerciseIfNotExists("Bicep Curl");

        // Insert multiple activity records
        ActivityRecord record1 = new ActivityRecord(exerciseId1, 5, 100.0, System.currentTimeMillis());
        ActivityRecord record2 = new ActivityRecord(exerciseId2, 10, 15.0, System.currentTimeMillis());
        dao.insertActivity(record1);
        dao.insertActivity(record2);

        // Get all activities from the database
        List<ActivityRecord> activities = dao.getAllActivitiesOrderedByTimestamp();

        // Verify that the activities are correctly retrieved
        assertNotNull(activities, "Activities list should not be null");
        assertEquals(2, activities.size(), "There should be two activities in the list");
        assertEquals(exerciseId1, activities.get(0).getExerciseId(), "First activity should match the correct exerciseId");
        assertEquals(exerciseId2, activities.get(1).getExerciseId(), "Second activity should match the correct exerciseId");
    }

    @Test
    void testDayTrainingSession() {
        // Insert exercise into exercises table first
        int exerciseId = insertExerciseIfNotExists("Bench Press");

        // Create a list of activity records
        List<ActivityRecord> activityRecords = List.of(
                new ActivityRecord(exerciseId, 10, 40.0, 1635288019000L),
                new ActivityRecord(exerciseId, 10, 60.0, 1635288374000L)
        );

        // Insert the activities into the database
        activityRecords.forEach(dao::insertActivity);

        // Get all activities from the database
        List<ActivityRecord> activities = dao.getAllActivitiesOrderedByTimestamp();

        // Verify the number of activities
        assertEquals(2, activities.size(), "The number of activities should match the inserted records");

        // Additional assertions for specific activities
        ActivityRecord firstRecord = activities.get(0);
        assertEquals(exerciseId, firstRecord.getExerciseId(), "First record should have correct exerciseId");
        assertEquals(40.0, firstRecord.getWeight(), "First record should have weight 40.0 kg");
    }

    // Helper method to insert an exercise if it does not exist
    private int insertExerciseIfNotExists(String exerciseName) {
        try (Statement stmt = connection.createStatement()) {
            // Check if exercise already exists
            String checkQuery = "SELECT id FROM exercises WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkQuery)) {
                pstmt.setString(1, exerciseName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");  // Return existing exercise ID
                }
            }

            // If not, insert the exercise
            String insertQuery = "INSERT INTO exercises (name) VALUES (?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, exerciseName);
                pstmt.executeUpdate();
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);  // Return the generated exercise ID
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting exercise: {}", e.getMessage(), e);
        }
        return -1;  // Return -1 if there was an error
    }
}
