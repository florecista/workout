package info.matthewryan.workoutlogger.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:test.db";  // Modify with actual DB URL
    private static final String USER = "";  // If needed
    private static final String PASSWORD = "";  // If needed

    public Connection getConnection() {
        try {
            // Establish and return the connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            return null;
        }
    }
}
