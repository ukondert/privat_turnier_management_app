package com.turniermanagement.db;

import org.junit.jupiter.api.BeforeEach;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAOTest {
    protected Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // In-Memory SQLite Datenbank für Tests
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Player Tabelle
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    ranking INTEGER DEFAULT 0,
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

            // Tournament_Player Verbindungstabelle
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tournament_player (
                    tournament_id INTEGER,
                    player_id INTEGER,
                    PRIMARY KEY (tournament_id, player_id),
                    FOREIGN KEY (tournament_id) REFERENCES tournament(id),
                    FOREIGN KEY (player_id) REFERENCES player(id)
                )
            """);
        }
    }
}