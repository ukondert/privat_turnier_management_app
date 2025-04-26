package com.turniermanagement.db;

import com.turniermanagement.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoundDAOTest extends BaseDAOTest {
    private RoundDAO roundDAO;
    private PlayerDAO playerDAO;
    private MatchDAO matchDAO;
    private Long tournamentId;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() throws SQLException {
        super.setUp();
        roundDAO = daoFactory.createRoundDAO();
        playerDAO = daoFactory.createPlayerDAO();
        matchDAO = daoFactory.createMatchDAO();

        // Testdaten vorbereiten
        player1 = new Player("Player 1");
        player2 = new Player("Player 2");
        playerDAO.save(player1);
        playerDAO.save(player2);
        
        // Ein Testturnier erstellen
        tournamentId = createTestTournament();
    }

    private Long createTestTournament() throws SQLException {
        try (var stmt = connection.prepareStatement(
                "INSERT INTO tournament (name, start_date, end_date, status) VALUES ('Test Tournament', '2024-01-01', '2024-01-02', 'CREATED')",
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    @Test
    void testSaveRound() throws SQLException {
        Round round = new Round();
        round.setRoundNumber(1);
        
        Match match = new Match(player1, player2);
        match.setStatus(MatchStatus.SCHEDULED);
        round.addMatch(match);

        roundDAO.save(round, tournamentId);
        assertNotNull(round.getId(), "Round ID should be set after save");

        Optional<Round> savedRound = roundDAO.findById(round.getId());
        assertTrue(savedRound.isPresent(), "Saved round should be found");
        assertEquals(1, savedRound.get().getRoundNumber());
        assertEquals(1, savedRound.get().getMatches().size(), "Round should have one match");
    }

    @Test
    void testUpdateRound() throws SQLException {
        Round round = new Round();
        round.setRoundNumber(1);
        roundDAO.save(round, tournamentId);

        round.setCompleted(true);
        Match match = new Match(player1, player2);
        match.setStatus(MatchStatus.COMPLETED);
        match.setResult(3, 1);
        match.setWinner(player1);
        
        // Wichtig: Match erst speichern
        matchDAO.save(match, round.getId());
        round.addMatch(match);
        
        roundDAO.update(round);

        Optional<Round> updatedRound = roundDAO.findById(round.getId());
        assertTrue(updatedRound.isPresent(), "Updated round should be found");
        assertTrue(updatedRound.get().isCompleted());
        assertEquals(1, updatedRound.get().getMatches().size(), "Round should have one match");
        
        Match updatedMatch = updatedRound.get().getMatches().get(0);
        assertEquals(MatchStatus.COMPLETED, updatedMatch.getStatus());
        assertEquals(3, updatedMatch.getScorePlayer1());
        assertEquals(1, updatedMatch.getScorePlayer2());
        assertEquals(player1.getId(), updatedMatch.getWinner().getId());
    }

    @Test
    void testFindByTournamentId() throws SQLException {
        Round round1 = new Round();
        round1.setRoundNumber(1);
        Round round2 = new Round();
        round2.setRoundNumber(2);

        roundDAO.save(round1, tournamentId);
        roundDAO.save(round2, tournamentId);

        List<Round> tournamentRounds = roundDAO.findByTournamentId(tournamentId);
        assertEquals(2, tournamentRounds.size(), "Should find all rounds in tournament");
        assertEquals(1, tournamentRounds.get(0).getRoundNumber(), "Rounds should be ordered by round number");
        assertEquals(2, tournamentRounds.get(1).getRoundNumber());
    }

    @Test
    void testDeleteRound() throws SQLException {
        Round round = new Round();
        round.setRoundNumber(1);
        Match match = new Match(player1, player2);
        round.addMatch(match);
        roundDAO.save(round, tournamentId);
        Long roundId = round.getId();

        roundDAO.delete(roundId);
        Optional<Round> deletedRound = roundDAO.findById(roundId);
        assertFalse(deletedRound.isPresent(), "Round should be deleted");
    }

    @Test
    void testFindByIdNonExistent() throws SQLException {
        Optional<Round> nonExistentRound = roundDAO.findById(999L);
        assertFalse(nonExistentRound.isPresent(), "Should not find non-existent round");
    }
}