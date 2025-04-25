package com.turniermanagement.db;

import com.turniermanagement.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MatchDAOTest extends BaseDAOTest {
    private MatchDAO matchDAO;
    private PlayerDAO playerDAO;
    private Long roundId;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() throws SQLException {
        super.setUp();
        matchDAO = new MatchDAO() {
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

        // Testdaten vorbereiten
        player1 = new Player("Player 1");
        player2 = new Player("Player 2");
        playerDAO.save(player1);
        playerDAO.save(player2);
        
        // Eine Testrunde erstellen
        roundId = createTestRound();
    }

    private Long createTestRound() throws SQLException {
        try (var stmt = connection.prepareStatement(
                "INSERT INTO round (tournament_id, round_number, completed) VALUES (1, 1, 0)",
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
    void testSaveMatch() throws SQLException {
        Match match = createTestMatch();
        
        matchDAO.save(match, roundId);
        assertNotNull(match.getId(), "Match ID should be set after save");

        Optional<Match> savedMatch = matchDAO.findById(match.getId());
        assertTrue(savedMatch.isPresent(), "Saved match should be found");
        assertEquals(player1.getId(), savedMatch.get().getPlayer1().getId());
        assertEquals(player2.getId(), savedMatch.get().getPlayer2().getId());
        assertEquals(MatchStatus.SCHEDULED, savedMatch.get().getStatus());
    }

    @Test
    void testUpdateMatch() throws SQLException {
        Match match = createTestMatch();
        matchDAO.save(match, roundId);

        match.setResult(3, 1);
        match.setStatus(MatchStatus.COMPLETED);
        match.setWinner(player1);
        matchDAO.update(match);

        Optional<Match> updatedMatch = matchDAO.findById(match.getId());
        assertTrue(updatedMatch.isPresent(), "Updated match should be found");
        assertEquals(3, updatedMatch.get().getScorePlayer1());
        assertEquals(1, updatedMatch.get().getScorePlayer2());
        assertEquals(player1.getId(), updatedMatch.get().getWinner().getId());
        assertEquals(MatchStatus.COMPLETED, updatedMatch.get().getStatus());
    }

    private Match createTestMatch() {
        Match match = new Match(player1, player2);
        match.setStatus(MatchStatus.SCHEDULED);
        return match;
    }

    @Test
    void testFindByRoundId() throws SQLException {
        Match match1 = new Match(player1, player2);
        Match match2 = new Match(player2, player1);
        matchDAO.save(match1, roundId);
        matchDAO.save(match2, roundId);

        List<Match> roundMatches = matchDAO.findByRoundId(roundId);
        assertEquals(2, roundMatches.size(), "Should find all matches in round");
    }

    @Test
    void testDeleteMatch() throws SQLException {
        Match match = new Match(player1, player2);
        matchDAO.save(match, roundId);
        Long matchId = match.getId();

        matchDAO.delete(matchId);
        Optional<Match> deletedMatch = matchDAO.findById(matchId);
        assertFalse(deletedMatch.isPresent(), "Match should be deleted");
    }

    @Test
    void testFindByIdNonExistent() throws SQLException {
        Optional<Match> nonExistentMatch = matchDAO.findById(999L);
        assertFalse(nonExistentMatch.isPresent(), "Should not find non-existent match");
    }
}