package com.turniermanagement.service;

import com.turniermanagement.db.PlayerDAO;
import com.turniermanagement.db.DAOFactory;
import com.turniermanagement.model.Player;
import com.turniermanagement.model.Tournament;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service-Klasse für Player-bezogene Geschäftslogik.
 * Nutzt das DAO-Pattern für die Datenpersistenz.
 */
public class PlayerService {
    private final PlayerDAO playerDAO;

    /**
     * Erstellt einen neuen PlayerService mit Standard-DAOs.
     */
    public PlayerService() {
        this(DAOFactory.getInstance());
    }
    
    /**
     * Erstellt einen neuen PlayerService mit der angegebenen DAOFactory.
     * @param daoFactory Die DAOFactory zum Erstellen von DAOs
     */
    public PlayerService(DAOFactory daoFactory) {
        this.playerDAO = daoFactory.createPlayerDAO();
    }

    /**
     * Erstellt einen neuen Spieler.
     * @param name Name des Spielers
     * @param email E-Mail-Adresse des Spielers (optional)
     * @return Der erstellte Spieler
     * @throws SQLException Bei Datenbankfehlern
     * @throws IllegalArgumentException Wenn der Name leer ist
     * @throws IllegalStateException Wenn bereits ein Spieler mit diesem Namen existiert
     */
    public Player createPlayer(String name, String email) throws SQLException {
        // Validierung
        validatePlayerData(name, email, null);
        
        Player player = new Player(name, email);
        playerDAO.save(player);
        return player;
    }

    /**
     * Aktualisiert einen existierenden Spieler.
     * @param player Der zu aktualisierende Spieler
     * @return Der aktualisierte Spieler
     * @throws SQLException Bei Datenbankfehlern
     * @throws IllegalArgumentException Wenn der Name leer ist oder die ID fehlt
     * @throws IllegalStateException Wenn bereits ein anderer Spieler mit diesem Namen existiert
     */
    public Player updatePlayer(Player player) throws SQLException {
        // Validierung
        if (player.getId() == null) {
            throw new IllegalArgumentException("Player ID cannot be null for update");
        }
        
        validatePlayerData(player.getName(), player.getEmail(), player.getId());
        
        playerDAO.update(player);
        return player;
    }

    /**
     * Löscht einen Spieler.
     * @param playerId ID des zu löschenden Spielers
     * @throws SQLException Bei Datenbankfehlern
     * @throws IllegalArgumentException Wenn die ID null ist
     */
    public void deletePlayer(Long playerId) throws SQLException {
        if (playerId == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }
        
        playerDAO.delete(playerId);
    }

    /**
     * Findet einen Spieler anhand seiner ID.
     * @param playerId ID des Spielers
     * @return Optional mit dem Spieler, falls gefunden
     * @throws SQLException Bei Datenbankfehlern
     */
    public Optional<Player> findPlayerById(Long playerId) throws SQLException {
        return playerDAO.findById(playerId);
    }

    /**
     * Findet einen Spieler anhand seines Namens.
     * @param name Name des Spielers
     * @return Optional mit dem Spieler, falls gefunden
     * @throws SQLException Bei Datenbankfehlern
     */
    public Optional<Player> findPlayerByName(String name) throws SQLException {
        return playerDAO.findByName(name);
    }

    /**
     * Findet einen Spieler anhand seiner E-Mail-Adresse.
     * @param email E-Mail-Adresse des Spielers
     * @return Optional mit dem Spieler, falls gefunden
     * @throws SQLException Bei Datenbankfehlern
     */
    public Optional<Player> findPlayerByEmail(String email) throws SQLException {
        return playerDAO.findByEmail(email);
    }

    /**
     * Gibt alle Spieler zurück.
     * @return Liste aller Spieler
     * @throws SQLException Bei Datenbankfehlern
     */
    public List<Player> getAllPlayers() throws SQLException {
        return playerDAO.findAll();
    }

    /**
     * Aktualisiert das Ranking eines Spielers in einem Turnier.
     * @param player Der Spieler
     * @param tournament Das Turnier
     * @param ranking Das neue Ranking
     * @throws SQLException Bei Datenbankfehlern
     */
    public void updatePlayerRanking(Player player, Tournament tournament, int ranking) throws SQLException {
        playerDAO.updatePlayerRanking(player, tournament, ranking);
    }

    /**
     * Validiert Spielerdaten vor dem Speichern oder Aktualisieren.
     * @param name Name des Spielers
     * @param email E-Mail-Adresse des Spielers (optional)
     * @param playerId ID des Spielers bei Updates (null bei neuen Spielern)
     * @throws SQLException Bei Datenbankfehlern
     * @throws IllegalArgumentException Wenn der Name leer ist
     * @throws IllegalStateException Wenn bereits ein Spieler mit diesem Namen existiert
     */
    private void validatePlayerData(String name, String email, Long playerId) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }
        
        // Überprüfe, ob der Name bereits existiert (aber ignoriere den aktuellen Spieler bei Updates)
        Optional<Player> existingPlayerByName = playerDAO.findByName(name);
        if (existingPlayerByName.isPresent() && 
            (playerId == null || !existingPlayerByName.get().getId().equals(playerId))) {
            throw new IllegalStateException("A player with this name already exists");
        }
        
        // Überprüfe, ob die E-Mail bereits existiert (aber nur, wenn sie angegeben wurde)
        if (email != null && !email.trim().isEmpty()) {
            Optional<Player> existingPlayerByEmail = playerDAO.findByEmail(email);
            if (existingPlayerByEmail.isPresent() && 
                (playerId == null || !existingPlayerByEmail.get().getId().equals(playerId))) {
                throw new IllegalStateException("A player with this email already exists");
            }
        }
    }
}