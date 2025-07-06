package info.matthewryan.workoutlogger.model;

import java.util.List;

public class Session {

    private int id;
    private long startTimestamp;
    private long endTimestamp;
    private List<ActivityRecord> activityRecords;

    public Session(int id, long startTimestamp, long endTimestamp, List<ActivityRecord> activityRecords) {
        this.id = id;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.activityRecords = activityRecords;
    }

    public Session(long startTimestamp, long endTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.activityRecords = null;  // Default to null since no activity records are assigned yet
    }

    public int getId() {
        return id;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public List<ActivityRecord> getActivityRecords() {
        return activityRecords;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public void setActivityRecords(List<ActivityRecord> activityRecords) {
        this.activityRecords = activityRecords;
    }
}
