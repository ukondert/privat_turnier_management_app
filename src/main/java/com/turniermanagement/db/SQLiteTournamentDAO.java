package com.turniermanagement.db;

import com.turniermanagement.model.Tournament;
import com.turniermanagement.model.Player;
import com.turniermanagement.model.TournamentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteTournamentDAO implements TournamentDAO {
    private final PlayerDAO playerDAO;
    private Connection connection;

    public SQLiteTournamentDAO() {
        this(DatabaseConnection.getInstance().getConnection());
    }
    
    public SQLiteTournamentDAO(Connection connection) {
        this.connection = connection;
        this.playerDAO = new SQLitePlayerDAO(connection);
    }

    protected Connection getConnection() {
        return connection;
    }

    @Override
    public void save(Tournament tournament) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "INSERT INTO tournament (name, start_date, end_date, status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, tournament.getName());
                DatabaseConnection.setDateParameter(pstmt, 2, tournament.getStartDate(), connection);
                DatabaseConnection.setDateParameter(pstmt, 3, tournament.getEndDate(), connection);
                pstmt.setString(4, tournament.getStatus().name());
                pstmt.executeUpdate();
    
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        tournament.setId(generatedKeys.getLong(1));
                    }
                }
            }
    
            // Speichere Player-Beziehungen
            if (tournament.getPlayers() != null && !tournament.getPlayers().isEmpty()) {
                savePlayerRelations(tournament, connection);
            }
    
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void savePlayerRelations(Tournament tournament, Connection connection) throws SQLException {
        String sql = "INSERT INTO tournament_player (tournament_id, player_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Player player : tournament.getPlayers()) {
                if (player.getId() != null) {
                    pstmt.setLong(1, tournament.getId());
                    pstmt.setLong(2, player.getId());
                    pstmt.addBatch();
                    
                    // Update player's tournament list
                    player.addTournament(tournament);
                }
            }
            pstmt.executeBatch();
        }
    }

    @Override
    public void update(Tournament tournament) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "UPDATE tournament SET name = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, tournament.getName());
                DatabaseConnection.setDateParameter(pstmt, 2, tournament.getStartDate(), connection);
                DatabaseConnection.setDateParameter(pstmt, 3, tournament.getEndDate(), connection);
                pstmt.setString(4, tournament.getStatus().name());
                pstmt.setLong(5, tournament.getId());
                pstmt.executeUpdate();
            }

            // Aktualisiere Player-Beziehungen
            updatePlayerRelations(tournament, connection);

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void updatePlayerRelations(Tournament tournament, Connection connection) throws SQLException {
        // Get current player associations before removing them
        List<Player> previousPlayers = new ArrayList<>();
        String selectSql = "SELECT player_id FROM tournament_player WHERE tournament_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectSql)) {
            pstmt.setLong(1, tournament.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long playerId = rs.getLong("player_id");
                    playerDAO.findById(playerId).ifPresent(previousPlayers::add);
                }
            }
        }
        
        // Lösche alte Beziehungen
        String deleteSql = "DELETE FROM tournament_player WHERE tournament_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setLong(1, tournament.getId());
            pstmt.executeUpdate();
        }

        // Update player objects to remove this tournament from their list if they're no longer in it
        for (Player player : previousPlayers) {
            if (!tournament.getPlayers().contains(player)) {
                player.removeTournament(tournament);
            }
        }

        // Füge neue Beziehungen hinzu
        if (tournament.getPlayers() != null && !tournament.getPlayers().isEmpty()) {
            savePlayerRelations(tournament, connection);
        }
    }

    @Override
    public Optional<Tournament> findById(Long id) throws SQLException {
        String sql = "SELECT t.*, p.id as player_id, p.name as player_name, " +
                    "tp.ranking, p.games_won, p.games_lost " +
                    "FROM tournament t " +
                    "LEFT JOIN tournament_player tp ON t.id = tp.tournament_id " +
                    "LEFT JOIN player p ON tp.player_id = p.id " +
                    "WHERE t.id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            Tournament tournament = null;
            while (rs.next()) {
                if (tournament == null) {
                    tournament = createTournamentFromResultSet(rs);
                }
                Long playerId = rs.getLong("player_id");
                if (!rs.wasNull()) {
                    Player player = createPlayerFromResultSet(rs);
                    tournament.addPlayer(player);
                }
            }
            return Optional.ofNullable(tournament);
        }
    }

    @Override
    public List<Tournament> findAll() throws SQLException {
        List<Tournament> tournaments = new ArrayList<>();
        String sql = "SELECT t.*, p.id as player_id, p.name as player_name, " +
                    "tp.ranking, p.games_won, p.games_lost " +
                    "FROM tournament t " +
                    "LEFT JOIN tournament_player tp ON t.id = tp.tournament_id " +
                    "LEFT JOIN player p ON tp.player_id = p.id";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            Long currentTournamentId = null;
            Tournament currentTournament = null;
            
            while (rs.next()) {
                Long tournamentId = rs.getLong("id");
                if (!tournamentId.equals(currentTournamentId)) {
                    currentTournament = createTournamentFromResultSet(rs);
                    currentTournamentId = tournamentId;
                    tournaments.add(currentTournament);
                }
                
                Long playerId = rs.getLong("player_id");
                if (!rs.wasNull()) {
                    Player player = createPlayerFromResultSet(rs);
                    currentTournament.addPlayer(player);
                }
            }
        }
        return tournaments;
    }

    @Override
    public void delete(Long id) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            // First get the tournament to manage player relationships
            Optional<Tournament> tournament = findById(id);
            
            // Lösche zuerst die Beziehungen
            String deleteRelationsSql = "DELETE FROM tournament_player WHERE tournament_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteRelationsSql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            }

            // Update tournament references in players
            if (tournament.isPresent()) {
                for (Player player : tournament.get().getPlayers()) {
                    player.removeTournament(tournament.get());
                }
            }

            // Dann lösche das Tournament
            String deleteTournamentSql = "DELETE FROM tournament WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteTournamentSql)) {
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

    @Override
    public void deleteRound(Long roundId) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            // Lösche zuerst alle Matches der Runde
            String deleteMatchesSql = "DELETE FROM match WHERE round_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteMatchesSql)) {
                pstmt.setLong(1, roundId);
                pstmt.executeUpdate();
            }

            // Dann lösche die Runde selbst
            String deleteRoundSql = "DELETE FROM round WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteRoundSql)) {
                pstmt.setLong(1, roundId);
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

    @Override
    public void removePlayers(Long tournamentId) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            // Hole zuerst das Turnier und die Spieler, um die Objektreferenzen zu aktualisieren
            Optional<Tournament> tournament = findById(tournamentId);
            
            // Lösche dann die Beziehungen aus der Datenbank
            String deleteSql = "DELETE FROM tournament_player WHERE tournament_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
                pstmt.setLong(1, tournamentId);
                pstmt.executeUpdate();
            }

            // Aktualisiere die Objektreferenzen in den Spielern
            if (tournament.isPresent()) {
                Tournament t = tournament.get();
                for (Player player : new ArrayList<>(t.getPlayers())) {
                    player.removeTournament(t);
                    t.removePlayer(player);
                }
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
        tournament.setStartDate(DatabaseConnection.getDateParameter(rs, "start_date", getConnection()));
        tournament.setEndDate(DatabaseConnection.getDateParameter(rs, "end_date", getConnection()));
        tournament.setStatus(TournamentStatus.valueOf(rs.getString("status")));
        return tournament;
    }

    private Player createPlayerFromResultSet(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.setId(rs.getLong("player_id"));
        player.setName(rs.getString("player_name"));
        player.setRanking(rs.getInt("ranking"));
        player.setGamesWon(rs.getInt("games_won"));
        player.setGamesLost(rs.getInt("games_lost"));
        return player;
    }
}