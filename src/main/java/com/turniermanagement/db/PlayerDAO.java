package com.turniermanagement.db;

import com.turniermanagement.model.Player;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerDAO {
    protected Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public void save(Player player) throws SQLException {
        String sql = "INSERT INTO player (name, ranking, games_won, games_lost) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
    }

    public void update(Player player) throws SQLException {
        String sql = "UPDATE player SET name = ?, ranking = ?, games_won = ?, games_lost = ? WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, player.getName());
            pstmt.setInt(2, player.getRanking());
            pstmt.setInt(3, player.getGamesWon());
            pstmt.setInt(4, player.getGamesLost());
            pstmt.setLong(5, player.getId());
            pstmt.executeUpdate();
        }
    }

    public Optional<Player> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM player WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(createPlayerFromResultSet(rs));
            }
        }
        return Optional.empty();
    }

    public List<Player> findAll() throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM player";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                players.add(createPlayerFromResultSet(rs));
            }
        }
        return players;
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM player WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
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