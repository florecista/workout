package info.matthewryan.workoutlogger.model;

import java.util.Objects;

public class ActivityRecord {

    private long id;
    private int exerciseId;
    private int reps;
    private double weight;
    private long timestamp;

    public ActivityRecord(int exerciseId, int reps, double weight, long timestamp) {
        this.exerciseId = exerciseId;
        this.reps = reps;
        this.weight = weight;
        this.timestamp = timestamp;
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

    // Override toString() for easy logging and debugging
    @Override
    public String toString() {
        return String.format("ActivityRecord{id=%d, exerciseId=%d, reps=%d, weight=%.2f, timestamp=%d}",
                id, exerciseId, reps, weight, timestamp);
    }

    // Override equals() to compare ActivityRecord objects based on their fields
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // If both references are the same
        if (obj == null || getClass() != obj.getClass()) return false; // Null check and class type check

        // Cast the object to ActivityRecord to compare its fields
        ActivityRecord that = (ActivityRecord) obj;
        return id == that.id &&              // Compare id
                exerciseId == that.exerciseId &&  // Compare exerciseId
                reps == that.reps &&            // Compare reps
                Double.compare(that.weight, weight) == 0 && // Compare weight
                timestamp == that.timestamp;    // Compare timestamp
    }

    // Override hashCode() to generate a unique hash code based on the fields
    @Override
    public int hashCode() {
        return Objects.hash(id, exerciseId, reps, weight, timestamp); // Use Objects.hash to generate the hash code
    }
}
