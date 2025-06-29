package info.matthewryan.workoutlogger.services;

import info.matthewryan.workoutlogger.ApplicationSettings;
import info.matthewryan.workoutlogger.model.ActivityRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class WorkoutServiceTest {

    private WorkoutService workoutService;
    private ApplicationSettings mockSettings;

    @BeforeEach
    public void setUp() {
        // Create a mock ApplicationSettings instance or use real settings if necessary
        mockSettings = new ApplicationSettings();
        workoutService = new WorkoutService(mockSettings);
        workoutService.startNewSession();  // Start a new session before each test
    }

    @Test
    void testStartNewSession() {
        // Ensure that the session ID is generated
        String sessionId = workoutService.getCurrentSessionId();
        assertNotNull(sessionId, "Session ID should be generated.");
    }

    @Test
    void testRecordActivity() {
        // Record an activity
        workoutService.recordActivity("Bench Press", 10, 100.0);

        // Retrieve session history and verify the activity is recorded
        List<ActivityRecord> sessionHistory = workoutService.getSessionHistory();
        assertEquals(1, sessionHistory.size(), "There should be one recorded activity.");
        assertEquals("Bench Press", sessionHistory.get(0).getActivity(), "Activity should be 'Bench Press'");
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
    void testGetSessionDuration() throws InterruptedException {
        // Start session, then sleep for 1 second to simulate workout duration
        workoutService.startNewSession();
        Thread.sleep(1000);  // Simulating workout time

        // End the session and check the duration
        workoutService.endSession();
        long sessionDuration = workoutService.getSessionDuration();
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
