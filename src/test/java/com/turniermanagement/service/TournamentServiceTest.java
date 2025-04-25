package com.turniermanagement.service;

import com.turniermanagement.db.PlayerDAO;
import com.turniermanagement.db.TournamentDAO;
import com.turniermanagement.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TournamentServiceTest {
    @Mock
    private TournamentDAO tournamentDAO;
    @Mock
    private PlayerDAO playerDAO;

    private TournamentService tournamentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tournamentService = new TournamentService() {
            @Override
            protected TournamentDAO getTournamentDAO() {
                return tournamentDAO;
            }
            @Override
            protected PlayerDAO getPlayerDAO() {
                return playerDAO;
            }
        };
    }

    @Test
    void testCreateTournament() throws SQLException {
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);

        Tournament tournament = tournamentService.createTournament(
            "Test Tournament",
            startDate,
            endDate,
            Arrays.asList(player1, player2)
        );

        assertNotNull(tournament);
        assertEquals("Test Tournament", tournament.getName());
        assertEquals(TournamentStatus.CREATED, tournament.getStatus());
        assertEquals(2, tournament.getPlayers().size());
        verify(tournamentDAO).save(tournament);
    }

    @Test
    void testStartTournament() throws SQLException {
        Tournament tournament = new Tournament("Test", LocalDate.now(), LocalDate.now().plusDays(1));
        tournament.setId(1L);
        tournament.setStatus(TournamentStatus.CREATED);
        tournament.addPlayer(new Player("Player 1"));
        tournament.addPlayer(new Player("Player 2"));

        when(tournamentDAO.findById(1L)).thenReturn(Optional.of(tournament));

        tournamentService.startTournament(1L);

        assertEquals(TournamentStatus.IN_PROGRESS, tournament.getStatus());
        verify(tournamentDAO).update(tournament);
    }

    @Test
    void testStartTournamentWithInvalidStatus() throws SQLException {
        Tournament tournament = new Tournament("Test", LocalDate.now(), LocalDate.now().plusDays(1));
        tournament.setId(1L);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);

        when(tournamentDAO.findById(1L)).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class, () -> tournamentService.startTournament(1L));
    }

    @Test
    void testCompleteTournament() throws SQLException {
        Tournament tournament = new Tournament("Test", LocalDate.now(), LocalDate.now().plusDays(1));
        tournament.setId(1L);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);

        Round round = new Round();
        round.setCompleted(true);
        tournament.addRound(round);

        when(tournamentDAO.findById(1L)).thenReturn(Optional.of(tournament));

        tournamentService.completeTournament(1L);

        assertEquals(TournamentStatus.COMPLETED, tournament.getStatus());
        verify(tournamentDAO).update(tournament);
    }

    @Test
    void testCompleteTournamentWithIncompleteRounds() throws SQLException {
        Tournament tournament = new Tournament("Test", LocalDate.now(), LocalDate.now().plusDays(1));
        tournament.setId(1L);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);

        Round round = new Round();
        round.setCompleted(false);
        tournament.addRound(round);

        when(tournamentDAO.findById(1L)).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class, () -> tournamentService.completeTournament(1L));
    }

    @Test
    void testCancelTournament() throws SQLException {
        Tournament tournament = new Tournament("Test", LocalDate.now(), LocalDate.now().plusDays(1));
        tournament.setId(1L);

        when(tournamentDAO.findById(1L)).thenReturn(Optional.of(tournament));

        tournamentService.cancelTournament(1L);

        assertEquals(TournamentStatus.CANCELLED, tournament.getStatus());
        verify(tournamentDAO).update(tournament);
    }
}