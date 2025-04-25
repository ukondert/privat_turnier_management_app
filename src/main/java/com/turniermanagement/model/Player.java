package com.turniermanagement.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private Long id;
    private String name;
    private int ranking;
    private int gamesWon;
    private int gamesLost;
    private List<Tournament> tournaments;

    public Player() {
        this.tournaments = new ArrayList<>();
        this.ranking = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
    }

    public Player(String name) {
        this();
        this.name = name;
    }

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getRanking() { return ranking; }
    public void setRanking(int ranking) { this.ranking = ranking; }
    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }
    public int getGamesLost() { return gamesLost; }
    public void setGamesLost(int gamesLost) { this.gamesLost = gamesLost; }
    public List<Tournament> getTournaments() { return tournaments; }
    public void setTournaments(List<Tournament> tournaments) { this.tournaments = tournaments; }

    public void addTournament(Tournament tournament) {
        if (!tournaments.contains(tournament)) {
            tournaments.add(tournament);
            tournament.addPlayer(this);
        }
    }

    public void removeTournament(Tournament tournament) {
        if (tournaments.remove(tournament)) {
            tournament.removePlayer(this);
        }
    }
}