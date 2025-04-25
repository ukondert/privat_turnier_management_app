package com.turniermanagement.service;

import com.turniermanagement.db.MatchDAO;
import com.turniermanagement.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchServiceTest {
    @Mock
    private MatchDAO matchDAO;

    private MatchService matchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        matchService = new MatchService() {
            @Override
            protected MatchDAO getMatchDAO() {
                return matchDAO;
            }
        };
    }

    @Test
    void testGenerateMatchesEvenPlayers() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");
        Player player4 = createPlayer(4L, "Player 4");

        List<Match> matches = matchService.generateMatches(Arrays.asList(player1, player2, player3, player4));

        assertEquals(2, matches.size(), "Should create 2 matches for 4 players");
        matches.forEach(match -> {
            assertNotNull(match.getPlayer1());
            assertNotNull(match.getPlayer2());
            assertEquals(MatchStatus.SCHEDULED, match.getStatus());
        });
    }

    @Test
    void testGenerateMatchesOddPlayers() {
        Player player1 = createPlayer(1L, "Player 1");
        Player player2 = createPlayer(2L, "Player 2");
        Player player3 = createPlayer(3L, "Player 3");

        List<Match> matches = matchService.generateMatches(Arrays.asList(player1, player2, player3));

        assertEquals(2, matches.size(), "Should create 2 matches (1 regular + 1 bye) for 3 players");
        
        // Überprüfe das reguläre Match
        Match regularMatch = matches.stream()
            .filter(m -> m.getPlayer2() != null)
            .findFirst()
            .orElseThrow();
        assertEquals(MatchStatus.SCHEDULED, regularMatch.getStatus());

        // Überprüfe das Freilos-Match
        Match byeMatch = matches.stream()
            .filter(m -> m.getPlayer2() == null)
            .findFirst()
            .orElseThrow();
        assertEquals(MatchStatus.COMPLETED, byeMatch.getStatus());
        assertNotNull(byeMatch.getWinner());
    }

    @Test
    void testUpdateMatchResult() throws SQLException {
        Match match = new Match(
            createPlayer(1L, "Player 1"),
            createPlayer(2L, "Player 2")
        );
        match.setId(1L);
        match.setStatus(MatchStatus.IN_PROGRESS);

        when(matchDAO.findById(1L)).thenReturn(Optional.of(match));

        matchService.updateMatchResult(1L, 3, 1);

        assertEquals(MatchStatus.COMPLETED, match.getStatus());
        assertEquals(3, match.getScorePlayer1());
        assertEquals(1, match.getScorePlayer2());
        assertEquals(match.getPlayer1(), match.getWinner());
        verify(matchDAO).update(match);
    }

    @Test
    void testUpdateMatchResultDraw() throws SQLException {
        Match match = new Match(
            createPlayer(1L, "Player 1"),
            createPlayer(2L, "Player 2")
        );
        match.setId(1L);
        match.setStatus(MatchStatus.IN_PROGRESS);

        when(matchDAO.findById(1L)).thenReturn(Optional.of(match));

        matchService.updateMatchResult(1L, 2, 2);

        assertEquals(MatchStatus.COMPLETED, match.getStatus());
        assertEquals(2, match.getScorePlayer1());
        assertEquals(2, match.getScorePlayer2());
        assertNull(match.getWinner());
        verify(matchDAO).update(match);
    }

    @Test
    void testStartMatch() throws SQLException {
        Match match = new Match(
            createPlayer(1L, "Player 1"),
            createPlayer(2L, "Player 2")
        );
        match.setId(1L);
        match.setStatus(MatchStatus.SCHEDULED);

        when(matchDAO.findById(1L)).thenReturn(Optional.of(match));

        matchService.startMatch(1L);

        assertEquals(MatchStatus.IN_PROGRESS, match.getStatus());
        verify(matchDAO).update(match);
    }

    @Test
    void testCancelMatch() throws SQLException {
        Match match = new Match(
            createPlayer(1L, "Player 1"),
            createPlayer(2L, "Player 2")
        );
        match.setId(1L);
        match.setStatus(MatchStatus.SCHEDULED);

        when(matchDAO.findById(1L)).thenReturn(Optional.of(match));

        matchService.cancelMatch(1L);

        assertEquals(MatchStatus.CANCELLED, match.getStatus());
        verify(matchDAO).update(match);
    }

    @Test
    void testCancelCompletedMatch() throws SQLException {
        Match match = new Match(
            createPlayer(1L, "Player 1"),
            createPlayer(2L, "Player 2")
        );
        match.setId(1L);
        match.setStatus(MatchStatus.COMPLETED);

        when(matchDAO.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalStateException.class, () -> matchService.cancelMatch(1L));
    }

    private Player createPlayer(Long id, String name) {
        Player player = new Player(name);
        player.setId(id);
        return player;
    }
}