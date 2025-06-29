package info.matthewryan.workoutlogger;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityDaoTest extends UnitTestBase {  // Ensure this extends UnitTestBase

    private static final Logger logger = LoggerFactory.getLogger(ActivityDaoTest.class);

    private ActivityDao dao;
    private ApplicationSettings settings;  // Declare the settings object

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp(); // Call the parent class's setUp method to delete the database and set up the connection

        // The connection is inherited from UnitTestBase, no need to create it again
        dao = new ActivityDao(connection);  // Use the connection inherited from UnitTestBase

        // Initialize ApplicationSettings for the tests (set to true for metric units, false for imperial)
        settings = new ApplicationSettings();  // Or false, depending on your test scenario

        // Create necessary tables and insert initial activity records
        dao.createActivityTable();

        // Clear the table to start fresh for each test
        clearActivityTable();
    }

    private void clearActivityTable() {
        // Clear the activity records table before each test to avoid conflicts
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM activity_records");
            logger.info("Cleared the activity records table");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testInsertActivity() {
        // Create an ActivityRecord with the settings
        ActivityRecord record = new ActivityRecord("Bench Press", 5, 100.0, System.currentTimeMillis());

        // Insert the activity record
        dao.insertActivity(record);

        // Retrieve activities from the database
        List<ActivityRecord> activities = dao.getAllActivities();

        // Verify the activity was inserted
        assertNotNull(activities, "Activities list should not be null");
        assertEquals(1, activities.size(), "There should be one activity");
        assertEquals("Bench Press", activities.get(0).getActivity(), "Activity should be 'Bench Press'");
    }

    @Test
    void testGetAllActivities() {
        // Insert multiple activity records
        ActivityRecord record1 = new ActivityRecord("Bench Press", 5, 100.0, System.currentTimeMillis());
        ActivityRecord record2 = new ActivityRecord("Bicep Curl", 10, 15.0, System.currentTimeMillis());
        dao.insertActivity(record1);
        dao.insertActivity(record2);

        // Get all activities from the database
        List<ActivityRecord> activities = dao.getAllActivities();

        // Verify that the activities are correctly retrieved
        assertNotNull(activities, "Activities list should not be null");
        assertEquals(2, activities.size(), "There should be two activities in the list");
        assertEquals("Bench Press", activities.get(0).getActivity(), "First activity should be 'Bench Press'");
        assertEquals("Bicep Curl", activities.get(1).getActivity(), "Second activity should be 'Bicep Curl'");
    }

    @Test
    void testDayTrainingSession() {
        // Ensure weight is properly set in the ActivityRecord creation
        List<ActivityRecord> activityRecords = List.of(
                new ActivityRecord("Bench Press 5’s", 10, 40.0, 1635288019000L),  // Correct weight
                new ActivityRecord("Bench Press 5’s", 10, 60.0, 1635288374000L)  // Correct weight
        );

        // Insert the activities into the database
        activityRecords.forEach(dao::insertActivity);

        // Get all activities from the database
        List<ActivityRecord> activities = dao.getAllActivities();

        // Verify the number of activities
        assertEquals(2, activities.size(), "The number of activities should match the inserted records");

        // Additional assertions for specific activities
        ActivityRecord firstRecord = activities.get(0);
        assertEquals("Bench Press 5’s", firstRecord.getActivity(), "First activity should be 'Bench Press 5’s'");
        assertEquals(40.0, firstRecord.getWeight(), "First record should have weight 40.0 kg");
    }

}
