package info.matthewryan.workoutlogger.persistence;

import info.matthewryan.workoutlogger.model.Exercise;
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
                "name TEXT NOT NULL UNIQUE, " +
                "factory BOOLEAN NOT NULL DEFAULT 0" +  // Add factory column with default value 0 (false)
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Exercises table created successfully or already exists.");
        } catch (SQLException e) {
            logger.error("Error creating exercises table: {}", e.getMessage(), e);
        }
    }

    // Insert an exercise into the exercises table
    public boolean insertExercise(String exerciseName, boolean factory) {
        if (exerciseName == null || exerciseName.isEmpty())
            return false;

        // Check if the exercise already exists
        String checkQuery = "SELECT id FROM exercises WHERE name = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, exerciseName);
            ResultSet rs = checkStmt.executeQuery();

            // If the exercise exists, don't insert it again
            if (rs.next()) {
                // Exercise already exists, so we don't insert it again
                logger.info("Exercise '{}' already exists, skipping insert.", exerciseName);
                return false; // Return false to indicate the exercise was not inserted
            }
        } catch (SQLException e) {
            logger.error("Error checking if exercise exists: {}", e.getMessage());
            return false; // Return false if there was an error during the check
        }

        // If exercise does not exist, insert it
        String insertQuery = "INSERT INTO exercises (name, factory) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, exerciseName);
            pstmt.setBoolean(2, factory);  // Insert the factory value
            pstmt.executeUpdate();
            logger.info("Inserted exercise: {} (Factory: {})", exerciseName, factory);
            return true; // Return true to indicate the exercise was successfully inserted
        } catch (SQLException e) {
            logger.error("Error inserting exercise: {}", e.getMessage());
            return false; // Return false if there was an error during the insert
        }
    }

    // Get a list of all exercises
    public List<Exercise> getAllExercises() {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT * FROM exercises";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                boolean factory = rs.getBoolean("factory");
                exercises.add(new Exercise(id, name, factory));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }

    public int getOrCreateExerciseId(String exerciseName) {
        String selectSql = "SELECT id FROM exercises WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectSql)) {
            pstmt.setString(1, exerciseName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");  // Exercise exists, return the id
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking exercise: {}", e.getMessage(), e);
        }

        // If exercise doesn't exist, insert it
        String insertSql = "INSERT INTO exercises (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, exerciseName);
            pstmt.executeUpdate();

            // Get the generated ID of the new exercise
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);  // Return the new exercise id
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting exercise: {}", e.getMessage(), e);
        }
        return -1;  // In case of error
    }

    public int getExerciseIdByName(String exerciseName) {
        String query = "SELECT id FROM exercises WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, exerciseName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");  // Return the exerciseId
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting exercise ID for {}: {}", exerciseName, e.getMessage(), e);
        }
        return -1;  // Return -1 if the exercise doesn't exist
    }

    public String getExerciseNameById(int exerciseId) {
        String exerciseName = null;
        String sql = "SELECT name FROM exercises WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, exerciseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    exerciseName = rs.getString("name");
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving exercise name by id: {}", e.getMessage(), e);
        }

        return exerciseName;
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
