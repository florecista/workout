package info.matthewryan.workoutlogger.services;

import info.matthewryan.workoutlogger.ApplicationSettings;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.model.Exercise;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WorkoutServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutServiceTest.class);

    private WorkoutService workoutService;
    private ExerciseDao mockExerciseDao;
    private ApplicationSettings mockSettings;

    // Default exercises (from your provided code)
    private final String[] defaultExercises = {
            "Abdominal Twists", "Abmat Crunches", "Back Extension", "Back Squat",
            "Barbell Chest Squat", "Bench Press", "Bicep Curl", "Bulgarian Split Squat",
            "Cable Side Deltoid Pulls", "Deadlift", "Dumbbell Bench Press", "Dumbbell Chin Row",
            "Dumbbell Side Raises", "Dumbbell Shoulder Press", "Lat Pulldown", "Leg Press",
            "Parallel Dips", "Pull-Up", "Push-Up", "Seated Row", "Tricep Extension"
    };

    @BeforeEach
    public void setUp() {
        // Create a mock ExerciseDao instance
        mockExerciseDao = mock(ExerciseDao.class);

        // Set up the behavior of the mock DAO
        Mockito.when(mockExerciseDao.getExerciseIdByName("Bench Press")).thenReturn(1);  // ID for "Bench Press"
        Mockito.when(mockExerciseDao.getExerciseNameById(1)).thenReturn("Bench Press");  // Name for ID 1
        // If insertExercise should return a boolean, we return true (simulating a successful insertion)
        Mockito.when(mockExerciseDao.insertExercise("Bench Press", false)).thenReturn(true);  // Simulate successful insert

        // Set up the WorkoutService with the mocked ExerciseDao
        workoutService = new WorkoutService(mockSettings, mockExerciseDao);

        // Start a new session before each test
        workoutService.startNewSession();
    }

    // Preload the default exercises into the Exercise table (mocked)
    private void preloadDefaultExercises() {
        final Map<Integer, String> exerciseIdToNameMap = new HashMap<>(); // Map to track exercise name by ID
        final int[] currentId = {1}; // ID starts at 1 (to avoid 0)

        // Loop through each default exercise and set up the mocks
        for (String exercise : defaultExercises) {
            // Mock insertExercise to return a unique ID for each exercise
            Mockito.when(mockExerciseDao.insertExercise(exercise, true)).thenAnswer(invocation -> {
                int id = currentId[0];  // Get the current ID
                exerciseIdToNameMap.put(id, exercise);  // Associate the exercise with the ID
                currentId[0]++;  // Increment for the next exercise
                return id;  // Return the ID for the exercise
            });

            // Mock getExerciseNameById to return the exercise name for the given ID
            Mockito.when(mockExerciseDao.getExerciseNameById(Mockito.anyInt())).thenAnswer(invocation -> {
                int id = invocation.getArgument(0);  // Get the ID from the method argument
                return exerciseIdToNameMap.get(id);  // Return the associated name for the ID
            });
        }
    }

    @Test
    void testStartNewSession() {
        // Ensure that the session ID is generated correctly
        long sessionId = workoutService.getCurrentSessionId();
        assertTrue(sessionId > 0, "Session ID should be a positive long value.");
    }

    @Test
    void testRecordActivity() {
        // Record an activity
        workoutService.recordActivity("Bench Press", 10, 100.0);

        // Retrieve session history and verify the activity is recorded
        List<ActivityRecord> sessionHistory = workoutService.getSessionHistoryForCurrentSession();
        assertEquals(1, sessionHistory.size(), "There should be one recorded activity.");
        ActivityRecord activityRecord = sessionHistory.get(0);

        // Verify that the exercise ID returned corresponds to the expected exercise
        int exerciseId = activityRecord.getExerciseId();

        // Verify that the ID corresponds to the "Bench Press"
        String exerciseName = mockExerciseDao.getExerciseNameById(exerciseId);
        assertEquals("Bench Press", exerciseName, "Activity should be 'Bench Press'");

        // Assert the expected values for reps and weight
        assertEquals(10, activityRecord.getReps(), "Reps should be 10.");
        assertEquals(100.0, activityRecord.getWeight(), "Weight should be 100.0.");
    }

    @Test
    void testPersonalBest() {
        // Record activities for personal best testing
        workoutService.recordActivity("Bench Press", 10, 100.0);
        workoutService.recordActivity("Bench Press", 8, 120.0);  // Better weight

        // Retrieve the personal best for Bench Press
        ActivityRecord personalBest = workoutService.getPersonalBest("Bench Press");
        assertNotNull(personalBest, "Personal best should not be null.");
        assertEquals(120.0, personalBest.getWeight(), "Personal best weight should be 120.0.");
    }

    @Test
    void testGetSessionDuration() throws InterruptedException {
        // Start session, simulate 1 second workout duration
        workoutService.startNewSession();
        long startTime = workoutService.getStartTime();  // Assuming you have a method to get start time for debugging
        Thread.sleep(1000);
        workoutService.endSession();  // End the session
        long sessionDuration = workoutService.getSessionDuration();
        long endTime = workoutService.getEndTime();  // Assuming you have a method to get end time for debugging

        // Debugging: Print out the times
        logger.info("Start Time: {}", startTime);
        logger.info("End Time: {}", endTime);
        logger.info("Session Duration: {}", sessionDuration);

        // Assert that session duration is close to the simulated duration
        assertTrue(sessionDuration >= 1000, "Session duration should be greater than or equal to 1000 ms.");
    }

    @Test
    void testGetTotalReps() {
        // Record some activities
        workoutService.recordActivity("Bench Press", 10, 100.0);
        workoutService.recordActivity("Bench Press", 5, 100.0);

        // Get the total reps for "Bench Press"
        double totalReps = workoutService.getTotalReps("Bench Press");
        assertEquals(15, totalReps, "Total reps for 'Bench Press' should be 15.");
    }

    @Test
    void testGetAverageWeight() {
        // Record activities with different weights
        workoutService.recordActivity("Bench Press", 10, 100.0);
        workoutService.recordActivity("Bench Press", 8, 120.0);

        // Get the average weight for "Bench Press"
        double averageWeight = workoutService.getAverageWeight("Bench Press");
        assertEquals(110.0, averageWeight, "Average weight for 'Bench Press' should be 110.0.");
    }

    @Test
    void testInvalidActivityRecording() {
        // Attempt to record invalid activity (negative weight or reps)
        assertThrows(IllegalArgumentException.class, () -> {
            workoutService.recordActivity("Invalid Activity", -5, -10.0);
        }, "Recording invalid activity should throw an exception.");
    }
}
