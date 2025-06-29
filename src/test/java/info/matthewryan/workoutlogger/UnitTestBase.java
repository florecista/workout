package info.matthewryan.workoutlogger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitTestBase {
    protected Connection connection;
    protected static final Logger logger = LoggerFactory.getLogger(UnitTestBase.class);

    // Method to delete the database file if it exists
    private void deleteDatabaseIfExists() {
        // Delete the database file if it exists
        File dbFile = new File("test.db");
        if (dbFile.exists()) {
            if (dbFile.delete()) {
                logger.info("Deleted existing test.db file.");
            } else {
                logger.error("Failed to delete existing test.db file.");
            }
        }
    }

    // Method to set up the database connection
    protected void setUpDatabase() {
        try {
            // Ensure connection to disk-based SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            logger.info("Connection established: " + connection);
        } catch (SQLException e) {
            logger.error("Failed to establish database connection", e);
        }
    }

    // @BeforeEach ensures this method is called before each test method
    @BeforeEach
    protected void setUp() {
        // Delete the database before each test to ensure a clean slate
        deleteDatabaseIfExists();
        setUpDatabase();
    }

    // @AfterEach ensures this method is called after each test method to close the connection
    @AfterEach
    public void tearDown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed.");
            }
        } catch (SQLException e) {
            logger.error("Failed to close the database connection", e);
        }
    }
}
