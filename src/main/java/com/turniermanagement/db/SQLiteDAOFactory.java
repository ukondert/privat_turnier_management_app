package com.turniermanagement.db;

import java.sql.Connection;

/**
 * SQLite-Implementierung der DAOFactory.
 * Erstellt SQLite-spezifische Implementierungen der DAOs.
 */
public class SQLiteDAOFactory extends DAOFactory {
    
    @Override
    public PlayerDAO createPlayerDAO() {
        return new SQLitePlayerDAO();
    }
    
    @Override
    public TournamentDAO createTournamentDAO() {
        return new SQLiteTournamentDAO();
    }
    
    @Override
    public RoundDAO createRoundDAO() {
        return new SQLiteRoundDAO();
    }
    
    @Override
    public MatchDAO createMatchDAO() {
        return new SQLiteMatchDAO();
    }
}