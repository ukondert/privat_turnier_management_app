package com.turniermanagement.model;

import java.util.Objects;

public class Match {
    private Long id;
    private Player player1;
    private Player player2;
    private Player winner;
    private int scorePlayer1;
    private int scorePlayer2;
    private MatchStatus status;

    public Match() {
        this.status = MatchStatus.SCHEDULED;
    }

    public Match(Player player1, Player player2) {
        this();
        this.player1 = player1;
        this.player2 = player2;
    }

    public void setResult(int scorePlayer1, int scorePlayer2) {
        this.scorePlayer1 = scorePlayer1;
        this.scorePlayer2 = scorePlayer2;
        if (scorePlayer1 > scorePlayer2) {
            this.winner = player1;
        } else if (scorePlayer2 > scorePlayer1) {
            this.winner = player2;
        }
        this.status = MatchStatus.COMPLETED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return scorePlayer1 == match.scorePlayer1 &&
               scorePlayer2 == match.scorePlayer2 &&
               Objects.equals(id, match.id) &&
               Objects.equals(player1, match.player1) &&
               Objects.equals(player2, match.player2) &&
               Objects.equals(winner, match.winner) &&
               status == match.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, player1, player2, winner, scorePlayer1, scorePlayer2, status);
    }

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Player getPlayer1() { return player1; }
    public void setPlayer1(Player player1) { this.player1 = player1; }
    public Player getPlayer2() { return player2; }
    public void setPlayer2(Player player2) { this.player2 = player2; }
    public Player getWinner() { return winner; }
    public void setWinner(Player winner) { this.winner = winner; }
    public int getScorePlayer1() { return scorePlayer1; }
    public void setScorePlayer1(int scorePlayer1) { this.scorePlayer1 = scorePlayer1; }
    public int getScorePlayer2() { return scorePlayer2; }
    public void setScorePlayer2(int scorePlayer2) { this.scorePlayer2 = scorePlayer2; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
}