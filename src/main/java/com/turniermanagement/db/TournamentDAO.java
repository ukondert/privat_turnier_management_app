package com.turniermanagement.db;

import com.turniermanagement.model.Tournament;
import java.sql.SQLException;

/**
 * Interface für den Datenbankzugriff auf Tournament-Objekte.
 */
public interface TournamentDAO extends DAO<Tournament, Long> {
    // Spezifische Methoden für Tournament, falls benötigt
    
    /**
     * Löscht eine Runde und alle zugehörigen Matches aus der Datenbank.
     * 
     * @param roundId Die ID der zu löschenden Runde
     * @throws SQLException Bei Datenbankfehlern
     */
    void deleteRound(Long roundId) throws SQLException;
    
    /**
     * Entfernt alle Spieler aus einem Turnier.
     * 
     * @param tournamentId Die ID des Turniers
     * @throws SQLException Bei Datenbankfehlern
     */
    void removePlayers(Long tournamentId) throws SQLException;
}