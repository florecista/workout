package info.matthewryan.workoutlogger.services;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import info.matthewryan.workoutlogger.ApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WorkoutService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutService.class);

    private List<ActivityRecord> sessionHistory = new ArrayList<>();
    private ApplicationSettings settings;
    private long currentSessionId;
    private long sessionStartTime;
    private long sessionEndTime;
    private ExerciseDao exerciseDao;

    public WorkoutService(ApplicationSettings settings, ExerciseDao exerciseDao) {
        this.settings = settings;
        this.exerciseDao = exerciseDao;
    }

    // Start a new session with a unique session ID (based on the current time)
    public void startNewSession() {
        this.currentSessionId = System.currentTimeMillis();  // Use the current timestamp as session ID
        this.sessionStartTime = System.currentTimeMillis();
        sessionHistory.clear();  // Start fresh for each session
    }

    // Record a new activity under the current session
    public void recordActivity(String activityName, int reps, double weight) {
        if (activityName == null || activityName.isEmpty()) {
            throw new IllegalArgumentException("Activity name cannot be null or empty");
        }
        if (reps <= 0 || weight <= 0) {
            throw new IllegalArgumentException("Reps and weight must be positive values");
        }

        // Look up exerciseId from ExerciseDao based on activityName
        int exerciseId = exerciseDao.getExerciseIdByName(activityName);
        if (exerciseId == -1) {
            logger.info(activityName + " does not exist in the database, adding it now");
            // If the exercise doesn't exist, create a new exercise record
            exerciseDao.insertExercise(activityName, false);
            exerciseId = exerciseDao.getExerciseIdByName(activityName);  // Fetch the newly inserted ID
        }
        else {
            logger.info(activityName + " id = " + exerciseId);
        }

        // Create ActivityRecord using the exerciseId and current sessionId
        ActivityRecord activityRecord = new ActivityRecord(exerciseId, reps, weight, System.currentTimeMillis(), currentSessionId);
        sessionHistory.add(activityRecord);
    }

    // Get the history of activities for the current session
    public List<ActivityRecord> getSessionHistoryForCurrentSession() {
        // Filter sessionHistory for records matching the current sessionId
        List<ActivityRecord> currentSessionActivities = new ArrayList<>();
        for (ActivityRecord record : sessionHistory) {
            if (record.getSessionId() == currentSessionId) {
                currentSessionActivities.add(record);
            }
        }
        return currentSessionActivities;
    }

    // Retrieve the personal best (highest weight) for a specific exercise in the current session
    public ActivityRecord getPersonalBest(String activityName) {
        int exerciseId = exerciseDao.getExerciseIdByName(activityName);
        if (exerciseId == -1) {
            return null;  // If the exercise does not exist in the database
        }

        return sessionHistory.stream()
                .filter(record -> record.getExerciseId() == exerciseId && record.getSessionId() == currentSessionId)
                .max(Comparator.comparingDouble(ActivityRecord::getWeight))
                .orElse(null);
    }

    // Get the total reps for a specific exercise in the current session
    public double getTotalReps(String activityName) {
        int exerciseId = exerciseDao.getExerciseIdByName(activityName);
        if (exerciseId == -1) {
            return 0.0;  // If the exercise does not exist in the database
        }

        return sessionHistory.stream()
                .filter(record -> record.getExerciseId() == exerciseId && record.getSessionId() == currentSessionId)
                .mapToInt(ActivityRecord::getReps)
                .sum();
    }

    // Get the average weight for a specific exercise in the current session
    public double getAverageWeight(String activityName) {
        int exerciseId = exerciseDao.getExerciseIdByName(activityName);
        if (exerciseId == -1) {
            return 0.0;  // If the exercise does not exist in the database
        }

        return sessionHistory.stream()
                .filter(record -> record.getExerciseId() == exerciseId && record.getSessionId() == currentSessionId)
                .mapToDouble(ActivityRecord::getWeight)
                .average()
                .orElse(0.0);
    }

    // End the current session
    public void endSession() {
        this.sessionEndTime = System.currentTimeMillis();
    }

    // Get the duration of the current session
    public long getSessionDuration() {
        return sessionEndTime - sessionStartTime;
    }

    // Get the current session ID
    public long getCurrentSessionId() {
        return currentSessionId;
    }

    public long getStartTime() {
        return sessionStartTime;
    }

    public long getEndTime() {
        return sessionEndTime;
    }
}
