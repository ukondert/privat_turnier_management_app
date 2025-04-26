package com.turniermanagement.db;

import com.turniermanagement.model.Round;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface für den Datenbankzugriff auf Round-Objekte.
 */
public interface RoundDAO extends DAO<Round, Long> {
    
    /**
     * Speichert eine neue Runde mit Zuordnung zu einem Turnier.
     * @param round Die zu speichernde Runde
     * @param tournamentId Die ID des zugehörigen Turniers
     * @throws SQLException Bei Datenbankfehlern
     */
    void save(Round round, Long tournamentId) throws SQLException;
    
    /**
     * Findet alle Runden eines bestimmten Turniers.
     * @param tournamentId Die ID des Turniers
     * @return Liste der Runden des Turniers
     * @throws SQLException Bei Datenbankfehlern
     */
    List<Round> findByTournamentId(Long tournamentId) throws SQLException;
}