package com.turniermanagement.db;

import com.turniermanagement.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TournamentDAOTest extends BaseDAOTest {
    private TournamentDAO tournamentDAO;
    private PlayerDAO playerDAO;

    @BeforeEach
    void setUp() throws SQLException {
        super.setUp();
        tournamentDAO = new TournamentDAO() {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
        playerDAO = new PlayerDAO() {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
    }

    @Test
    void testSaveTournament() throws SQLException {
        Tournament tournament = new Tournament(
            "Test Tournament",
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        );
        tournament.setStatus(TournamentStatus.CREATED);

        tournamentDAO.save(tournament);
        assertNotNull(tournament.getId(), "Tournament ID should be set after save");

        Optional<Tournament> savedTournament = tournamentDAO.findById(tournament.getId());
        assertTrue(savedTournament.isPresent(), "Saved tournament should be found");
        assertEquals("Test Tournament", savedTournament.get().getName());
        assertEquals(TournamentStatus.CREATED, savedTournament.get().getStatus());
    }

    @Test
    void testSaveTournamentWithPlayers() throws SQLException {
        Tournament tournament = new Tournament(
            "Tournament with Players",
            LocalDate.now(),
            LocalDate.now().plusDays(2)
        );

        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        playerDAO.save(player1);
        playerDAO.save(player2);

        tournament.addPlayer(player1);
        tournament.addPlayer(player2);

        tournamentDAO.save(tournament);

        Optional<Tournament> savedTournament = tournamentDAO.findById(tournament.getId());
        assertTrue(savedTournament.isPresent(), "Saved tournament should be found");
        assertEquals(2, savedTournament.get().getPlayers().size(), "Tournament should have 2 players");
    }

    @Test
    void testUpdateTournament() throws SQLException {
        Tournament tournament = new Tournament(
            "Original Name",
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        );
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
    void testUpdateTournamentPlayers() throws SQLException {
        Tournament tournament = new Tournament(
            "Tournament to Update Players",
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        );
        Player player1 = new Player("Player 1");
        playerDAO.save(player1);
        tournament.addPlayer(player1);
        tournamentDAO.save(tournament);

        Player player2 = new Player("Player 2");
        playerDAO.save(player2);
        tournament.addPlayer(player2);
        tournamentDAO.update(tournament);

        Optional<Tournament> updatedTournament = tournamentDAO.findById(tournament.getId());
        assertTrue(updatedTournament.isPresent(), "Updated tournament should be found");
        assertEquals(2, updatedTournament.get().getPlayers().size(), "Tournament should have 2 players");
    }

    @Test
    void testFindAllTournaments() throws SQLException {
        Tournament tournament1 = new Tournament("Tournament 1", LocalDate.now(), LocalDate.now().plusDays(1));
        Tournament tournament2 = new Tournament("Tournament 2", LocalDate.now(), LocalDate.now().plusDays(1));
        tournamentDAO.save(tournament1);
        tournamentDAO.save(tournament2);

        List<Tournament> allTournaments = tournamentDAO.findAll();
        assertEquals(2, allTournaments.size(), "Should find all saved tournaments");
        assertTrue(allTournaments.stream().anyMatch(t -> t.getName().equals("Tournament 1")));
        assertTrue(allTournaments.stream().anyMatch(t -> t.getName().equals("Tournament 2")));
    }

    @Test
    void testDeleteTournament() throws SQLException {
        Tournament tournament = new Tournament("To Delete", LocalDate.now(), LocalDate.now().plusDays(1));
        tournamentDAO.save(tournament);
        Long tournamentId = tournament.getId();

        tournamentDAO.delete(tournamentId);
        Optional<Tournament> deletedTournament = tournamentDAO.findById(tournamentId);
        assertFalse(deletedTournament.isPresent(), "Tournament should be deleted");
    }

    @Test
    void testDeleteTournamentWithPlayers() throws SQLException {
        Tournament tournament = new Tournament("To Delete with Players", LocalDate.now(), LocalDate.now().plusDays(1));
        Player player = new Player("Test Player");
        playerDAO.save(player);
        tournament.addPlayer(player);
        tournamentDAO.save(tournament);
        Long tournamentId = tournament.getId();

        tournamentDAO.delete(tournamentId);
        Optional<Tournament> deletedTournament = tournamentDAO.findById(tournamentId);
        assertFalse(deletedTournament.isPresent(), "Tournament should be deleted");
        
        // Player should still exist
        Optional<Player> existingPlayer = playerDAO.findById(player.getId());
        assertTrue(existingPlayer.isPresent(), "Player should still exist after tournament deletion");
    }
}