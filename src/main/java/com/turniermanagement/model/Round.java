package com.turniermanagement.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Round {
    private Long id;
    private int roundNumber;
    private List<Match> matches;
    private boolean completed;

    public Round() {
        this.matches = new ArrayList<>();
        this.completed = false;
    }

    public Round(int roundNumber) {
        this();
        this.roundNumber = roundNumber;
    }

    public void addMatch(Match match) {
        matches.add(match);
    }

    public boolean isRoundComplete() {
        return matches.stream().allMatch(match -> match.getStatus() == MatchStatus.COMPLETED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Round round = (Round) o;
        return roundNumber == round.roundNumber &&
               completed == round.completed &&
               Objects.equals(id, round.id) &&
               Objects.equals(matches, round.matches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roundNumber, matches, completed);
    }

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public List<Match> getMatches() { return matches; }
    public void setMatches(List<Match> matches) { this.matches = matches; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}