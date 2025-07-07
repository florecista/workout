package info.matthewryan.workoutlogger.model;

import java.util.Objects;

public class ActivityRecord {

    private long id;
    private int exerciseId;
    private int reps;
    private double weight;
    private long timestamp;
    private long sessionId;  // Add sessionId field

    // Updated constructor to include sessionId
    public ActivityRecord(int exerciseId, int reps, double weight, long timestamp, long sessionId) {
        this.exerciseId = exerciseId;
        this.reps = reps;
        this.weight = weight;
        this.timestamp = timestamp;
        this.sessionId = sessionId;  // Set the sessionId
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getExerciseId() {
        return exerciseId;  // Foreign key referencing the exercise
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getSessionId() {
        return sessionId;  // Getter for sessionId
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;  // Setter for sessionId
    }

    // Override toString() for easy logging and debugging
    @Override
    public String toString() {
        return String.format("ActivityRecord{id=%d, exerciseId=%d, reps=%d, weight=%.2f, timestamp=%d, sessionId=%d}",
                id, exerciseId, reps, weight, timestamp, sessionId);
    }

    // Override equals() to compare ActivityRecord objects based on their fields
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ActivityRecord that = (ActivityRecord) obj;
        return id == that.id &&
                exerciseId == that.exerciseId &&
                reps == that.reps &&
                Double.compare(that.weight, weight) == 0 &&
                timestamp == that.timestamp &&
                sessionId == that.sessionId;  // Compare sessionId
    }

    // Override hashCode() to generate a unique hash code based on the fields
    @Override
    public int hashCode() {
        return Objects.hash(id, exerciseId, reps, weight, timestamp, sessionId);  // Include sessionId in hashCode
    }
}
