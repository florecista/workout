package info.matthewryan.workoutlogger;

import info.matthewryan.workoutlogger.persistence.ActivityDao;
import info.matthewryan.workoutlogger.persistence.ApplicationSettingsDao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Application {

    private ApplicationSettings settings;

    public void initialize() {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            ApplicationSettingsDao settingsDao = new ApplicationSettingsDao(connection);
            settings = settingsDao.loadSettings();  // Load settings from the database

            // Pass the settings to the ActivityDao
            ActivityDao activityDao = new ActivityDao(connection);

            // Now you can use the activityDao to insert activities with the correct settings
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ApplicationSettings getSettings() {
        return settings;
    }

    // Other application methods
}
