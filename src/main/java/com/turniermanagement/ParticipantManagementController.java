package com.turniermanagement;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class ParticipantManagementController {

    @FXML
    private TableView<Participant> participantsTable;
    
    @FXML
    private TableColumn<Participant, String> nameColumn;
    
    @FXML
    private TableColumn<Participant, String> gamesColumn;
    
    @FXML
    private TableColumn<Participant, String> tournamentsColumn;
    
    @FXML
    private TableColumn<Participant, String> rankingsColumn;
    
    @FXML
    private TableColumn<Participant, Void> actionsColumn;
    
    @FXML
    private Button addParticipantButton;
    
    @FXML
    private Label totalParticipantsLabel;
    
    @FXML
    private Label totalTournamentsLabel;
    
    @FXML
    private TextField searchField;
    
    private ObservableList<Participant> participantsList = FXCollections.observableArrayList();
    
    @FXML
    private void initialize() {
        // Konfiguriere die Spalten der TableView
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        gamesColumn.setCellValueFactory(new PropertyValueFactory<>("games"));
        tournamentsColumn.setCellValueFactory(new PropertyValueFactory<>("tournaments"));
        rankingsColumn.setCellValueFactory(new PropertyValueFactory<>("rankings"));
        
        // Spalte für Aktionen mit Bearbeiten-Button
        setupActionsColumn();
        
        // Beispieldaten hinzufügen
        loadParticipants();
        
        // Teilnehmer-Button-Klick-Handler
        addParticipantButton.setOnAction(event -> openParticipantEditDialog(null));
        
        // Suche einrichten
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterParticipants(newValue);
        });
        
        // Statistik aktualisieren
        updateStatistics();
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<Participant, Void>, TableCell<Participant, Void>> cellFactory = 
            new Callback<TableColumn<Participant, Void>, TableCell<Participant, Void>>() {
            @Override
            public TableCell<Participant, Void> call(final TableColumn<Participant, Void> param) {
                final TableCell<Participant, Void> cell = new TableCell<Participant, Void>() {
                    private final Button editButton = new Button("Bearbeiten");
                    private final Button deleteButton = new Button("Löschen");
                    
                    {
                        editButton.getStyleClass().add("btn-small");
                        deleteButton.getStyleClass().add("btn-small");
                        
                        editButton.setOnAction(event -> {
                            Participant participant = getTableView().getItems().get(getIndex());
                            openParticipantEditDialog(participant);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            Participant participant = getTableView().getItems().get(getIndex());
                            handleDeleteParticipant(participant);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // HBox für die Buttons
                            javafx.scene.layout.HBox buttonsBox = new javafx.scene.layout.HBox(5);
                            buttonsBox.getChildren().addAll(editButton, deleteButton);
                            setGraphic(buttonsBox);
                        }
                    }
                };
                return cell;
            }
        };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    private void openParticipantEditDialog(Participant participant) {
        try {
            // Dialog-Fenster erstellen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("participant-edit-dialog.fxml"));
            Parent root = loader.load();
            
            // Controller holen und Teilnehmer setzen
            ParticipantEditDialogController controller = loader.getController();
            
            // setParticipant immer aufrufen, auch mit null für einen neuen Teilnehmer
            controller.setParticipant(participant);
            
            // Callback für Speichern-Aktion setzen
            controller.setOnSaveHandler(savedParticipant -> {
                if (participant == null) {
                    // Neuer Teilnehmer
                    participantsList.add(savedParticipant);
                } else {
                    // Vorhandenen Teilnehmer aktualisieren 
                    int index = participantsList.indexOf(participant);
                    if (index >= 0) {
                        participantsList.set(index, savedParticipant);
                    }
                }
                
                participantsTable.refresh();
                updateStatistics();
            });
            
            // Dialog einrichten und anzeigen
            Stage dialogStage = new Stage();
            dialogStage.setTitle(participant == null ? "Neuen Teilnehmer hinzufügen" : "Teilnehmer bearbeiten");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(participantsTable.getScene().getWindow());
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Fehler beim Öffnen des Dialogs", e.getMessage());
        }
    }
    
    private void handleDeleteParticipant(Participant participant) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Teilnehmer löschen");
        confirmDialog.setHeaderText("Teilnehmer löschen");
        confirmDialog.setContentText("Sind Sie sicher, dass Sie den Teilnehmer " + 
                                    participant.getName() + " löschen möchten?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                participantsList.remove(participant);
                updateStatistics();
            }
        });
    }
    
    private void loadParticipants() {
        // Beispieldaten - später durch Datenbankabfrage ersetzen
        participantsList.add(new Participant("Max Mustermann", "10/5", "3", "1., 2., 5."));
        participantsList.add(new Participant("Erika Musterfrau", "15/2", "4", "1., 1., 2., 3."));
        participantsList.add(new Participant("John Doe", "8/8", "2", "4., 6."));
        
        participantsTable.setItems(participantsList);
    }
    
    private void filterParticipants(String searchText) {
        ObservableList<Participant> filteredList = FXCollections.observableArrayList();
        
        if (searchText == null || searchText.isEmpty()) {
            filteredList.addAll(participantsList);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            
            for (Participant participant : participantsList) {
                if (participant.getName().toLowerCase().contains(lowerCaseFilter)) {
                    filteredList.add(participant);
                }
            }
        }
        
        participantsTable.setItems(filteredList);
    }
    
    private void updateStatistics() {
        totalParticipantsLabel.setText(String.valueOf(participantsList.size()));
        // Weitere Statistikberechnungen hier
        totalTournamentsLabel.setText("5"); // Später durch echte Daten ersetzen
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Teilnehmer-Klasse
    public static class Participant {
        private final SimpleStringProperty name;
        private final SimpleStringProperty games;
        private final SimpleStringProperty tournaments;
        private final SimpleStringProperty rankings;
        
        public Participant(String name, String games, String tournaments, String rankings) {
            this.name = new SimpleStringProperty(name);
            this.games = new SimpleStringProperty(games);
            this.tournaments = new SimpleStringProperty(tournaments);
            this.rankings = new SimpleStringProperty(rankings);
        }
        
        public String getName() {
            return name.get();
        }
        
        public void setName(String name) {
            this.name.set(name);
        }
        
        public SimpleStringProperty nameProperty() {
            return name;
        }
        
        public String getGames() {
            return games.get();
        }
        
        public void setGames(String games) {
            this.games.set(games);
        }
        
        public SimpleStringProperty gamesProperty() {
            return games;
        }
        
        public String getTournaments() {
            return tournaments.get();
        }
        
        public void setTournaments(String tournaments) {
            this.tournaments.set(tournaments);
        }
        
        public SimpleStringProperty tournamentsProperty() {
            return tournaments;
        }
        
        public String getRankings() {
            return rankings.get();
        }
        
        public void setRankings(String rankings) {
            this.rankings.set(rankings);
        }
        
        public SimpleStringProperty rankingsProperty() {
            return rankings;
        }
    }
}