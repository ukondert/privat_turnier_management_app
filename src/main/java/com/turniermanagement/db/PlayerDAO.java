package com.turniermanagement.db;

import com.turniermanagement.model.Player;
import com.turniermanagement.model.Tournament;
import java.sql.SQLException;

/**
 * Interface für den Datenbankzugriff auf Player-Objekte.
 */
public interface PlayerDAO extends DAO<Player, Long> {
    
    /**
     * Aktualisiert das Ranking eines Spielers in einem bestimmten Turnier.
     * @param player Der Spieler
     * @param tournament Das Turnier
     * @param ranking Das neue Ranking
     * @throws SQLException Bei Datenbankfehlern
     */
    void updatePlayerRanking(Player player, Tournament tournament, int ranking) throws SQLException;
}