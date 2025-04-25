package com.turniermanagement.service;

import com.turniermanagement.db.RoundDAO;
import com.turniermanagement.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoundServiceTest {
    @Mock
    private RoundDAO roundDAO;

    private RoundService roundService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roundService = new RoundService() {
            @Override
            protected RoundDAO getRoundDAO() {
                return roundDAO;
            }
        };
    }

    @Test
    void testCreateNextRoundFirstRound() throws SQLException {
        Tournament tournament = createTournament();
        Player player1 = createPlayer(1L, "Player 1", 2, 1);
        Player player2 = createPlayer(2L, "Player 2", 1, 2);
        Player player3 = createPlayer(3L, "Player 3", 3, 0);
        Player player4 = createPlayer(4L, "Player 4", 0, 3);
        tournament.getPlayers().addAll(Arrays.asList(player1, player2, player3, player4));

        Round round = roundService.createNextRound(tournament);

        assertEquals(1, round.getRoundNumber());
        assertEquals(2, round.getMatches().size());
        verify(roundDAO).save(round, tournament.getId());
    }

    @Test
    void testCreateNextRoundWithPreviousMatches() throws SQLException {
        Tournament tournament = createTournament();
        Player player1 = createPlayer(1L, "Player 1", 2, 0);
        Player player2 = createPlayer(2L, "Player 2", 1, 1);
        Player player3 = createPlayer(3L, "Player 3", 1, 1);
        Player player4 = createPlayer(4L, "Player 4", 0, 2);
        tournament.getPlayers().addAll(Arrays.asList(player1, player2, player3, player4));

        // Erste Runde mit existierenden Matches
        Round round1 = new Round();
        round1.setRoundNumber(1);
        Match match1 = new Match(player1, player2);
        match1.setStatus(MatchStatus.COMPLETED);
        match1.setWinner(player1);
        Match match2 = new Match(player3, player4);
        match2.setStatus(MatchStatus.COMPLETED);
        match2.setWinner(player3);
        round1.addMatch(match1);
        round1.addMatch(match2);
        tournament.addRound(round1);

        Round round2 = roundService.createNextRound(tournament);

        assertEquals(2, round2.getRoundNumber());
        assertEquals(2, round2.getMatches().size());
        
        // Überprüfe, dass keine bereits gespielten Paarungen erstellt wurden
        for (Match match : round2.getMatches()) {
            assertFalse(
                (match.getPlayer1().equals(player1) && match.getPlayer2().equals(player2)) ||
                (match.getPlayer1().equals(player2) && match.getPlayer2().equals(player1)) ||
                (match.getPlayer1().equals(player3) && match.getPlayer2().equals(player4)) ||
                (match.getPlayer1().equals(player4) && match.getPlayer2().equals(player3))
            );
        }

        verify(roundDAO).save(round2, tournament.getId());
    }

    @Test
    void testCreateNextRoundOddNumberOfPlayers() throws SQLException {
        Tournament tournament = createTournament();
        Player player1 = createPlayer(1L, "Player 1", 2, 0);
        Player player2 = createPlayer(2L, "Player 2", 1, 1);
        Player player3 = createPlayer(3L, "Player 3", 0, 2);
        tournament.getPlayers().addAll(Arrays.asList(player1, player2, player3));

        Round round = roundService.createNextRound(tournament);

        assertEquals(1, round.getRoundNumber());
        assertEquals(2, round.getMatches().size()); // 1 reguläres Match + 1 Freilos
        
        Optional<Match> byeMatch = round.getMatches().stream()
            .filter(m -> m.getPlayer2() == null)
            .findFirst();
        assertTrue(byeMatch.isPresent(), "Ein Freilos-Match sollte existieren");
        assertEquals(MatchStatus.COMPLETED, byeMatch.get().getStatus());

        verify(roundDAO).save(round, tournament.getId());
    }

    @Test
    void testCompleteRound() throws SQLException {
        Round round = new Round();
        round.setId(1L);
        Player player1 = createPlayer(1L, "Player 1", 0, 0);
        Player player2 = createPlayer(2L, "Player 2", 0, 0);
        Match match = new Match(player1, player2);
        match.setStatus(MatchStatus.COMPLETED);
        round.addMatch(match);

        when(roundDAO.findById(1L)).thenReturn(Optional.of(round));

        roundService.completeRound(1L);

        assertTrue(round.isCompleted());
        verify(roundDAO).update(round);
    }

    @Test
    void testCompleteRoundWithIncompleteMatches() throws SQLException {
        Round round = new Round();
        round.setId(1L);
        Player player1 = createPlayer(1L, "Player 1", 0, 0);
        Player player2 = createPlayer(2L, "Player 2", 0, 0);
        Match match = new Match(player1, player2);
        match.setStatus(MatchStatus.IN_PROGRESS);
        round.addMatch(match);

        when(roundDAO.findById(1L)).thenReturn(Optional.of(round));

        assertThrows(IllegalStateException.class, () -> roundService.completeRound(1L));
    }

    @Test
    void testGetTournamentRounds() throws SQLException {
        Long tournamentId = 1L;
        List<Round> expectedRounds = Arrays.asList(new Round(), new Round());
        when(roundDAO.findByTournamentId(tournamentId)).thenReturn(expectedRounds);

        List<Round> actualRounds = roundService.getTournamentRounds(tournamentId);

        assertEquals(expectedRounds, actualRounds);
        verify(roundDAO).findByTournamentId(tournamentId);
    }

    private Tournament createTournament() {
        Tournament tournament = new Tournament("Test Tournament", LocalDate.now(), LocalDate.now().plusDays(1));
        tournament.setId(1L);
        return tournament;
    }

    private Player createPlayer(Long id, String name, int gamesWon, int gamesLost) {
        Player player = new Player(name);
        player.setId(id);
        player.setGamesWon(gamesWon);
        player.setGamesLost(gamesLost);
        return player;
    }
}