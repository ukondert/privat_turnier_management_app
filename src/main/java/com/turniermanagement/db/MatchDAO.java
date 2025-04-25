package com.turniermanagement.db;

import com.turniermanagement.model.Match;
import com.turniermanagement.model.MatchStatus;
import com.turniermanagement.model.Player;
import java.sql.*;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

public class MatchDAO {
    private final PlayerDAO playerDAO;

    public MatchDAO() {
        this.playerDAO = new PlayerDAO() {
            @Override
            protected Connection getConnection() {
                return MatchDAO.this.getConnection();
            }
        };
    }

    protected Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public void save(Match match, Long roundId) throws SQLException {
        String sql = "INSERT INTO match (round_id, player1_id, player2_id, winner_id, score_player1, score_player2, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, roundId);
            pstmt.setLong(2, match.getPlayer1().getId());
            pstmt.setLong(3, match.getPlayer2().getId());
            pstmt.setObject(4, match.getWinner() != null ? match.getWinner().getId() : null);
            pstmt.setInt(5, match.getScorePlayer1());
            pstmt.setInt(6, match.getScorePlayer2());
            pstmt.setString(7, match.getStatus().toString());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    match.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public void update(Match match) throws SQLException {
        String sql = "UPDATE match SET player1_id = ?, player2_id = ?, winner_id = ?, " +
                    "score_player1 = ?, score_player2 = ?, status = ? WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, match.getPlayer1().getId());
            pstmt.setLong(2, match.getPlayer2().getId());
            pstmt.setObject(3, match.getWinner() != null ? match.getWinner().getId() : null);
            pstmt.setInt(4, match.getScorePlayer1());
            pstmt.setInt(5, match.getScorePlayer2());
            pstmt.setString(6, match.getStatus().toString());
            pstmt.setLong(7, match.getId());
            pstmt.executeUpdate();
        }
    }

    public Optional<Match> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM match WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(createMatchFromResultSet(rs));
            }
        }
        return Optional.empty();
    }

    public List<Match> findByRoundId(Long roundId) throws SQLException {
        List<Match> matches = new ArrayList<>();
        String sql = "SELECT * FROM match WHERE round_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, roundId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                matches.add(createMatchFromResultSet(rs));
            }
        }
        return matches;
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM match WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    private Match createMatchFromResultSet(ResultSet rs) throws SQLException {
        Match match = new Match();
        match.setId(rs.getLong("id"));
        
        Long player1Id = rs.getLong("player1_id");
        Long player2Id = rs.getLong("player2_id");
        Long winnerId = rs.getObject("winner_id") != null ? rs.getLong("winner_id") : null;
        
        playerDAO.findById(player1Id).ifPresent(match::setPlayer1);
        playerDAO.findById(player2Id).ifPresent(match::setPlayer2);
        if (winnerId != null) {
            playerDAO.findById(winnerId).ifPresent(match::setWinner);
        }
        
        match.setScorePlayer1(rs.getInt("score_player1"));
        match.setScorePlayer2(rs.getInt("score_player2"));
        match.setStatus(MatchStatus.valueOf(rs.getString("status")));
        
        return match;
    }
}