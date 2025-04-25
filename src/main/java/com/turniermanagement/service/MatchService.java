package com.turniermanagement.service;

import com.turniermanagement.db.MatchDAO;
import com.turniermanagement.model.Match;
import com.turniermanagement.model.Player;
import com.turniermanagement.model.MatchStatus;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MatchService {
    private final MatchDAO matchDAO;

    public MatchService() {
        this.matchDAO = getMatchDAO();
    }

    protected MatchDAO getMatchDAO() {
        return new MatchDAO();
    }

    public List<Match> generateMatches(List<Player> players) {
        List<Match> matches = new ArrayList<>();
        List<Player> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        for (int i = 0; i < shuffledPlayers.size() - 1; i += 2) {
            Player player1 = shuffledPlayers.get(i);
            Player player2 = shuffledPlayers.get(i + 1);
            
            Match match = new Match(player1, player2);
            match.setStatus(MatchStatus.SCHEDULED);
            matches.add(match);
        }

        // Wenn ungerade Anzahl an Spielern, bekommt der letzte ein Freilos
        if (shuffledPlayers.size() % 2 != 0 && !shuffledPlayers.isEmpty()) {
            Player lastPlayer = shuffledPlayers.get(shuffledPlayers.size() - 1);
            Match byeMatch = new Match(lastPlayer, null);
            byeMatch.setStatus(MatchStatus.COMPLETED);
            byeMatch.setWinner(lastPlayer);
            matches.add(byeMatch);
        }

        return matches;
    }

    public void updateMatchResult(Long matchId, int scorePlayer1, int scorePlayer2) throws SQLException {
        Match match = matchDAO.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalStateException("Match is already completed");
        }

        match.setResult(scorePlayer1, scorePlayer2);
        match.setStatus(MatchStatus.COMPLETED);

        // Bestimme den Gewinner
        if (scorePlayer1 > scorePlayer2) {
            match.setWinner(match.getPlayer1());
        } else if (scorePlayer2 > scorePlayer1) {
            match.setWinner(match.getPlayer2());
        }

        matchDAO.update(match);
    }

    public void startMatch(Long matchId) throws SQLException {
        Match match = matchDAO.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Match must be in SCHEDULED state to start");
        }

        match.setStatus(MatchStatus.IN_PROGRESS);
        matchDAO.update(match);
    }

    public void cancelMatch(Long matchId) throws SQLException {
        Match match = matchDAO.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed match");
        }

        match.setStatus(MatchStatus.CANCELLED);
        matchDAO.update(match);
    }
}