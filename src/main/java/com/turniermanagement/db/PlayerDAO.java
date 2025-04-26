package com.turniermanagement.db;

import com.turniermanagement.model.Player;
import com.turniermanagement.model.Tournament;
import java.sql.SQLException;
import java.util.Optional;

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
    
    /**
     * Sucht einen Spieler anhand seines Namens.
     * @param name Der Name des Spielers
     * @return Optional mit dem gefundenen Spieler oder leer, wenn kein Spieler mit diesem Namen existiert
     * @throws SQLException Bei Datenbankfehlern
     */
    Optional<Player> findByName(String name) throws SQLException;
    
    /**
     * Sucht einen Spieler anhand seiner E-Mail-Adresse.
     * @param email Die E-Mail-Adresse des Spielers
     * @return Optional mit dem gefundenen Spieler oder leer, wenn kein Spieler mit dieser E-Mail existiert
     * @throws SQLException Bei Datenbankfehlern
     */
    Optional<Player> findByEmail(String email) throws SQLException;
}