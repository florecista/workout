package info.matthewryan.workoutlogger.ui;

import info.matthewryan.workoutlogger.model.Session;

public class SessionManager {

    private static SessionManager instance;
    private Session currentSession;

    private SessionManager() {
        // Private constructor to prevent multiple instances
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void startSession() {
        currentSession = new Session(System.currentTimeMillis(), 0);
        // You could also save the session to a database or shared file if needed
    }

    public void endSession() {
        if (currentSession != null) {
            currentSession.setEndTimestamp(System.currentTimeMillis());
            // Update session in database or perform any cleanup
            currentSession = null;  // Reset the session once it's ended
        }
    }

    public Session getCurrentSession() {
        return currentSession;
    }
}
