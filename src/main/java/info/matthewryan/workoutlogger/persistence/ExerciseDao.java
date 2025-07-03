package info.matthewryan.workoutlogger.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDao {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseDao.class);

    private final Connection connection;  // Change to use a shared connection

    public ExerciseDao(Connection connection) {
        this.connection = connection;
    }

    public void createExerciseTable() {
        String sql = "CREATE TABLE IF NOT EXISTS exercises (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Exercises table created successfully or already exists.");
        } catch (SQLException e) {
            logger.error("Error creating exercises table: {}", e.getMessage(), e);
        }
    }

    public boolean insertExercise(String exerciseName) {
        // Check for empty or null exercise name
        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            logger.error("Attempted to insert an empty or null exercise name.");
            return false;  // Reject empty or null names
        }

        String sql = "INSERT OR IGNORE INTO exercises (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, exerciseName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;  // If no rows were affected, it means the exercise was ignored
        } catch (SQLException e) {
            logger.error("Error inserting exercise: {}", e.getMessage(), e);
            return false;
        }
    }

    public List<String> getAllExercises() {
        List<String> exercises = new ArrayList<>();
        String sql = "SELECT name FROM exercises";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                exercises.add(rs.getString("name"));
            }
            logger.info("Retrieved {} exercises", exercises.size());  // Log the number of exercises
        } catch (SQLException e) {
            logger.error("Error retrieving exercises: {}", e.getMessage(), e);  // Log error
        }
        return exercises;
    }

    // ===================== Volume Group Table Methods ==========================
    public void createVolumeGroupTable() {
        String sql = "CREATE TABLE IF NOT EXISTS volume_groups (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Volume Groups table created successfully or already exists.");
        } catch (SQLException e) {
            logger.error("Error creating volume groups table: {}", e.getMessage(), e);
        }
    }

    public void insertVolumeGroup(String groupName) {
        String sql = "INSERT OR IGNORE INTO volume_groups (name) VALUES (?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();
            logger.info("Inserted volume group: {}", groupName);  // Log volume group insertion
        } catch (SQLException e) {
            logger.error("Error inserting volume group: {}", e.getMessage(), e);  // Log error
        }
    }

    public List<String> getAllVolumeGroups() {
        List<String> volumeGroups = new ArrayList<>();
        String sql = "SELECT name FROM volume_groups";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                volumeGroups.add(rs.getString("name"));
            }
            logger.info("Retrieved {} volume groups", volumeGroups.size());  // Log the number of volume groups
        } catch (SQLException e) {
            logger.error("Error retrieving volume groups: {}", e.getMessage(), e);  // Log error
        }
        return volumeGroups;
    }

    // ===================== Archive Exercise Method ==========================
    public void archiveExercise(int exerciseId, boolean archive) {
        String sql = "UPDATE exercises SET archived = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, archive);
            pstmt.setInt(2, exerciseId);
            pstmt.executeUpdate();
            logger.info("Exercise with ID {} archived status updated to {}", exerciseId, archive);
        } catch (SQLException e) {
            logger.error("Error updating archive status for exercise ID {}: {}", exerciseId, e.getMessage(), e);
        }
    }

    // ===================== Delete Exercise Method ==========================
    public void deleteExercise(int exerciseId) {
        String sql = "DELETE FROM exercises WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, exerciseId);
            pstmt.executeUpdate();
            logger.info("Exercise with ID {} deleted", exerciseId);
        } catch (SQLException e) {
            logger.error("Error deleting exercise ID {}: {}", exerciseId, e.getMessage(), e);
        }
    }

    // ===================== Test Helper Method ==========================

    // Helper method to clear the exercises table (for tests)
    public void clearExercisesTable() {
        String sql = "DELETE FROM exercises";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            logger.info("Exercises table cleared");
        } catch (SQLException e) {
            logger.error("Error clearing exercises table: {}", e.getMessage(), e);
        }
    }
}
