package com.turniermanagement.service;

import com.turniermanagement.db.DAOFactory;
import com.turniermanagement.db.PlayerDAO;
import com.turniermanagement.model.Player;
import com.turniermanagement.model.Tournament;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlayerServiceTest {
    @Mock
    private PlayerDAO playerDAO;
    
    @Mock
    private DAOFactory mockDAOFactory;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDAOFactory.createPlayerDAO()).thenReturn(playerDAO);
        playerService = new PlayerService(mockDAOFactory);
    }

    @Test
    void testCreatePlayer() throws SQLException {
        // Setup
        String playerName = "Test Player";
        String playerEmail = "test@example.com";
        Player player = new Player(playerName, playerEmail);
        player.setId(1L);
        
        when(playerDAO.findByName(playerName)).thenReturn(Optional.empty());
        when(playerDAO.findByEmail(playerEmail)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Player p = invocation.getArgument(0);
            p.setId(1L);
            return null;
        }).when(playerDAO).save(any(Player.class));

        // Execute
        Player createdPlayer = playerService.createPlayer(playerName, playerEmail);

        // Verify
        assertNotNull(createdPlayer);
        assertEquals(1L, createdPlayer.getId());
        assertEquals(playerName, createdPlayer.getName());
        assertEquals(playerEmail, createdPlayer.getEmail());
        
        verify(playerDAO).findByName(playerName);
        verify(playerDAO).findByEmail(playerEmail);
        verify(playerDAO).save(any(Player.class));
    }

    @Test
    void testCreatePlayerWithDuplicateName() throws SQLException {
        // Setup
        String playerName = "Existing Player";
        String playerEmail = "new@example.com";
        Player existingPlayer = new Player(playerName);
        existingPlayer.setId(1L);
        
        when(playerDAO.findByName(playerName)).thenReturn(Optional.of(existingPlayer));

        // Execute & Verify
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            playerService.createPlayer(playerName, playerEmail);
        });
        
        assertEquals("A player with this name already exists", exception.getMessage());
        verify(playerDAO).findByName(playerName);
        verify(playerDAO, never()).save(any(Player.class));
    }

    @Test
    void testCreatePlayerWithDuplicateEmail() throws SQLException {
        // Setup
        String playerName = "New Player";
        String playerEmail = "existing@example.com";
        Player existingPlayer = new Player("Other Player", playerEmail);
        existingPlayer.setId(1L);
        
        when(playerDAO.findByName(playerName)).thenReturn(Optional.empty());
        when(playerDAO.findByEmail(playerEmail)).thenReturn(Optional.of(existingPlayer));

        // Execute & Verify
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            playerService.createPlayer(playerName, playerEmail);
        });
        
        assertEquals("A player with this email already exists", exception.getMessage());
        verify(playerDAO).findByName(playerName);
        verify(playerDAO).findByEmail(playerEmail);
        verify(playerDAO, never()).save(any(Player.class));
    }

    @Test
    void testCreatePlayerWithEmptyName() throws SQLException {
        // Execute & Verify
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.createPlayer("", "email@example.com");
        });
        
        assertEquals("Player name cannot be empty", exception.getMessage());
        verify(playerDAO, never()).save(any(Player.class));
    }

    @Test
    void testCreatePlayerWithNullName() throws SQLException {
        // Execute & Verify
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.createPlayer(null, "email@example.com");
        });
        
        assertEquals("Player name cannot be empty", exception.getMessage());
        verify(playerDAO, never()).save(any(Player.class));
    }

    @Test
    void testCreatePlayerWithOptionalEmail() throws SQLException {
        // Setup
        String playerName = "No Email Player";
        
        when(playerDAO.findByName(playerName)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Player p = invocation.getArgument(0);
            p.setId(1L);
            return null;
        }).when(playerDAO).save(any(Player.class));

        // Execute
        Player createdPlayer = playerService.createPlayer(playerName, null);

        // Verify
        assertNotNull(createdPlayer);
        assertEquals(1L, createdPlayer.getId());
        assertEquals(playerName, createdPlayer.getName());
        assertNull(createdPlayer.getEmail());
        
        verify(playerDAO).findByName(playerName);
        verify(playerDAO, never()).findByEmail(any());
        verify(playerDAO).save(any(Player.class));
    }

    @Test
    void testUpdatePlayer() throws SQLException {
        // Setup
        Long playerId = 1L;
        String updatedName = "Updated Player";
        String updatedEmail = "updated@example.com";
        
        Player existingPlayer = new Player("Original Player", "original@example.com");
        existingPlayer.setId(playerId);
        
        Player updatedPlayer = new Player(updatedName, updatedEmail);
        updatedPlayer.setId(playerId);
        
        when(playerDAO.findByName(updatedName)).thenReturn(Optional.empty());
        when(playerDAO.findByEmail(updatedEmail)).thenReturn(Optional.empty());

        // Execute
        Player result = playerService.updatePlayer(updatedPlayer);

        // Verify
        assertNotNull(result);
        assertEquals(updatedName, result.getName());
        assertEquals(updatedEmail, result.getEmail());
        
        verify(playerDAO).findByName(updatedName);
        verify(playerDAO).findByEmail(updatedEmail);
        verify(playerDAO).update(updatedPlayer);
    }

    @Test
    void testUpdatePlayerOwnNameAndEmail() throws SQLException {
        // Setup
        Long playerId = 1L;
        String playerName = "Same Player";
        String playerEmail = "same@example.com";
        
        Player existingPlayer = new Player(playerName, playerEmail);
        existingPlayer.setId(playerId);
        
        Player updatedPlayer = new Player(playerName, playerEmail);
        updatedPlayer.setId(playerId);
        updatedPlayer.setGamesWon(5); // Nur andere Felder werden geändert
        
        when(playerDAO.findByName(playerName)).thenReturn(Optional.of(existingPlayer));
        when(playerDAO.findByEmail(playerEmail)).thenReturn(Optional.of(existingPlayer));

        // Execute
        Player result = playerService.updatePlayer(updatedPlayer);

        // Verify
        assertNotNull(result);
        assertEquals(playerName, result.getName());
        assertEquals(playerEmail, result.getEmail());
        
        verify(playerDAO).findByName(playerName);
        verify(playerDAO).findByEmail(playerEmail);
        verify(playerDAO).update(updatedPlayer);
    }

    @Test
    void testUpdatePlayerWithNullId() throws SQLException {
        // Setup
        Player player = new Player("No ID Player", "noid@example.com");
        
        // Execute & Verify
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.updatePlayer(player);
        });
        
        assertEquals("Player ID cannot be null for update", exception.getMessage());
        verify(playerDAO, never()).update(any(Player.class));
    }

    @Test
    void testUpdatePlayerWithDuplicateName() throws SQLException {
        // Setup
        Long playerId = 1L;
        String newName = "Existing Player";
        String email = "email@example.com";
        
        Player player = new Player(newName, email);
        player.setId(playerId);
        
        Player existingPlayer = new Player(newName, "other@example.com");
        existingPlayer.setId(2L); // Andere ID
        
        when(playerDAO.findByName(newName)).thenReturn(Optional.of(existingPlayer));
        
        // Execute & Verify
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            playerService.updatePlayer(player);
        });
        
        assertEquals("A player with this name already exists", exception.getMessage());
        verify(playerDAO).findByName(newName);
        verify(playerDAO, never()).update(any(Player.class));
    }

    @Test
    void testDeletePlayer() throws SQLException {
        // Setup
        Long playerId = 1L;

        // Execute
        playerService.deletePlayer(playerId);

        // Verify
        verify(playerDAO).delete(playerId);
    }

    @Test
    void testDeletePlayerWithNullId() throws SQLException {
        // Execute & Verify
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.deletePlayer(null);
        });
        
        assertEquals("Player ID cannot be null", exception.getMessage());
        verify(playerDAO, never()).delete(any());
    }

    @Test
    void testFindPlayerById() throws SQLException {
        // Setup
        Long playerId = 1L;
        Player player = new Player("Test Player");
        player.setId(playerId);
        
        when(playerDAO.findById(playerId)).thenReturn(Optional.of(player));

        // Execute
        Optional<Player> result = playerService.findPlayerById(playerId);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(playerId, result.get().getId());
        assertEquals("Test Player", result.get().getName());
        
        verify(playerDAO).findById(playerId);
    }

    @Test
    void testFindPlayerByName() throws SQLException {
        // Setup
        String playerName = "Player By Name";
        Player player = new Player(playerName);
        player.setId(1L);
        
        when(playerDAO.findByName(playerName)).thenReturn(Optional.of(player));

        // Execute
        Optional<Player> result = playerService.findPlayerByName(playerName);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(playerName, result.get().getName());
        
        verify(playerDAO).findByName(playerName);
    }

    @Test
    void testFindPlayerByEmail() throws SQLException {
        // Setup
        String playerEmail = "byemail@example.com";
        Player player = new Player("Player By Email", playerEmail);
        player.setId(1L);
        
        when(playerDAO.findByEmail(playerEmail)).thenReturn(Optional.of(player));

        // Execute
        Optional<Player> result = playerService.findPlayerByEmail(playerEmail);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(playerEmail, result.get().getEmail());
        
        verify(playerDAO).findByEmail(playerEmail);
    }

    @Test
    void testGetAllPlayers() throws SQLException {
        // Setup
        Player player1 = new Player("Player 1");
        player1.setId(1L);
        Player player2 = new Player("Player 2");
        player2.setId(2L);
        
        List<Player> players = Arrays.asList(player1, player2);
        when(playerDAO.findAll()).thenReturn(players);

        // Execute
        List<Player> result = playerService.getAllPlayers();

        // Verify
        assertEquals(2, result.size());
        assertEquals("Player 1", result.get(0).getName());
        assertEquals("Player 2", result.get(1).getName());
        
        verify(playerDAO).findAll();
    }

    @Test
    void testUpdatePlayerRanking() throws SQLException {
        // Setup
        Player player = new Player("Ranking Player");
        player.setId(1L);
        
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Test Tournament");
        
        int newRanking = 10;

        // Execute
        playerService.updatePlayerRanking(player, tournament, newRanking);

        // Verify
        verify(playerDAO).updatePlayerRanking(player, tournament, newRanking);
    }
}