package info.matthewryan.workoutlogger;

import info.matthewryan.workoutlogger.persistence.WorkoutDao;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkoutDaoTest extends UnitTestBase {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutDaoTest.class);

    private WorkoutDao workoutDao;
    private ExerciseDao exerciseDao;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp(); // Calls the parent class's setUp method for common setup

        // Pass the shared connection to DAOs
        workoutDao = new WorkoutDao(connection);
        exerciseDao = new ExerciseDao(connection);

        // Create tables and ensure the "order" column exists
        workoutDao.createWorkoutTable();
        workoutDao.createWorkoutExercisesTable();
        workoutDao.createExerciseTable(); // Ensure the exercises table is created

        // Add the "order" column to workout_exercises table if it doesn't exist
        ensureOrderColumnExists();

        // Preload default exercises
        preloadDefaultExercises();

        // Insert workouts to test
        workoutDao.insertWorkout("Leg Day");

    }

    private void ensureOrderColumnExists() {
        String sql = "PRAGMA table_info(workout_exercises);";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            boolean orderColumnExists = false;
            while (rs.next()) {
                if ("order".equals(rs.getString("name"))) {
                    orderColumnExists = true;
                    break;
                }
            }
            if (!orderColumnExists) {
                String alterSql = "ALTER TABLE workout_exercises ADD COLUMN \"order\" INTEGER;";
                try (Statement alterStmt = connection.createStatement()) {
                    alterStmt.executeUpdate(alterSql);
                    logger.info("Added 'order' column to workout_exercises table.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking or adding 'order' column: {}", e.getMessage(), e);
        }
    }

    private void preloadDefaultExercises() {
        // Insert default exercises (make sure to include 'Squats' here)
        String[] defaultExercises = {
                "Squats", "Leg Press", "Deadlift", "Lunges", "Calf Raises",
                "Leg Curl", "Leg Extension"
        };
        for (String exercise : defaultExercises) {
            exerciseDao.insertExercise(exercise, true);
        }
    }

    @Test
    void testWorkoutExercisesAssociation() {
        // Look up the exercise IDs by name
        int squatsId = workoutDao.getExerciseIdByName("Squats");
        int legPressId = workoutDao.getExerciseIdByName("Leg Press");

        // Check if the exercises are found
        assertNotEquals(squatsId, -1, "Squats should exist in the database");
        assertNotEquals(legPressId, -1, "Leg Press should exist in the database");

        // Add exercises to the workout in a specific order
        workoutDao.addExerciseToWorkout(1, squatsId, 1);  // workout 1, squats, order 1
        workoutDao.addExerciseToWorkout(1, legPressId, 2);  // workout 1, leg press, order 2

        // Retrieve exercises for the workout and check if they are ordered correctly
        List<String> exercises = workoutDao.getExercisesForWorkout(1);

        // Verify exercises are in the correct order
        assertEquals(2, exercises.size());  // Only two exercises should be added (not three)
        assertEquals("Squats", exercises.get(0));  // Verify "Squats" is in the first position
        assertEquals("Leg Press", exercises.get(1));  // Verify "Leg Press" is in the second position
    }
}
