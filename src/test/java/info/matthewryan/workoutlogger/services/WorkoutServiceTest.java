package info.matthewryan.workoutlogger.services;

import info.matthewryan.workoutlogger.ApplicationSettings;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.List;

class WorkoutServiceTest {

    private WorkoutService workoutService;
    private ExerciseDao mockExerciseDao;
    private ApplicationSettings mockSettings;

    @BeforeEach
    public void setUp() {
        // Create a mock ExerciseDao instance
        mockExerciseDao = mock(ExerciseDao.class);

        // Create a mock ApplicationSettings instance
        mockSettings = mock(ApplicationSettings.class);

        // Create the WorkoutService with the mocked dependencies
        workoutService = new WorkoutService(mockSettings, mockExerciseDao);

        // Start a new session before each test
        workoutService.startNewSession();
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
        assertEquals("Bench Press", mockExerciseDao.getExerciseNameById(sessionHistory.get(0).getExerciseId()), "Activity should be 'Bench Press'");
        assertEquals(10, sessionHistory.get(0).getReps(), "Reps should be 10.");
        assertEquals(100.0, sessionHistory.get(0).getWeight(), "Weight should be 100.0.");
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
    void testGetSessionDuration() {
        // Start session, simulate 1 second workout duration
        workoutService.startNewSession();

        // Instead of using Thread.sleep(), simulate session time end
        long simulatedEndTime = System.currentTimeMillis() + 1000;
        workoutService.endSession();  // End the session
        long sessionDuration = workoutService.getSessionDuration();

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
