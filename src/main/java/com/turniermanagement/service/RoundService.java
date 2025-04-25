package com.turniermanagement.service;

import com.turniermanagement.db.RoundDAO;
import com.turniermanagement.model.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RoundService {
    private final RoundDAO roundDAO;
    private final MatchService matchService;

    public RoundService() {
        this.roundDAO = getRoundDAO();
        this.matchService = new MatchService();
    }

    protected RoundDAO getRoundDAO() {
        return new RoundDAO();
    }

    public Round createNextRound(Tournament tournament) throws SQLException {
        int nextRoundNumber = tournament.getRounds().size() + 1;
        Round round = new Round();
        round.setRoundNumber(nextRoundNumber);

        List<Player> players = new ArrayList<>(tournament.getPlayers());
        
        // Sortiere Spieler nach Ranking (Gewonnene Spiele - Verlorene Spiele)
        players.sort((p1, p2) -> {
            int p1Score = p1.getGamesWon() - p1.getGamesLost();
            int p2Score = p2.getGamesWon() - p2.getGamesLost();
            return Integer.compare(p2Score, p1Score); // Absteigend sortieren
        });

        // Generiere Paarungen nach Schweizer System
        List<Match> matches = generateSwissSystemPairings(players, tournament);
        matches.forEach(round::addMatch);

        roundDAO.save(round, tournament.getId());
        return round;
    }

    private List<Match> generateSwissSystemPairings(List<Player> players, Tournament tournament) {
        // Sammle bereits gespielte Paarungen
        Set<String> playedPairings = getPlayedPairings(tournament);
        
        List<Match> matches = new ArrayList<>();
        Set<Player> pairedPlayers = new HashSet<>();

        // Versuche Spieler mit ähnlichem Ranking zu paaren
        for (int i = 0; i < players.size(); i++) {
            if (pairedPlayers.contains(players.get(i))) {
                continue;
            }

            Player player1 = players.get(i);
            Player bestMatch = null;
            int minRankingDiff = Integer.MAX_VALUE;

            // Suche den am besten passenden Gegner
            for (int j = i + 1; j < players.size(); j++) {
                Player player2 = players.get(j);
                if (pairedPlayers.contains(player2)) {
                    continue;
                }

                // Prüfe ob diese Paarung bereits gespielt wurde
                String pairingKey = getPairingKey(player1, player2);
                if (playedPairings.contains(pairingKey)) {
                    continue;
                }

                // Berechne Ranking-Differenz
                int rankingDiff = Math.abs(
                    (player1.getGamesWon() - player1.getGamesLost()) -
                    (player2.getGamesWon() - player2.getGamesLost())
                );

                if (rankingDiff < minRankingDiff) {
                    minRankingDiff = rankingDiff;
                    bestMatch = player2;
                }
            }

            // Wenn ein passender Gegner gefunden wurde
            if (bestMatch != null) {
                Match match = new Match(player1, bestMatch);
                match.setStatus(MatchStatus.SCHEDULED);
                matches.add(match);
                pairedPlayers.add(player1);
                pairedPlayers.add(bestMatch);
            }
        }

        // Behandle übrige Spieler (falls ungerade Anzahl)
        players.stream()
            .filter(p -> !pairedPlayers.contains(p))
            .findFirst()
            .ifPresent(player -> {
                Match byeMatch = new Match(player, null);
                byeMatch.setStatus(MatchStatus.COMPLETED);
                byeMatch.setWinner(player);
                matches.add(byeMatch);
            });

        return matches;
    }

    private Set<String> getPlayedPairings(Tournament tournament) {
        return tournament.getRounds().stream()
            .flatMap(r -> r.getMatches().stream())
            .filter(m -> m.getPlayer1() != null && m.getPlayer2() != null)
            .map(m -> getPairingKey(m.getPlayer1(), m.getPlayer2()))
            .collect(Collectors.toSet());
    }

    private String getPairingKey(Player p1, Player p2) {
        // Erstelle einen eindeutigen Schlüssel für eine Spielerpaarung
        // Sortiere IDs, damit die Reihenfolge der Spieler egal ist
        long id1 = p1.getId();
        long id2 = p2.getId();
        return id1 < id2 ? id1 + "-" + id2 : id2 + "-" + id1;
    }

    public void completeRound(Long roundId) throws SQLException {
        Round round = roundDAO.findById(roundId)
            .orElseThrow(() -> new IllegalArgumentException("Round not found"));

        // Prüfe ob alle Matches abgeschlossen sind
        boolean allMatchesCompleted = round.getMatches().stream()
            .allMatch(m -> m.getStatus() == MatchStatus.COMPLETED);

        if (!allMatchesCompleted) {
            throw new IllegalStateException("All matches must be completed before completing the round");
        }

        round.setCompleted(true);
        roundDAO.update(round);
    }

    public List<Round> getTournamentRounds(Long tournamentId) throws SQLException {
        return roundDAO.findByTournamentId(tournamentId);
    }
}