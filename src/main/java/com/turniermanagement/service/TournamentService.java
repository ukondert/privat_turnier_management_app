package com.turniermanagement.service;

import com.turniermanagement.db.TournamentDAO;
import com.turniermanagement.db.PlayerDAO;
import com.turniermanagement.model.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class TournamentService {
    private final TournamentDAO tournamentDAO;
    private final PlayerDAO playerDAO;

    public TournamentService() {
        this.tournamentDAO = getTournamentDAO();
        this.playerDAO = getPlayerDAO();
    }

    protected TournamentDAO getTournamentDAO() {
        return new TournamentDAO();
    }

    protected PlayerDAO getPlayerDAO() {
        return new PlayerDAO();
    }

    public Tournament createTournament(String name, LocalDate startDate, LocalDate endDate, List<Player> players) throws SQLException {
        Tournament tournament = new Tournament(name, startDate, endDate);
        tournament.setStatus(TournamentStatus.CREATED);
        
        for (Player player : players) {
            if (player.getId() == null) {
                playerDAO.save(player);
            }
            tournament.addPlayer(player);
        }

        tournamentDAO.save(tournament);
        return tournament;
    }

    public void startTournament(Long tournamentId) throws SQLException {
        Tournament tournament = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.CREATED) {
            throw new IllegalStateException("Tournament must be in CREATED state to start");
        }

        if (tournament.getPlayers().size() < 2) {
            throw new IllegalStateException("Tournament needs at least 2 players to start");
        }

        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournamentDAO.update(tournament);
    }

    public void completeTournament(Long tournamentId) throws SQLException {
        Tournament tournament = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Tournament must be in progress to complete");
        }

        // Prüfe ob alle Runden abgeschlossen sind
        if (tournament.getRounds().stream().anyMatch(round -> !round.isCompleted())) {
            throw new IllegalStateException("All rounds must be completed before completing tournament");
        }

        tournament.setStatus(TournamentStatus.COMPLETED);
        tournamentDAO.update(tournament);

        // Aktualisiere Spielerstatistiken
        updatePlayerStatistics(tournament);
    }

    public void cancelTournament(Long tournamentId) throws SQLException {
        Tournament tournament = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        tournament.setStatus(TournamentStatus.CANCELLED);
        tournamentDAO.update(tournament);
    }

    private void updatePlayerStatistics(Tournament tournament) throws SQLException {
        Map<Long, PlayerStats> playerStats = new HashMap<>();

        // Initialisiere Statistiken für alle Spieler
        for (Player player : tournament.getPlayers()) {
            playerStats.put(player.getId(), new PlayerStats());
        }

        // Sammle Statistiken aus allen Matches
        for (Round round : tournament.getRounds()) {
            for (Match match : round.getMatches()) {
                if (match.getStatus() == MatchStatus.COMPLETED) {
                    PlayerStats stats1 = playerStats.get(match.getPlayer1().getId());
                    PlayerStats stats2 = playerStats.get(match.getPlayer2().getId());

                    if (match.getWinner() != null) {
                        if (match.getWinner().getId().equals(match.getPlayer1().getId())) {
                            stats1.gamesWon++;
                            stats2.gamesLost++;
                        } else {
                            stats1.gamesLost++;
                            stats2.gamesWon++;
                        }
                    }
                }
            }
        }

        // Aktualisiere Spieler in der Datenbank
        for (Player player : tournament.getPlayers()) {
            PlayerStats stats = playerStats.get(player.getId());
            player.setGamesWon(player.getGamesWon() + stats.gamesWon);
            player.setGamesLost(player.getGamesLost() + stats.gamesLost);
            playerDAO.update(player);
        }
    }

    private static class PlayerStats {
        int gamesWon = 0;
        int gamesLost = 0;
    }
}