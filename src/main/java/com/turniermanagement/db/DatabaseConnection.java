package com.turniermanagement.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:tournament.db";
    private static DatabaseConnection instance;
    private Connection connection;
    private static String dbUrl = null;

    private DatabaseConnection() {
        this(DB_URL);
    }

    private DatabaseConnection(String dburl) {
        try {
            dbUrl = dburl;
            connection = DriverManager.getConnection(dburl);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getInstance(String dburl) {
        if (instance == null) {
            instance = new DatabaseConnection(dburl);
        }
        return instance.getConnection();
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    public static boolean isSQLite(Connection connection) throws SQLException {
        return dbUrl != null ? dbUrl.contains("sqlite") : connection.getMetaData().getDatabaseProductName().toLowerCase().contains("sqlite");  
    }

    public static void setDateParameter(PreparedStatement pstmt, int paramIndex, LocalDate date, Connection connection) 
        throws SQLException {
        if (date == null) {
            pstmt.setNull(paramIndex, Types.VARCHAR);
            return;
        }
        
        if (isSQLite(connection)) {
            // SQLite: Als String im ISO-Format
            pstmt.setString(paramIndex, date.toString());
        } else {
            // Andere Datenbanken: Als java.sql.Date
            pstmt.setDate(paramIndex, java.sql.Date.valueOf(date));
        }
    }

    public static LocalDate getDateParameter(ResultSet rs, int paramIndex, Connection connection) 
        throws SQLException {
        if (isSQLite(connection)) {
            String dateString = rs.getString(paramIndex);
            return dateString != null ? LocalDate.parse(dateString) : null;
        } else {
            java.sql.Date sqlDate = rs.getDate(paramIndex);
            return sqlDate != null ? sqlDate.toLocalDate() : null;
        }
    }

    public static LocalDate getDateParameter(ResultSet rs, String paramName, Connection connection) 
        throws SQLException {
        if (isSQLite(connection)) {
            String dateString = rs.getString(paramName);
            if (dateString == null) return null;
            
            try {
                return LocalDate.parse(dateString);
            } catch (Exception e) {
                // Try parsing as timestamp
                try {
                    long timestamp = Long.parseLong(dateString);
                    return new Date(timestamp).toLocalDate();
                } catch (Exception e2) {
                    throw new SQLException("Unable to parse date: " + dateString, e2);
                }
            }
        } else {
            java.sql.Date sqlDate = rs.getDate(paramName);
            return sqlDate != null ? sqlDate.toLocalDate() : null;
        }
    }
    public static LocalDate getLocalDate(ResultSet rs, String columnName) throws SQLException {
        return getDateParameter(rs, columnName, rs.getStatement().getConnection());
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Player Tabelle (ohne Ranking-Feld in der Haupttabelle)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    games_won INTEGER DEFAULT 0,
                    games_lost INTEGER DEFAULT 0
                )
            """);

            // Prüfe, ob das alte Ranking-Feld existiert und migriere die Daten wenn nötig
            try {
                ResultSet rs = connection.getMetaData().getColumns(null, null, "player", "ranking");
                if (rs.next()) {
                    // Das Ranking-Feld existiert noch, wir müssen es migrieren
                    migrateRankingData();
                    
                    // Entferne das Ranking-Feld aus der Player-Tabelle
                    try {
                        stmt.execute("ALTER TABLE player DROP COLUMN ranking");
                    } catch (SQLException e) {
                        // SQLite unterstützt DROP COLUMN nicht direkt; wir müssten die Tabelle neu erstellen
                        // In diesem Fall ignorieren wir das alte Feld einfach
                        System.out.println("Konnte das Ranking-Feld nicht entfernen. Es wird ignoriert.");
                    }
                }
            } catch (SQLException e) {
                // Ignoriere Fehler bei der Metadatenabfrage
            }

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
            
            // Prüfe, ob das Ranking-Feld bereits in tournament_player existiert
            try {
                ResultSet rs = connection.getMetaData().getColumns(null, null, "tournament_player", "ranking");
                if (!rs.next()) {
                    // Das Ranking-Feld existiert noch nicht, wir fügen es hinzu
                    try {
                        stmt.execute("ALTER TABLE tournament_player ADD COLUMN ranking INTEGER DEFAULT 0");
                    } catch (SQLException e) {
                        System.out.println("Fehler beim Hinzufügen des Ranking-Felds: " + e.getMessage());
                    }
                }
            } catch (SQLException e) {
                // Ignoriere Fehler bei der Metadatenabfrage
            }
        }
    }
    
    private void migrateRankingData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT p.id, p.ranking, t.id as tournament_id FROM player p JOIN tournament_player tp ON p.id = tp.player_id JOIN tournament t ON tp.tournament_id = t.id");
            
            while (rs.next()) {
                long playerId = rs.getLong(1);
                int ranking = rs.getInt(2);
                long tournamentId = rs.getLong(3);
                
                // Aktualisiere die Beziehungstabelle mit dem Ranking-Wert
                try (PreparedStatement pstmt = connection.prepareStatement(
                        "UPDATE tournament_player SET ranking = ? WHERE player_id = ? AND tournament_id = ?")) {
                    pstmt.setInt(1, ranking);
                    pstmt.setLong(2, playerId);
                    pstmt.setLong(3, tournamentId);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Fehler bei der Migration der Ranking-Daten: " + e.getMessage());
        }
    }
}