package com.turniermanagement.db;

import java.sql.Connection;

/**
 * Eine Test-Version der DAOFactory, die für Tests verwendet werden kann.
 * Diese Factory-Implementierung erlaubt das Übergeben einer Testverbindung.
 */
public class TestDAOFactory extends DAOFactory {
    
    private Connection testConnection;
    
    /**
     * Erstellt eine TestDAOFactory mit einer Testverbindung.
     * @param testConnection Die Datenbankverbindung für die Tests
     */
    public TestDAOFactory(Connection testConnection) {
        this.testConnection = testConnection;
    }
    
    /**
     * Gibt die Testverbindung zurück.
     * @return Die Testverbindung
     */
    public Connection getTestConnection() {
        return testConnection;
    }
    
    @Override
    public PlayerDAO createPlayerDAO() {
        return new SQLitePlayerDAO(testConnection);
    }
    
    @Override
    public TournamentDAO createTournamentDAO() {
        return new SQLiteTournamentDAO(testConnection);
    }
    
    @Override
    public RoundDAO createRoundDAO() {
        return new SQLiteRoundDAO(testConnection);
    }
    
    @Override
    public MatchDAO createMatchDAO() {
        return new SQLiteMatchDAO(testConnection);
    }
}