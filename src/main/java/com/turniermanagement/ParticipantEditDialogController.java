package com.turniermanagement;

import com.turniermanagement.model.Player;
import com.turniermanagement.service.PlayerService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;

public class ParticipantEditDialogController {

    @FXML
    private Label dialogTitleLabel;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField gamesField;
    
    @FXML
    private TextField tournamentsField;
    
    @FXML
    private TextField rankingsField;
    
    @FXML
    private CheckBox gamesEditCheckbox;
    
    @FXML
    private CheckBox tournamentsEditCheckbox;
    
    @FXML
    private CheckBox rankingsEditCheckbox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private ParticipantManagementController.Participant participant;
    private Player playerModel;
    private Consumer<ParticipantManagementController.Participant> onSaveHandler;
    private PlayerService playerService;
    private boolean isNewParticipant;
    
    @FXML
    private void initialize() {
        // UI-Komponenten initialisieren
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        
        // Felder standardmäßig deaktivieren (außer Name und Email)
        gamesField.setDisable(true);
        tournamentsField.setDisable(true);
        rankingsField.setDisable(true);
        
        // Listener für Checkboxen hinzufügen
        gamesEditCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            gamesField.setDisable(!newVal);
        });
        
        tournamentsEditCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            tournamentsField.setDisable(!newVal);
        });
        
        rankingsEditCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            rankingsField.setDisable(!newVal);
        });
    }
    
    /**
     * Setzt den PlayerService zur Interaktion mit der Datenbank
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }
    
    /**
     * Setzt den zu bearbeitenden Teilnehmer und füllt die Felder aus
     */
    public void setParticipant(ParticipantManagementController.Participant participant) {
        this.participant = participant;
        this.isNewParticipant = (participant == null);
        
        if (participant != null) {
            // Bestehenden Teilnehmer bearbeiten
            dialogTitleLabel.setText("Teilnehmer bearbeiten");
            
            try {
                // Lade den Spieler aus der Datenbank
                Optional<Player> player = playerService.findPlayerById(participant.getPlayerId());
                if (player.isPresent()) {
                    this.playerModel = player.get();
                    
                    // Felder mit den Daten des Spielers füllen
                    nameField.setText(playerModel.getName());
                    emailField.setText(playerModel.getEmail());
                    gamesField.setText(participant.getGames());
                    tournamentsField.setText(participant.getTournaments());
                    rankingsField.setText(participant.getRankings());
                } else {
                    showErrorAlert("Fehler", "Der Teilnehmer konnte nicht gefunden werden.");
                    handleCancel();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAlert("Datenbankfehler", "Fehler beim Laden des Teilnehmers: " + e.getMessage());
                handleCancel();
            }
        } else {
            // Neuen Teilnehmer hinzufügen
            dialogTitleLabel.setText("Teilnehmer hinzufügen");
            
            // Leere Felder für neuen Teilnehmer
            nameField.setText("");
            emailField.setText("");
            gamesField.setText("0/0");
            tournamentsField.setText("0");
            rankingsField.setText("");
            
            this.playerModel = new Player();
        }
        
        // Checkboxen zurücksetzen
        gamesEditCheckbox.setSelected(false);
        tournamentsEditCheckbox.setSelected(false);
        rankingsEditCheckbox.setSelected(false);
    }
    
    /**
     * Setzt den Handler, der aufgerufen wird, wenn ein Teilnehmer gespeichert wird
     */
    public void setOnSaveHandler(Consumer<ParticipantManagementController.Participant> handler) {
        this.onSaveHandler = handler;
    }
    
    /**
     * Speichert die Änderungen oder erstellt einen neuen Teilnehmer
     */
    private void handleSave() {
        // Validierung durchführen
        if (!validateInput()) {
            return;
        }
        
        try {
            // Werte in das Spieler-Modell übertragen
            playerModel.setName(nameField.getText());
            playerModel.setEmail(emailField.getText());
            
            // Spieler erstellen oder aktualisieren
            if (isNewParticipant) {
                // Neuen Spieler erstellen
                Player createdPlayer = playerService.createPlayer(nameField.getText(), emailField.getText());
                playerModel = createdPlayer;
            } else {
                // Bestehenden Spieler aktualisieren
                Player updatedPlayer = playerService.updatePlayer(playerModel);
                playerModel = updatedPlayer;
            }
            
            // Participant-Objekt für die UI erstellen
            String gamesStr = playerModel.getGamesWon() + "/" + playerModel.getGamesLost();
            String tournamentsStr = String.valueOf(playerModel.getTournaments().size());
            String rankingsStr = participant != null ? participant.getRankings() : "";
            
            ParticipantManagementController.Participant savedParticipant = 
                new ParticipantManagementController.Participant(
                    playerModel.getId(), 
                    playerModel.getName(), 
                    playerModel.getEmail(), 
                    gamesStr, 
                    tournamentsStr, 
                    rankingsStr
                );
            
            // Callback aufrufen
            if (onSaveHandler != null) {
                onSaveHandler.accept(savedParticipant);
            }
            
            // Dialog schließen
            closeDialog();
            
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            showErrorAlert("Fehler beim Speichern", "Der Teilnehmer konnte nicht gespeichert werden: " + e.getMessage());
        }
    }
    
    /**
     * Validiert die Eingabefelder
     */
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage.append("Der Name darf nicht leer sein!\n");
        }
        
        // E-Mail ist optional, aber wenn angegeben, sollte sie einem gültigen Format entsprechen
        String email = emailField.getText();
        if (email != null && !email.trim().isEmpty() && !isValidEmail(email)) {
            errorMessage.append("Die E-Mail-Adresse hat ein ungültiges Format!\n");
        }
        
        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Fehler anzeigen
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Ungültige Eingabe");
            alert.setHeaderText("Bitte korrigieren Sie die ungültigen Felder");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
    }
    
    /**
     * Überprüft, ob eine E-Mail-Adresse ein gültiges Format hat
     */
    private boolean isValidEmail(String email) {
        // Einfache E-Mail-Validierung
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    /**
     * Bricht die Bearbeitung ab und schließt den Dialog
     */
    private void handleCancel() {
        closeDialog();
    }
    
    /**
     * Schließt den Dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Zeigt einen Fehlerdialog an
     */
    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}