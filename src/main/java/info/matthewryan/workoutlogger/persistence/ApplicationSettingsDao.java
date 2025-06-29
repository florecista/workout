package info.matthewryan.workoutlogger.persistence;

import info.matthewryan.workoutlogger.ApplicationSettings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ApplicationSettingsDao {

    private Connection connection;

    public ApplicationSettingsDao(Connection connection) {
        this.connection = connection;
    }

    public ApplicationSettings loadSettings() throws SQLException {
        String sql = "SELECT prefer_metric_units FROM settings LIMIT 1"; // Assume settings table has this column
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                boolean preferMetricUnits = rs.getBoolean("prefer_metric_units");
                return new ApplicationSettings();
            } else {
                throw new SQLException("Settings not found");
            }
        }
    }
}
