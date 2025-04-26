package com.turniermanagement.db;

import com.turniermanagement.model.Match;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface für den Datenbankzugriff auf Match-Objekte.
 */
public interface MatchDAO extends DAO<Match, Long> {
    
    /**
     * Speichert ein neues Match mit Zuordnung zu einer Runde.
     * @param match Das zu speichernde Match
     * @param roundId Die ID der zugehörigen Runde
     * @throws SQLException Bei Datenbankfehlern
     */
    void save(Match match, Long roundId) throws SQLException;
    
    /**
     * Findet alle Matches einer bestimmten Runde.
     * @param roundId Die ID der Runde
     * @return Liste der Matches der Runde
     * @throws SQLException Bei Datenbankfehlern
     */
    List<Match> findByRoundId(Long roundId) throws SQLException;
}