package info.matthewryan.workoutlogger;

import info.matthewryan.workoutlogger.model.Exercise;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ExerciseDaoTest extends UnitTestBase {

    private ExerciseDao exerciseDao;

    private final String[] defaultExercises = {
            "Abdominal Twists", "Abmat Crunches", "Back Extension", "Back Squat",
            "Barbell Chest Squat", "Bench Press", "Bicep Curl", "Bulgarian Split Squat",
            "Cable Side Deltoid Pulls", "Deadlift", "Dumbbell Bench Press", "Dumbbell Chin Row",
            "Dumbbell Side Raises", "Dumbbell Shoulder Press", "Lat Pulldown", "Leg Press",
            "Parallel Dips", "Pull-Up", "Push-Up", "Seated Row", "Tricep Extension"
    };

    private final String[] volumeGroups = {
            "Arms", "Back", "Biceps", "Chest", "Hamstrings", "Legs", "Quads", "Triceps"
    };

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp(); // Call the parent class's setUp method to delete the database and set up the connection
        exerciseDao = new ExerciseDao(connection);
        exerciseDao.createExerciseTable();  // Setup the Exercise table
        exerciseDao.createVolumeGroupTable(); // Create Volume Group table
        preloadVolumeGroups(); // Preload volume groups
        preloadDefaultExercises(); // Preload default exercises
    }

    private void preloadVolumeGroups() {
        // Preload the volume group reference data
        for (String group : volumeGroups) {
            exerciseDao.insertVolumeGroup(group);
        }
    }

    private void preloadDefaultExercises() {
        for (String exercise : defaultExercises) {
            exerciseDao.insertExercise(exercise, false);
        }
    }

    @Test
    void testDefaultExercisesLoaded() {
        // Load all exercises
        List<String> exercises = exerciseDao.getAllExercises();
        assertNotNull(exercises, "Exercises list should not be null");

        // Verify the correct number of exercises is loaded
        assertEquals(21, exercises.size(), "Exercise count should match");

        for (String exercise : defaultExercises) {
            assertTrue(exercises.contains(exercise), "Exercise list should contain: " + exercise);
        }
    }

    @Test
    void testAddCustomExercises() {
        String[] customExercises = {
                "Bench Press 5's", "Bench Press Inclined 5's", "Dumbbell Bicep Curl", "Dumbbell Front Raise Single",
                "Dumbbell Front Raises", "Dumbbell Infinity Figure", "Dumbbell Shoulder Press Standing",
                "Front Dumbbell Raise", "Hanging Bent Knee Leg Raises", "Plank", "Rack Pull", "Reverse Grip Lat Pulldown",
                "Skipping", "Trapbar Deadlift"
        };

        // Add custom exercises
        for (String customExercise : customExercises) {
            exerciseDao.insertExercise(customExercise, false);
        }

        // Verify that the custom exercises have been added
        List<Exercise> exercises = exerciseDao.getAllExercises();
        List<String> exerciseNames = exercises.stream()
                .map(Exercise::getName)
                .collect(Collectors.toList());
        for (String customExercise : exerciseNames) {
            assertTrue(exercises.contains(customExercise), "Custom exercise list should contain: " + customExercise);
        }
    }

    @Test
    void testAddDuplicateExercise() {
        // Given: An exercise name that already exists in the database
        String exerciseName = "Squats";

        // First, add the exercise to ensure it exists
        boolean result1 = insertAndCheckExercise(exerciseName);
        assertTrue(result1, "First attempt should add the exercise");

        // When: We try to add the same exercise again
        boolean result2 = insertAndCheckExercise(exerciseName);

        // Then: The exercise should not be added again (INSERT OR IGNORE will prevent it)
        assertFalse(result2, "Exercise should not be added again if it already exists");
    }

    @Test
    void testAddExercise_nameDoesNotExist() throws SQLException {
        // Given: An exercise name that doesn't exist in the database
        String exerciseName = "Squats";

        // When: We add this exercise to the database
        boolean result = insertAndCheckExercise(exerciseName);

        // Then: The exercise should be added successfully, returning true
        assertTrue(result, "Exercise should be added successfully");
    }

    @Test
    void testAddNullExercise() {
        // Given: A null exercise name
        String exerciseName = null;

        // When: We try to add a null exercise to the database
        boolean result = insertAndCheckExercise(exerciseName);

        // Then: The result should be false because null values should not be allowed
        assertFalse(result, "Null exercise should not be added");
    }

    @Test
    void testAddEmptyExercise() {
        // Given: An empty exercise name
        String exerciseName = "";

        // When: We try to add an empty exercise to the database
        boolean result = insertAndCheckExercise(exerciseName);

        // Then: The result should be false because empty values should not be allowed
        assertFalse(result, "Empty exercise should not be added");
    }

    private boolean insertAndCheckExercise(String exerciseName) {
        return exerciseDao.insertExercise(exerciseName, false);
    }
}
