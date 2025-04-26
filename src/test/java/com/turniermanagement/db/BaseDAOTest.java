package com.turniermanagement.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAOTest {
    protected Connection connection;
    protected TestDAOFactory daoFactory;

    @BeforeEach
    void setUp() throws SQLException {
        // In-Memory SQLite Datenbank für Tests
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        
        // Erstelle eine TestDAOFactory, die die Test-Connection verwendet
        daoFactory = new TestDAOFactory(connection);
        
        // Aktiviere Foreign Keys
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        
        // Tabellen erstellen
        createTables();
    }
    
    @AfterEach
    void tearDown() throws SQLException {
        // Lösche alle Daten nach dem Test
        clearAllData();
        
        // Schließe die Verbindung
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    private void clearAllData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Deaktiviere Foreign Keys temporär für das Löschen
            stmt.execute("PRAGMA foreign_keys = OFF");
            
            // Lösche Daten aus allen Tabellen
            stmt.execute("DELETE FROM tournament_player");
            stmt.execute("DELETE FROM match");
            stmt.execute("DELETE FROM round");
            stmt.execute("DELETE FROM tournament");
            stmt.execute("DELETE FROM player");
            
            // Aktiviere Foreign Keys wieder
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }
    
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Player Tabelle (ohne Ranking Feld)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT,
                    games_won INTEGER DEFAULT 0,
                    games_lost INTEGER DEFAULT 0
                )
            """);

            // Tournament Tabelle
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tournament (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    start_date TEXT,
                    end_date TEXT,
                    status TEXT
                )
            """);

            // Round Tabelle
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS round (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tournament_id INTEGER,
                    round_number INTEGER,
                    completed BOOLEAN DEFAULT 0,
                    FOREIGN KEY (tournament_id) REFERENCES tournament(id)
                )
            """);

            // Match Tabelle
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS match (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    round_id INTEGER,
                    player1_id INTEGER,
                    player2_id INTEGER,
                    winner_id INTEGER,
                    score_player1 INTEGER DEFAULT 0,
                    score_player2 INTEGER DEFAULT 0,
                    status TEXT,
                    FOREIGN KEY (round_id) REFERENCES round(id),
                    FOREIGN KEY (player1_id) REFERENCES player(id),
                    FOREIGN KEY (player2_id) REFERENCES player(id),
                    FOREIGN KEY (winner_id) REFERENCES player(id)
                )
            """);

            // Tournament_Player Verbindungstabelle mit Ranking als Beziehungsattribut
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tournament_player (
                    tournament_id INTEGER,
                    player_id INTEGER,
                    ranking INTEGER DEFAULT 0,
                    PRIMARY KEY (tournament_id, player_id),
                    FOREIGN KEY (tournament_id) REFERENCES tournament(id),
                    FOREIGN KEY (player_id) REFERENCES player(id)
                )
            """);
        }
    }
}