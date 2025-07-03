package info.matthewryan.workoutlogger.model;

import java.util.Objects;

public class ActivityRecord {

    private long id;
    private String activity;
    private int reps;
    private double weight;
    private long timestamp;

    public ActivityRecord(String activity, int reps, double weight, long timestamp) {
        this.activity = activity;
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

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
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

    // Optional: Override toString() for easy logging and debugging
    @Override
    public String toString() {
        return "ActivityRecord{" +
                "id=" + id +
                ", activity='" + activity + '\'' +
                ", reps=" + reps +
                ", weight=" + weight +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ActivityRecord that = (ActivityRecord) obj;
        return id == that.id &&
                reps == that.reps &&
                Double.compare(that.weight, weight) == 0 &&
                timestamp == that.timestamp &&
                activity.equals(that.activity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, activity, reps, weight, timestamp);
    }
}
