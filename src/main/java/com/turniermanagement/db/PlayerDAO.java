package com.turniermanagement.db;

import com.turniermanagement.model.Player;
import com.turniermanagement.model.Tournament;
import com.turniermanagement.model.TournamentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerDAO {
    private Connection connection;
    
    public PlayerDAO() {
        // Standardkonstruktor für normale Anwendung
        this(DatabaseConnection.getInstance().getConnection());
    }
    
    public PlayerDAO(Connection connection) {
        // Konstruktor für Tests mit übergebener Verbindung
        this.connection = connection;
    }

    protected Connection getConnection() {
        return connection;
    }

    public void save(Player player) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "INSERT INTO player (name, ranking, games_won, games_lost) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, player.getName());
                pstmt.setInt(2, player.getRanking());
                pstmt.setInt(3, player.getGamesWon());
                pstmt.setInt(4, player.getGamesLost());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        player.setId(generatedKeys.getLong(1));
                    }
                }
            }

            // Speichere Tournament-Beziehungen
            if (player.getTournaments() != null && !player.getTournaments().isEmpty()) {
                saveTournamentRelations(player, connection);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void saveTournamentRelations(Player player, Connection connection) throws SQLException {
        String sql = "INSERT INTO tournament_player (player_id, tournament_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Tournament tournament : player.getTournaments()) {
                if (tournament.getId() != null) {
                    pstmt.setLong(1, player.getId());
                    pstmt.setLong(2, tournament.getId());
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }
    }

    public void update(Player player) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "UPDATE player SET name = ?, ranking = ?, games_won = ?, games_lost = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, player.getName());
                pstmt.setInt(2, player.getRanking());
                pstmt.setInt(3, player.getGamesWon());
                pstmt.setInt(4, player.getGamesLost());
                pstmt.setLong(5, player.getId());
                pstmt.executeUpdate();
            }

            // Aktualisiere Tournament-Beziehungen
            updateTournamentRelations(player, connection);

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void updateTournamentRelations(Player player, Connection connection) throws SQLException {
        // Lösche alte Beziehungen
        String deleteSql = "DELETE FROM tournament_player WHERE player_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setLong(1, player.getId());
            pstmt.executeUpdate();
        }

        // Füge neue Beziehungen hinzu
        if (player.getTournaments() != null && !player.getTournaments().isEmpty()) {
            saveTournamentRelations(player, connection);
        }
    }

    public Optional<Player> findById(Long id) throws SQLException {
        String sql = "SELECT p.*, t.id as tournament_id, t.name as tournament_name, " +
                    "t.start_date, t.end_date, t.status " +
                    "FROM player p " +
                    "LEFT JOIN tournament_player tp ON p.id = tp.player_id " +
                    "LEFT JOIN tournament t ON tp.tournament_id = t.id " +
                    "WHERE p.id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            Player player = null;
            while (rs.next()) {
                if (player == null) {
                    player = createPlayerFromResultSet(rs);
                }
                Long tournamentId = rs.getLong("tournament_id");
                if (!rs.wasNull()) {
                    Tournament tournament = createTournamentFromResultSet(rs);
                    player.getTournaments().add(tournament);
                }
            }
            return Optional.ofNullable(player);
        }
    }

    private Tournament createTournamentFromResultSet(ResultSet rs) throws SQLException {
        Tournament tournament = new Tournament();
        tournament.setId(rs.getLong("tournament_id"));
        tournament.setName(rs.getString("tournament_name"));
        tournament.setStartDate(DatabaseConnection.getLocalDate(rs, "start_date"));
        tournament.setEndDate(DatabaseConnection.getLocalDate(rs, "end_date"));
        tournament.setStatus(TournamentStatus.valueOf(rs.getString("status")));
        return tournament;
    }

    public List<Player> findAll() throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT p.*, t.id as tournament_id, t.name as tournament_name, " +
                    "t.start_date, t.end_date, t.status " +
                    "FROM player p " +
                    "LEFT JOIN tournament_player tp ON p.id = tp.player_id " +
                    "LEFT JOIN tournament t ON tp.tournament_id = t.id";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            Long currentPlayerId = null;
            Player currentPlayer = null;
            
            while (rs.next()) {
                Long playerId = rs.getLong("id");
                if (!playerId.equals(currentPlayerId)) {
                    currentPlayer = createPlayerFromResultSet(rs);
                    currentPlayerId = playerId;
                    players.add(currentPlayer);
                }
                
                Long tournamentId = rs.getLong("tournament_id");
                if (!rs.wasNull()) {
                    Tournament tournament = createTournamentFromResultSet(rs);
                    currentPlayer.getTournaments().add(tournament);
                }
            }
        }
        return players;
    }

    public void delete(Long id) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            // Lösche zuerst die Beziehungen
            String deleteRelationsSql = "DELETE FROM tournament_player WHERE player_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteRelationsSql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            }

            // Dann lösche den Player
            String deletePlayerSql = "DELETE FROM player WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deletePlayerSql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private Player createPlayerFromResultSet(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.setId(rs.getLong("id"));
        player.setName(rs.getString("name"));
        player.setRanking(rs.getInt("ranking"));
        player.setGamesWon(rs.getInt("games_won"));
        player.setGamesLost(rs.getInt("games_lost"));
        return player;
    }
}