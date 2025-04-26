package com.turniermanagement.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Player {
    private Long id;
    private String name;
    private String email;
    private Map<Tournament, Integer> tournamentRankings;
    private int gamesWon;
    private int gamesLost;
    private List<Tournament> tournaments;

    public Player() {
        this.tournaments = new ArrayList<>();
        this.tournamentRankings = new HashMap<>();
        this.gamesWon = 0;
        this.gamesLost = 0;
    }

    public Player(String name) {
        this();
        this.name = name;
    }
    
    public Player(String name, String email) {
        this();
        this.name = name;
        this.email = email;
    }

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    // Neue Methoden für tournamentRankings
    public Map<Tournament, Integer> getTournamentRankings() { return tournamentRankings; }
    public void setTournamentRankings(Map<Tournament, Integer> tournamentRankings) { this.tournamentRankings = tournamentRankings; }
    
    public Integer getRanking(Tournament tournament) {
        return tournamentRankings.getOrDefault(tournament, 0);
    }
    
    public void setRanking(Tournament tournament, int ranking) {
        tournamentRankings.put(tournament, ranking);
    }
    
    // Alte Ranking-Methoden für Abwärtskompatibilität 
    @Deprecated
    public int getRanking() { 
        // Gibt das höchste Ranking zurück, falls vorhanden
        return tournamentRankings.values().stream().max(Integer::compare).orElse(0); 
    }
    
    @Deprecated
    public void setRanking(int ranking) { 
        // Setzt das Ranking für alle Turniere (nicht empfohlen)
        tournaments.forEach(tournament -> tournamentRankings.put(tournament, ranking));
    }
    
    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }
    public int getGamesLost() { return gamesLost; }
    public void setGamesLost(int gamesLost) { this.gamesLost = gamesLost; }
    public List<Tournament> getTournaments() { return tournaments; }
    public void setTournaments(List<Tournament> tournaments) { this.tournaments = tournaments; }

    public void addTournament(Tournament tournament) {
        if (!tournaments.contains(tournament)) {
            tournaments.add(tournament);
            // Standardmäßig Ranking auf 0 setzen
            tournamentRankings.put(tournament, 0);
        }
    }

    public void removeTournament(Tournament tournament) {
        if (tournaments.remove(tournament)) {
            // Ranking entfernen
            tournamentRankings.remove(tournament);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return gamesWon == player.gamesWon &&
               gamesLost == player.gamesLost &&
               Objects.equals(id, player.id) &&
               Objects.equals(name, player.name) &&
               Objects.equals(email, player.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, gamesWon, gamesLost);
    }
}