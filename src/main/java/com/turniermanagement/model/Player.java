package com.turniermanagement.model;

public class Player {
    private Long id;
    private String name;
    private int ranking;
    private int gamesWon;
    private int gamesLost;

    public Player() {}

    public Player(String name) {
        this.name = name;
        this.ranking = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
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
}