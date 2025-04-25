package com.turniermanagement.db;

import com.turniermanagement.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlayerDAOTest extends BaseDAOTest {
    private PlayerDAO playerDAO;

    @BeforeEach
    void setUp() throws SQLException {
        super.setUp();
        playerDAO = new PlayerDAO() {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
    }

    @Test
    void testSavePlayer() throws SQLException {
        Player player = new Player("Test Player");
        player.setRanking(100);
        player.setGamesWon(5);
        player.setGamesLost(2);

        playerDAO.save(player);
        assertNotNull(player.getId(), "Player ID should be set after save");

        Optional<Player> savedPlayer = playerDAO.findById(player.getId());
        assertTrue(savedPlayer.isPresent(), "Saved player should be found");
        assertEquals("Test Player", savedPlayer.get().getName());
        assertEquals(100, savedPlayer.get().getRanking());
        assertEquals(5, savedPlayer.get().getGamesWon());
        assertEquals(2, savedPlayer.get().getGamesLost());
    }

    @Test
    void testUpdatePlayer() throws SQLException {
        Player player = new Player("Original Name");
        playerDAO.save(player);
        Long playerId = player.getId();

        player.setName("Updated Name");
        player.setRanking(200);
        playerDAO.update(player);

        Optional<Player> updatedPlayer = playerDAO.findById(playerId);
        assertTrue(updatedPlayer.isPresent(), "Updated player should be found");
        assertEquals("Updated Name", updatedPlayer.get().getName());
        assertEquals(200, updatedPlayer.get().getRanking());
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
}