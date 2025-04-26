package com.turniermanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ParticipantEditDialogController {

    @FXML
    private Label dialogTitleLabel;
    
    @FXML
    private TextField nameField;
    
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
    private Consumer<ParticipantManagementController.Participant> onSaveHandler;
    
    @FXML
    private void initialize() {
        // UI-Komponenten initialisieren
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        
        // Felder standardmäßig deaktivieren (außer Name)
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
     * Setzt den zu bearbeitenden Teilnehmer und füllt die Felder aus
     */
    public void setParticipant(ParticipantManagementController.Participant participant) {
        this.participant = participant;
        
        if (participant != null) {
            // Bestehenden Teilnehmer bearbeiten
            dialogTitleLabel.setText("Teilnehmer bearbeiten");
            
            // Felder mit den Daten des Teilnehmers füllen
            nameField.setText(participant.getName());
            gamesField.setText(participant.getGames());
            tournamentsField.setText(participant.getTournaments());
            rankingsField.setText(participant.getRankings());
        } else {
            // Neuen Teilnehmer hinzufügen
            dialogTitleLabel.setText("Teilnehmer hinzufügen");
            
            // Leere Felder für neuen Teilnehmer
            nameField.setText("");
            gamesField.setText("");
            tournamentsField.setText("");
            rankingsField.setText("");
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
        
        // Neuen Teilnehmer erstellen oder bestehenden aktualisieren
        ParticipantManagementController.Participant savedParticipant;
        
        if (participant != null) {
            // Bestehenden Teilnehmer aktualisieren
            participant.setName(nameField.getText());
            
            // Nur Felder aktualisieren, die zur Bearbeitung freigegeben wurden
            if (gamesEditCheckbox.isSelected()) {
                participant.setGames(gamesField.getText());
            }
            
            if (tournamentsEditCheckbox.isSelected()) {
                participant.setTournaments(tournamentsField.getText());
            }
            
            if (rankingsEditCheckbox.isSelected()) {
                participant.setRankings(rankingsField.getText());
            }
            
            savedParticipant = participant;
        } else {
            // Neuen Teilnehmer erstellen
            savedParticipant = new ParticipantManagementController.Participant(
                nameField.getText(),
                gamesField.getText(),
                tournamentsField.getText(),
                rankingsField.getText()
            );
        }
        
        // Callback aufrufen
        if (onSaveHandler != null) {
            onSaveHandler.accept(savedParticipant);
        }
        
        // Dialog schließen
        closeDialog();
    }
    
    /**
     * Validiert die Eingabefelder
     */
    private boolean validateInput() {
        String errorMessage = "";
        
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "Der Name darf nicht leer sein!\n";
        }
        
        // Weitere Validierungen bei Bedarf hinzufügen
        
        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Fehler anzeigen
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Ungültige Eingabe");
            alert.setHeaderText("Bitte korrigieren Sie die ungültigen Felder");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
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
}