package com.turniermanagement.db;

import com.turniermanagement.model.Tournament;
import com.turniermanagement.model.Player;
import com.turniermanagement.model.TournamentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TournamentDAOTest extends BaseDAOTest {
    private TournamentDAO tournamentDAO;
    private PlayerDAO playerDAO;

    @BeforeEach
    void setUp() throws SQLException {
        super.setUp();
        tournamentDAO = daoFactory.createTournamentDAO();
        playerDAO = daoFactory.createPlayerDAO();
    }

    @Test
    void testSaveTournament() throws SQLException {
        Tournament tournament = new Tournament("Test Tournament", LocalDate.now(), LocalDate.now().plusDays(1));
        tournamentDAO.save(tournament);
        assertNotNull(tournament.getId(), "Tournament ID should be set after save");

        Optional<Tournament> savedTournament = tournamentDAO.findById(tournament.getId());
        assertTrue(savedTournament.isPresent(), "Saved tournament should be found");
        assertEquals("Test Tournament", savedTournament.get().getName());
    }

    @Test
    void testUpdateTournament() throws SQLException {
        Tournament tournament = new Tournament("Original Name", LocalDate.now(), LocalDate.now().plusDays(1));
        tournamentDAO.save(tournament);
        Long tournamentId = tournament.getId();

        tournament.setName("Updated Name");
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournamentDAO.update(tournament);

        Optional<Tournament> updatedTournament = tournamentDAO.findById(tournamentId);
        assertTrue(updatedTournament.isPresent(), "Updated tournament should be found");
        assertEquals("Updated Name", updatedTournament.get().getName());
        assertEquals(TournamentStatus.IN_PROGRESS, updatedTournament.get().getStatus());
    }

    @Test
    void testTournamentPlayerRelationship() throws SQLException {
        // Create a tournament
        Tournament tournament = new Tournament("Test Tournament", LocalDate.now(), LocalDate.now().plusDays(1));
        tournamentDAO.save(tournament);
        assertNotNull(tournament.getId(), "Tournament should have an ID after saving");

        // Create two players
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        playerDAO.save(player1);
        playerDAO.save(player2);
        
        // Add players to tournament
        tournament.addPlayer(player1);
        tournament.addPlayer(player2);
        tournamentDAO.update(tournament);

        // Verify relationships after loading from database
        Optional<Tournament> loadedTournament = tournamentDAO.findById(tournament.getId());
        assertTrue(loadedTournament.isPresent());
        assertEquals(2, loadedTournament.get().getPlayers().size());
        
        // Verify bidirectional relationship
        Optional<Player> loadedPlayer1 = playerDAO.findById(player1.getId());
        assertTrue(loadedPlayer1.isPresent());
        assertEquals(1, loadedPlayer1.get().getTournaments().size());
        assertEquals(tournament.getId(), loadedPlayer1.get().getTournaments().get(0).getId());
        
        // Remove one player
        tournament.removePlayer(player1);
        tournamentDAO.update(tournament);
        
        loadedTournament = tournamentDAO.findById(tournament.getId());
        assertTrue(loadedTournament.isPresent());
        assertEquals(1, loadedTournament.get().getPlayers().size());
        
        // Verify player's tournaments are updated
        loadedPlayer1 = playerDAO.findById(player1.getId());
        assertTrue(loadedPlayer1.isPresent());
        assertTrue(loadedPlayer1.get().getTournaments().isEmpty());
    }

    @Test
    void testDeleteTournament() throws SQLException {
        Tournament tournament = new Tournament("To Delete", LocalDate.now(), LocalDate.now().plusDays(1));
        tournamentDAO.save(tournament);
        Long tournamentId = tournament.getId();

        // Add a player to test relationship cleanup
        Player player = new Player("Test Player");
        playerDAO.save(player);
        tournament.addPlayer(player);
        tournamentDAO.update(tournament);

        // Delete tournament
        tournamentDAO.delete(tournamentId);
        
        // Verify tournament is deleted
        Optional<Tournament> deletedTournament = tournamentDAO.findById(tournamentId);
        assertFalse(deletedTournament.isPresent(), "Tournament should be deleted");
        
        // Verify player still exists but without tournament reference
        Optional<Player> existingPlayer = playerDAO.findById(player.getId());
        assertTrue(existingPlayer.isPresent(), "Player should still exist");
        assertTrue(existingPlayer.get().getTournaments().isEmpty(), "Player should have no tournament references");
    }
}