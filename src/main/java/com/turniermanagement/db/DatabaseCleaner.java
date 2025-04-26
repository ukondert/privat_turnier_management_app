package com.turniermanagement.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Hilfsklasse zum Löschen von Datenbankeinträgen.
 * Ermöglicht das Löschen einzelner Einträge sowie aller Daten aus bestimmten Tabellen.
 */
public class DatabaseCleaner {
    
    private final Connection connection;
    
    /**
     * Erstellt eine neue DatabaseCleaner-Instanz mit der Standarddatenbankverbindung.
     */
    public DatabaseCleaner() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Erstellt eine neue DatabaseCleaner-Instanz mit einer benutzerdefinierten Datenbankverbindung.
     *
     * @param connection Die zu verwendende Datenbankverbindung
     */
    public DatabaseCleaner(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Löscht einen Spieler anhand seiner ID.
     *
     * @param playerId Die ID des zu löschenden Spielers
     * @return true wenn der Löschvorgang erfolgreich war, sonst false
     * @throws SQLException Bei Datenbankfehlern
     */
    public boolean deletePlayer(long playerId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM player WHERE id = ?")) {
            pstmt.setLong(1, playerId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Löscht ein Turnier anhand seiner ID.
     * Beachten Sie, dass dies auch alle zugehörigen Runden, Matches und Turnier-Spieler-Beziehungen löscht.
     *
     * @param tournamentId Die ID des zu löschenden Turniers
     * @return true wenn der Löschvorgang erfolgreich war, sonst false
     * @throws SQLException Bei Datenbankfehlern
     */
    public boolean deleteTournament(long tournamentId) throws SQLException {
        // Transaktion beginnen
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        
        try {
            // Löschen aller Matches in allen Runden des Turniers
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "DELETE FROM match WHERE round_id IN (SELECT id FROM round WHERE tournament_id = ?)")) {
                pstmt.setLong(1, tournamentId);
                pstmt.executeUpdate();
            }
            
            // Löschen aller Runden des Turniers
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM round WHERE tournament_id = ?")) {
                pstmt.setLong(1, tournamentId);
                pstmt.executeUpdate();
            }
            
            // Löschen aller Turnier-Spieler-Beziehungen
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM tournament_player WHERE tournament_id = ?")) {
                pstmt.setLong(1, tournamentId);
                pstmt.executeUpdate();
            }
            
            // Löschen des Turniers selbst
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM tournament WHERE id = ?")) {
                pstmt.setLong(1, tournamentId);
                int result = pstmt.executeUpdate();
                
                // Commit der Transaktion
                connection.commit();
                return result > 0;
            }
        } catch (SQLException e) {
            // Rollback bei Fehler
            connection.rollback();
            throw e;
        } finally {
            // Ursprünglichen AutoCommit-Status wiederherstellen
            connection.setAutoCommit(autoCommit);
        }
    }
    
    /**
     * Löscht eine Runde anhand ihrer ID.
     * Beachten Sie, dass dies auch alle zugehörigen Matches löscht.
     *
     * @param roundId Die ID der zu löschenden Runde
     * @return true wenn der Löschvorgang erfolgreich war, sonst false
     * @throws SQLException Bei Datenbankfehlern
     */
    public boolean deleteRound(long roundId) throws SQLException {
        // Transaktion beginnen
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        
        try {
            // Löschen aller Matches in der Runde
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM match WHERE round_id = ?")) {
                pstmt.setLong(1, roundId);
                pstmt.executeUpdate();
            }
            
            // Löschen der Runde selbst
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM round WHERE id = ?")) {
                pstmt.setLong(1, roundId);
                int result = pstmt.executeUpdate();
                
                // Commit der Transaktion
                connection.commit();
                return result > 0;
            }
        } catch (SQLException e) {
            // Rollback bei Fehler
            connection.rollback();
            throw e;
        } finally {
            // Ursprünglichen AutoCommit-Status wiederherstellen
            connection.setAutoCommit(autoCommit);
        }
    }
    
    /**
     * Löscht ein Match anhand seiner ID.
     *
     * @param matchId Die ID des zu löschenden Matches
     * @return true wenn der Löschvorgang erfolgreich war, sonst false
     * @throws SQLException Bei Datenbankfehlern
     */
    public boolean deleteMatch(long matchId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM match WHERE id = ?")) {
            pstmt.setLong(1, matchId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Löscht alle Einträge aus allen Tabellen in einer bestimmten Reihenfolge, 
     * um Fremdschlüsselkonflikte zu vermeiden.
     *
     * @return true wenn alle Löschvorgänge erfolgreich waren, sonst false
     * @throws SQLException Bei Datenbankfehlern
     */
    public boolean deleteAllData() throws SQLException {
        // Transaktion beginnen
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        
        try {
            // Löschen in der richtigen Reihenfolge, um Fremdschlüsselkonflikte zu vermeiden
            try (Statement stmt = connection.createStatement()) {
                // Reihenfolge: match -> round -> tournament_player -> tournament -> player
                stmt.executeUpdate("DELETE FROM match");
                stmt.executeUpdate("DELETE FROM round");
                stmt.executeUpdate("DELETE FROM tournament_player");
                stmt.executeUpdate("DELETE FROM tournament");
                stmt.executeUpdate("DELETE FROM player");
                
                // Commit der Transaktion
                connection.commit();
                return true;
            }
        } catch (SQLException e) {
            // Rollback bei Fehler
            connection.rollback();
            throw e;
        } finally {
            // Ursprünglichen AutoCommit-Status wiederherstellen
            connection.setAutoCommit(autoCommit);
        }
    }
    
    /**
     * Löscht alle Turnier-Spieler-Beziehungen für einen bestimmten Spieler.
     *
     * @param playerId Die ID des Spielers
     * @return Die Anzahl der gelöschten Beziehungen
     * @throws SQLException Bei Datenbankfehlern
     */
    public int deletePlayerFromAllTournaments(long playerId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM tournament_player WHERE player_id = ?")) {
            pstmt.setLong(1, playerId);
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * Löscht alle Matches, an denen ein bestimmter Spieler beteiligt ist.
     *
     * @param playerId Die ID des Spielers
     * @return Die Anzahl der gelöschten Matches
     * @throws SQLException Bei Datenbankfehlern
     */
    public int deleteMatchesForPlayer(long playerId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "DELETE FROM match WHERE player1_id = ? OR player2_id = ?")) {
            pstmt.setLong(1, playerId);
            pstmt.setLong(2, playerId);
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * Löscht alle Datensätze einer bestimmten Tabelle.
     *
     * @param tableName Der Name der zu löschenden Tabelle
     * @return true wenn der Löschvorgang erfolgreich war, sonst false
     * @throws SQLException Bei Datenbankfehlern
     */
    public boolean clearTable(String tableName) throws SQLException {
        // Sicherheitsüberprüfung: Nur erlaubte Tabellen löschen
        if (!isValidTable(tableName)) {
            throw new IllegalArgumentException("Ungültiger Tabellenname: " + tableName);
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + tableName);
            return true;
        }
    }
    
    /**
     * Überprüft, ob der angegebene Tabellenname gültig ist.
     *
     * @param tableName Der zu überprüfende Tabellenname
     * @return true wenn der Tabellenname gültig ist, sonst false
     */
    private boolean isValidTable(String tableName) {
        // Liste der gültigen Tabellennamen
        return tableName.matches("^(player|tournament|round|match|tournament_player)$");
    }
}