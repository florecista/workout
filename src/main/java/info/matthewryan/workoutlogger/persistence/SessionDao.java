package info.matthewryan.workoutlogger.persistence;

import info.matthewryan.workoutlogger.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SessionDao {

    private static final Logger logger = LoggerFactory.getLogger(SessionDao.class);

    private Connection connection;

    public SessionDao(Connection connection) {
        this.connection = connection;
    }

    public void createSessionTable() {
        String sql = "CREATE TABLE IF NOT EXISTS session (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "start_timestamp LONG NOT NULL, " +
                "end_timestamp LONG)";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            logger.info("Session table created successfully or already exists.");
        } catch (SQLException e) {
            logger.error("Error creating session table: {}", e.getMessage(), e);
        }
    }

    public void startSession(Session session) {
        String sql = "INSERT INTO session (start_timestamp, end_timestamp) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, session.getStartTimestamp());
            stmt.setLong(2, session.getEndTimestamp());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    session.setId(rs.getInt(1));  // Get the generated ID
                }
            }
        }
        catch (SQLException e) {
            logger.error("Error creating session: {}", e.getMessage(), e);
        }
    }

    public void endSession(int sessionId, long endTimestamp) throws SQLException {
        String sql = "UPDATE session SET end_timestamp = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, endTimestamp);
            stmt.setInt(2, sessionId);
            stmt.executeUpdate();
        }
    }

    public Session getSessionById(int sessionId) throws SQLException {
        String sql = "SELECT * FROM session WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long startTimestamp = rs.getLong("start_timestamp");
                    long endTimestamp = rs.getLong("end_timestamp");
                    return new Session(sessionId, startTimestamp, endTimestamp, null); // We'll populate activityRecords later
                }
            }
        }
        return null;
    }
}
