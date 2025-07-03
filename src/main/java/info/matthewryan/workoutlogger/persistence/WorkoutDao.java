package info.matthewryan.workoutlogger.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkoutDao {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutDao.class);
    private Connection connection;

    public WorkoutDao(Connection connection) {
        this.connection = connection;
    }

    public void createWorkoutTable() {
        String sql = "CREATE TABLE IF NOT EXISTS workouts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Workouts table created successfully or already exists.");
        } catch (SQLException e) {
            logger.error("Error creating workouts table: {}", e.getMessage(), e);
        }
    }

    public void createWorkoutExercisesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS workout_exercises (" +
                "workout_id INTEGER, " +
                "exercise_id INTEGER, " +
                "\"order\" INTEGER, " +  // Quoted 'order' to avoid conflict with SQL reserved keyword
                "PRIMARY KEY (workout_id, exercise_id));";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Workout Exercises mapping table created successfully or already exists.");
        } catch (SQLException e) {
            logger.error("Error creating workout_exercises table: {}", e.getMessage(), e);
        }
    }

    public void createExerciseTable() {
        String sql = "CREATE TABLE IF NOT EXISTS exercises (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Exercises table created successfully or already exists.");

            // Test insertion immediately after creating the table to verify it works
            stmt.executeUpdate("INSERT INTO exercises (name) VALUES ('Test Exercise')");
            logger.info("Inserted a test exercise to verify the table.");
        } catch (SQLException e) {
            logger.error("Error creating exercises table: {}", e.getMessage(), e);
        }
    }

    public void insertWorkout(String workoutName) {
        String sql = "INSERT INTO workouts (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, workoutName);
            pstmt.executeUpdate();
            logger.info("Inserted workout: {}", workoutName);
        } catch (SQLException e) {
            logger.error("Error inserting workout: {}", e.getMessage(), e);
        }
    }

    public void addExerciseToWorkout(int workoutId, int exerciseId, int order) {
        String sql = "INSERT INTO workout_exercises (workout_id, exercise_id, \"order\") VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, workoutId);
            pstmt.setInt(2, exerciseId);
            pstmt.setInt(3, order);
            pstmt.executeUpdate();
            logger.info("Exercise {} added to workout {}", exerciseId, workoutId);
        } catch (SQLException e) {
            logger.error("Error adding exercise to workout: {}", e.getMessage(), e);
        }
    }

    public int getExerciseIdByName(String exerciseName) {
        String sql = "SELECT id FROM exercises WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, exerciseName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    logger.error("Exercise not found: {}", exerciseName);
                    return -1;  // Return -1 if not found
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting exercise ID by name: {}", e.getMessage(), e);
            return -1;
        }
    }

    public List<String> getExercisesForWorkout(int workoutId) {
        String sql = "SELECT e.name FROM exercises e " +
                "JOIN workout_exercises we ON e.id = we.exercise_id " +
                "WHERE we.workout_id = ? ORDER BY we.\"order\"";
        List<String> exercises = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, workoutId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    exercises.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving exercises for workout {}: {}", workoutId, e.getMessage(), e);
        }
        return exercises;
    }
}
