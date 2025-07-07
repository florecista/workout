package info.matthewryan.workoutlogger.ui;

import javafx.scene.layout.BorderPane;

public class SettingsScreen {

    public BorderPane getRoot() {
        // Create a BorderPane as the root for the settings screen
        BorderPane settingsPane = new BorderPane();

        // For now, we leave the settings pane empty
        settingsPane.setCenter(new javafx.scene.control.Label("Settings Screen - Placeholder"));

        return settingsPane;
    }
}
