package info.matthewryan.workoutlogger.services;

import info.matthewryan.workoutlogger.model.ActivityRecord;
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

    // Constructor that accepts ApplicationSettings
    public WorkoutService(ApplicationSettings settings) {
        this.settings = settings;
    }

    public void startNewSession() {
        this.currentSessionId = "SESSION_" + System.currentTimeMillis();
        this.sessionStartTime = System.currentTimeMillis();
        sessionHistory.clear(); // Start fresh for each session
    }

    public void recordActivity(String activity, int reps, double weight) {
        if (activity == null || activity.isEmpty()) {
            throw new IllegalArgumentException("Activity name cannot be null or empty");
        }
        if (reps <= 0 || weight <= 0) {
            throw new IllegalArgumentException("Reps and weight must be positive values");
        }

        ActivityRecord activityRecord = new ActivityRecord(activity, reps, weight, System.currentTimeMillis());
        sessionHistory.add(activityRecord);
    }

    public List<ActivityRecord> getSessionHistory() {
        return sessionHistory;
    }

    public ActivityRecord getPersonalBest(String activity) {
        return sessionHistory.stream()
                .filter(record -> record.getActivity().equals(activity))
                .max(Comparator.comparingDouble(ActivityRecord::getWeight))
                .orElse(null);
    }

    public double getTotalReps(String activity) {
        return sessionHistory.stream()
                .filter(record -> record.getActivity().equals(activity))
                .mapToInt(ActivityRecord::getReps)
                .sum();
    }

    public double getAverageWeight(String activity) {
        return sessionHistory.stream()
                .filter(record -> record.getActivity().equals(activity))
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
