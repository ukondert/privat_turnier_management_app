package com.turniermanagement;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the main application view.
 */
public class MainController implements Initializable {

    @FXML
    private Button participantManagementBtn;
    
    @FXML
    private Button tournamentPlanningBtn;
    
    @FXML
    private Button tournamentExecutionBtn;
    
    @FXML
    private Button statisticsBtn;
    
    @FXML
    private Button homeBtn;
    
    @FXML
    private StackPane contentArea;
    
    @FXML
    private VBox homeContent;
    
    private Node participantManagementView;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load the participant management view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("participant-management.fxml"));
            participantManagementView = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Show the participant management view
     */
    @FXML
    private void showParticipantManagement() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(participantManagementView);
        highlightButton(participantManagementBtn);
    }
    
    /**
     * Show the tournament planning view (placeholder)
     */
    @FXML
    private void showTournamentPlanning() {
        // This would load the tournament planning view
        contentArea.getChildren().clear();
        // Placeholder - would load actual view
        contentArea.getChildren().add(createPlaceholderContent("Turnierplanung"));
        highlightButton(tournamentPlanningBtn);
    }
    
    /**
     * Show the tournament execution view (placeholder)
     */
    @FXML
    private void showTournamentExecution() {
        // This would load the tournament execution view
        contentArea.getChildren().clear();
        // Placeholder - would load actual view
        contentArea.getChildren().add(createPlaceholderContent("Turnierdurchführung"));
        highlightButton(tournamentExecutionBtn);
    }
    
    /**
     * Show the statistics view (placeholder)
     */
    @FXML
    private void showStatistics() {
        // This would load the statistics view
        contentArea.getChildren().clear();
        // Placeholder - would load actual view
        contentArea.getChildren().add(createPlaceholderContent("Ergebnisse und Statistiken"));
        highlightButton(statisticsBtn);
    }
    
    /**
     * Show the home view
     */
    @FXML
    private void showHome() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(homeContent);
        highlightButton(homeBtn);
    }
    
    /**
     * Highlight the active navigation button
     */
    private void highlightButton(Button activeButton) {
        // Reset all buttons to default style
        for (Node node : ((VBox) activeButton.getParent()).getChildren()) {
            if (node instanceof Button) {
                ((Button) node).setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
            }
        }
        // Highlight the active button
        activeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
    }
    
    /**
     * Create a placeholder content for views not yet implemented
     */
    private VBox createPlaceholderContent(String title) {
        VBox placeholder = new VBox();
        placeholder.setStyle("-fx-padding: 20; -fx-spacing: 10;");
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        javafx.scene.control.Label infoLabel = new javafx.scene.control.Label("Diese Ansicht wird noch implementiert.");
        placeholder.getChildren().addAll(titleLabel, infoLabel);
        return placeholder;
    }
}