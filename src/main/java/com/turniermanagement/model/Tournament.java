package com.turniermanagement.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tournament {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Player> players;
    private List<Round> rounds;
    private TournamentStatus status;

    public Tournament() {
        this.players = new ArrayList<>();
        this.rounds = new ArrayList<>();
        this.status = TournamentStatus.CREATED;
    }

    public Tournament(String name, LocalDate startDate, LocalDate endDate) {
        this();
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    public List<Round> getRounds() { return rounds; }
    public void setRounds(List<Round> rounds) { this.rounds = rounds; }
    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void addRound(Round round) {
        rounds.add(round);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tournament that = (Tournament) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate) &&
               status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, startDate, endDate, status);
    }
}