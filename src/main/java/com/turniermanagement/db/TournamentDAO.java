package com.turniermanagement.db;

import com.turniermanagement.model.Tournament;
import com.turniermanagement.model.Player;
import com.turniermanagement.model.TournamentStatus;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TournamentDAO {
    private final PlayerDAO playerDAO;

    public TournamentDAO() {
        this.playerDAO = new PlayerDAO();
    }

    protected Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public void save(Tournament tournament) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "INSERT INTO tournament (name, start_date, end_date, status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, tournament.getName());
                pstmt.setString(2, tournament.getStartDate().toString());
                pstmt.setString(3, tournament.getEndDate().toString());
                pstmt.setString(4, tournament.getStatus().toString());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        tournament.setId(generatedKeys.getLong(1));
                    }
                }
            }

            // Speichere Spieler-Turnier-Beziehungen
            for (Player player : tournament.getPlayers()) {
                addPlayerToTournament(tournament.getId(), player.getId(), connection);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void addPlayerToTournament(Long tournamentId, Long playerId, Connection connection) throws SQLException {
        String sql = "INSERT INTO tournament_player (tournament_id, player_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, tournamentId);
            pstmt.setLong(2, playerId);
            pstmt.executeUpdate();
        }
    }

    private void removeAllPlayersFromTournament(Long tournamentId, Connection connection) throws SQLException {
        String sql = "DELETE FROM tournament_player WHERE tournament_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, tournamentId);
            pstmt.executeUpdate();
        }
    }

    private void loadTournamentPlayers(Tournament tournament, Connection connection) throws SQLException {
        String sql = "SELECT p.* FROM player p " +
                    "JOIN tournament_player tp ON p.id = tp.player_id " +
                    "WHERE tp.tournament_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, tournament.getId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getLong("id"));
                player.setName(rs.getString("name"));
                player.setRanking(rs.getInt("ranking"));
                player.setGamesWon(rs.getInt("games_won"));
                player.setGamesLost(rs.getInt("games_lost"));
                tournament.addPlayer(player);
            }
        }
    }

    public void update(Tournament tournament) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "UPDATE tournament SET name = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, tournament.getName());
                pstmt.setString(2, tournament.getStartDate().toString());
                pstmt.setString(3, tournament.getEndDate().toString());
                pstmt.setString(4, tournament.getStatus().toString());
                pstmt.setLong(5, tournament.getId());
                pstmt.executeUpdate();
            }

            // Aktualisiere Spieler-Turnier-Beziehungen
            removeAllPlayersFromTournament(tournament.getId(), connection);
            for (Player player : tournament.getPlayers()) {
                addPlayerToTournament(tournament.getId(), player.getId(), connection);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public Optional<Tournament> findById(Long id) throws SQLException {
        Connection connection = getConnection();
        String sql = "SELECT * FROM tournament WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Tournament tournament = createTournamentFromResultSet(rs);
                loadTournamentPlayers(tournament, connection);
                return Optional.of(tournament);
            }
        }
        return Optional.empty();
    }

    public List<Tournament> findAll() throws SQLException {
        List<Tournament> tournaments = new ArrayList<>();
        String sql = "SELECT * FROM tournament";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Tournament tournament = createTournamentFromResultSet(rs);
                loadTournamentPlayers(tournament);
                tournaments.add(tournament);
            }
        }
        return tournaments;
    }

    public void delete(Long id) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            removeAllPlayersFromTournament(id, connection);
            
            String sql = "DELETE FROM tournament WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    private Tournament createTournamentFromResultSet(ResultSet rs) throws SQLException {
        Tournament tournament = new Tournament();
        tournament.setId(rs.getLong("id"));
        tournament.setName(rs.getString("name"));
        tournament.setStartDate(LocalDate.parse(rs.getString("start_date")));
        tournament.setEndDate(LocalDate.parse(rs.getString("end_date")));
        tournament.setStatus(TournamentStatus.valueOf(rs.getString("status")));
        return tournament;
    }

    private void loadTournamentPlayers(Tournament tournament) throws SQLException {
        String sql = "SELECT player_id FROM tournament_player WHERE tournament_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, tournament.getId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Long playerId = rs.getLong("player_id");
                playerDAO.findById(playerId).ifPresent(tournament::addPlayer);
            }
        }
    }
}