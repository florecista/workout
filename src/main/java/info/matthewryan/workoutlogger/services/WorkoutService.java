package info.matthewryan.workoutlogger.services;

import info.matthewryan.workoutlogger.model.ActivityRecord;
import info.matthewryan.workoutlogger.persistence.ExerciseDao;
import info.matthewryan.workoutlogger.ApplicationSettings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WorkoutService {

    private List<ActivityRecord> sessionHistory = new ArrayList<>();
    private ApplicationSettings settings;
    private String currentSessionId;
    private long sessionStartTime;
    private long sessionEndTime;
    private ExerciseDao exerciseDao; // Added ExerciseDao to lookup exerciseId

    public WorkoutService(ApplicationSettings settings, ExerciseDao exerciseDao) {
        this.settings = settings;
        this.exerciseDao = exerciseDao;
    }

    public void startNewSession() {
        this.currentSessionId = "SESSION_" + System.currentTimeMillis();
        this.sessionStartTime = System.currentTimeMillis();
        sessionHistory.clear(); // Start fresh for each session
    }

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
            // If the exercise doesn't exist, create a new exercise record
            exerciseDao.insertExercise(activityName);
            exerciseId = exerciseDao.getExerciseIdByName(activityName);  // Fetch the newly inserted ID
        }

        // Create ActivityRecord using the exerciseId
        ActivityRecord activityRecord = new ActivityRecord(exerciseId, reps, weight, System.currentTimeMillis());
        sessionHistory.add(activityRecord);
    }

    public List<ActivityRecord> getSessionHistory() {
        return sessionHistory;
    }

    public ActivityRecord getPersonalBest(String activityName) {
        // Fetch the exerciseId for the given activityName
        int exerciseId = exerciseDao.getExerciseIdByName(activityName);

        if (exerciseId == -1) {
            return null;  // If the exercise does not exist in the database
        }

        return sessionHistory.stream()
                .filter(record -> record.getExerciseId() == exerciseId)
                .max(Comparator.comparingDouble(ActivityRecord::getWeight))
                .orElse(null);
    }

    public double getTotalReps(String activityName) {
        // Fetch the exerciseId for the given activityName
        int exerciseId = exerciseDao.getExerciseIdByName(activityName);

        if (exerciseId == -1) {
            return 0.0;  // If the exercise does not exist in the database
        }

        return sessionHistory.stream()
                .filter(record -> record.getExerciseId() == exerciseId)
                .mapToInt(ActivityRecord::getReps)
                .sum();
    }

    public double getAverageWeight(String activityName) {
        // Fetch the exerciseId for the given activityName
        int exerciseId = exerciseDao.getExerciseIdByName(activityName);

        if (exerciseId == -1) {
            return 0.0;  // If the exercise does not exist in the database
        }

        return sessionHistory.stream()
                .filter(record -> record.getExerciseId() == exerciseId)
                .mapToDouble(ActivityRecord::getWeight)
                .average()
                .orElse(0.0);
    }

    public void endSession() {
        this.sessionEndTime = System.currentTimeMillis();
    }

    public long getSessionDuration() {
        return sessionEndTime - sessionStartTime;
    }

    public String getCurrentSessionId() {
        return currentSessionId;
    }
}
