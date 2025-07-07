package info.matthewryan.workoutlogger.model;

public class Exercise {

    private int id;
    private String name;
    private boolean factory;

    // Constructor
    public Exercise(int id, String name, boolean factory) {
        this.id = id;
        this.name = name;
        this.factory = factory;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFactory() {
        return factory;
    }

    public void setFactory(boolean factory) {
        this.factory = factory;
    }

    @Override
    public String toString() {
        return "Exercise{id=" + id + ", name='" + name + '\'' + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Exercise exercise = (Exercise) obj;
        return id == exercise.id && name.equals(exercise.name);
    }

    @Override
    public int hashCode() {
        return 31 * id + (name != null ? name.hashCode() : 0);
    }
}
