package com.turniermanagement.db;

import com.turniermanagement.model.Player;
import com.turniermanagement.model.Tournament;
import com.turniermanagement.model.TournamentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlayerDAOTest extends BaseDAOTest {
    private PlayerDAO playerDAO;
    private TournamentDAO tournamentDAO;

    @BeforeEach
    void setUp() throws SQLException {
        super.setUp();
        playerDAO = daoFactory.createPlayerDAO();
        tournamentDAO = daoFactory.createTournamentDAO();
    }

    @Test
    void testSavePlayer() throws SQLException {
        Player player = new Player("Test Player");
        player.setGamesWon(5);
        player.setGamesLost(2);

        playerDAO.save(player);
        assertNotNull(player.getId(), "Player ID should be set after save");

        Optional<Player> savedPlayer = playerDAO.findById(player.getId());
        assertTrue(savedPlayer.isPresent(), "Saved player should be found");
        assertEquals("Test Player", savedPlayer.get().getName());
        assertEquals(5, savedPlayer.get().getGamesWon());
        assertEquals(2, savedPlayer.get().getGamesLost());
    }

    @Test
    void testSavePlayerWithEmail() throws SQLException {
        Player player = new Player("Test Player", "test@example.com");
        player.setGamesWon(3);
        player.setGamesLost(1);

        playerDAO.save(player);
        assertNotNull(player.getId(), "Player ID should be set after save");

        Optional<Player> savedPlayer = playerDAO.findById(player.getId());
        assertTrue(savedPlayer.isPresent(), "Saved player should be found");
        assertEquals("Test Player", savedPlayer.get().getName());
        assertEquals("test@example.com", savedPlayer.get().getEmail());
        assertEquals(3, savedPlayer.get().getGamesWon());
        assertEquals(1, savedPlayer.get().getGamesLost());
    }

    @Test
    void testUpdatePlayer() throws SQLException {
        Player player = new Player("Original Name");
        playerDAO.save(player);
        Long playerId = player.getId();

        player.setName("Updated Name");
        playerDAO.update(player);

        Optional<Player> updatedPlayer = playerDAO.findById(playerId);
        assertTrue(updatedPlayer.isPresent(), "Updated player should be found");
        assertEquals("Updated Name", updatedPlayer.get().getName());
    }

    @Test
    void testUpdatePlayerEmail() throws SQLException {
        Player player = new Player("Email Test Player", "initial@example.com");
        playerDAO.save(player);
        Long playerId = player.getId();

        player.setEmail("updated@example.com");
        playerDAO.update(player);

        Optional<Player> updatedPlayer = playerDAO.findById(playerId);
        assertTrue(updatedPlayer.isPresent(), "Updated player should be found");
        assertEquals("updated@example.com", updatedPlayer.get().getEmail());
    }

    @Test
    void testFindAllPlayers() throws SQLException {
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        playerDAO.save(player1);
        playerDAO.save(player2);

        List<Player> allPlayers = playerDAO.findAll();
        assertEquals(2, allPlayers.size(), "Should find all saved players");
        assertTrue(allPlayers.stream().anyMatch(p -> p.getName().equals("Player 1")));
        assertTrue(allPlayers.stream().anyMatch(p -> p.getName().equals("Player 2")));
    }

    @Test
    void testDeletePlayer() throws SQLException {
        Player player = new Player("To Delete");
        playerDAO.save(player);
        Long playerId = player.getId();

        playerDAO.delete(playerId);
        Optional<Player> deletedPlayer = playerDAO.findById(playerId);
        assertFalse(deletedPlayer.isPresent(), "Player should be deleted");
    }

    @Test
    void testFindByIdNonExistent() throws SQLException {
        Optional<Player> nonExistentPlayer = playerDAO.findById(999L);
        assertFalse(nonExistentPlayer.isPresent(), "Should not find non-existent player");
    }

    @Test
    void testFindByName() throws SQLException {
        Player player = new Player("Unique Name", "name@example.com");
        playerDAO.save(player);

        Optional<Player> foundPlayer = playerDAO.findByName("Unique Name");
        assertTrue(foundPlayer.isPresent(), "Player should be found by name");
        assertEquals(player.getId(), foundPlayer.get().getId());
        assertEquals("name@example.com", foundPlayer.get().getEmail());
    }

    @Test
    void testFindByEmail() throws SQLException {
        Player player = new Player("Email Search Player", "unique@example.com");
        playerDAO.save(player);

        Optional<Player> foundPlayer = playerDAO.findByEmail("unique@example.com");
        assertTrue(foundPlayer.isPresent(), "Player should be found by email");
        assertEquals(player.getId(), foundPlayer.get().getId());
        assertEquals("Email Search Player", foundPlayer.get().getName());
    }

    @Test
    void testFindByEmailWithNullEmail() throws SQLException {
        Optional<Player> notFoundPlayer = playerDAO.findByEmail(null);
        assertFalse(notFoundPlayer.isPresent(), "Player should not be found with null email");
    }

    @Test
    void testFindByNonExistentEmail() throws SQLException {
        Optional<Player> notFoundPlayer = playerDAO.findByEmail("nonexistent@example.com");
        assertFalse(notFoundPlayer.isPresent(), "Player should not be found with non-existent email");
    }

    @Test
    void testPlayerTournamentRelationship() throws SQLException {
        // Create a player
        Player player = new Player("Test Player");
        playerDAO.save(player);
        assertNotNull(player.getId(), "Player should have an ID after saving");

        // Create two tournaments
        Tournament tournament1 = new Tournament("Tournament 1", LocalDate.now(), LocalDate.now().plusDays(1));
        Tournament tournament2 = new Tournament("Tournament 2", LocalDate.now(), LocalDate.now().plusDays(1));
        tournamentDAO.save(tournament1);
        tournamentDAO.save(tournament2);
        
        // Add tournaments to player
        player.addTournament(tournament1);
        player.addTournament(tournament2);
        playerDAO.update(player);

        // Verify relationships after loading from database
        Optional<Player> loadedPlayer = playerDAO.findById(player.getId());
        assertTrue(loadedPlayer.isPresent());
        assertEquals(2, loadedPlayer.get().getTournaments().size());
        
        // Remove one tournament
        player.removeTournament(tournament1);
        playerDAO.update(player);
        
        loadedPlayer = playerDAO.findById(player.getId());
        assertTrue(loadedPlayer.isPresent());
        assertEquals(1, loadedPlayer.get().getTournaments().size());
    }
    
    @Test
    void testPlayerRankingInTournament() throws SQLException {
        // Create a player
        Player player = new Player("Ranking Test Player");
        playerDAO.save(player);
        
        // Create two tournaments
        Tournament tournament1 = new Tournament("Tournament 1", LocalDate.now(), LocalDate.now().plusDays(1));
        Tournament tournament2 = new Tournament("Tournament 2", LocalDate.now(), LocalDate.now().plusDays(2));
        tournamentDAO.save(tournament1);
        tournamentDAO.save(tournament2);
        
        // Add tournaments to player with different rankings
        player.addTournament(tournament1);
        player.addTournament(tournament2);
        player.setRanking(tournament1, 10);
        player.setRanking(tournament2, 20);
        playerDAO.update(player);
        
        // Verify rankings after loading from database
        Optional<Player> loadedPlayer = playerDAO.findById(player.getId());
        assertTrue(loadedPlayer.isPresent());
        assertEquals(2, loadedPlayer.get().getTournaments().size());
        assertEquals(10, loadedPlayer.get().getRanking(tournament1));
        assertEquals(20, loadedPlayer.get().getRanking(tournament2));
        
        // Test updating a specific ranking
        playerDAO.updatePlayerRanking(player, tournament1, 15);
        
        loadedPlayer = playerDAO.findById(player.getId());
        assertTrue(loadedPlayer.isPresent());
        assertEquals(15, loadedPlayer.get().getRanking(tournament1));
        assertEquals(20, loadedPlayer.get().getRanking(tournament2));
    }
}