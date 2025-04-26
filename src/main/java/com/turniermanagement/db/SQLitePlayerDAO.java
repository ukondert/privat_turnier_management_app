package com.turniermanagement.db;

import com.turniermanagement.model.Player;
import com.turniermanagement.model.Tournament;
import com.turniermanagement.model.TournamentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLitePlayerDAO implements PlayerDAO {
    private Connection connection;
    
    public SQLitePlayerDAO() {
        // Standardkonstruktor für normale Anwendung
        this(DatabaseConnection.getInstance().getConnection());
    }
    
    public SQLitePlayerDAO(Connection connection) {
        // Konstruktor für Tests mit übergebener Verbindung
        this.connection = connection;
    }

    protected Connection getConnection() {
        return connection;
    }

    @Override
    public void save(Player player) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "INSERT INTO player (name, games_won, games_lost) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, player.getName());
                pstmt.setInt(2, player.getGamesWon());
                pstmt.setInt(3, player.getGamesLost());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        player.setId(generatedKeys.getLong(1));
                    }
                }
            }

            // Speichere Tournament-Beziehungen und Rankings
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
        String sql = "INSERT INTO tournament_player (player_id, tournament_id, ranking) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Tournament tournament : player.getTournaments()) {
                if (tournament.getId() != null) {
                    pstmt.setLong(1, player.getId());
                    pstmt.setLong(2, tournament.getId());
                    pstmt.setInt(3, player.getRanking(tournament));
                    pstmt.addBatch();
                    
                    // Ensure the tournament also has this player in its list
                    // But only add to the DB relation, no circular method calls
                    if (!tournament.getPlayers().contains(player)) {
                        tournament.addPlayer(player);
                    }
                }
            }
            pstmt.executeBatch();
        }
    }

    @Override
    public void update(Player player) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "UPDATE player SET name = ?, games_won = ?, games_lost = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, player.getName());
                pstmt.setInt(2, player.getGamesWon());
                pstmt.setInt(3, player.getGamesLost());
                pstmt.setLong(4, player.getId());
                pstmt.executeUpdate();
            }

            // Aktualisiere Tournament-Beziehungen und Rankings
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
        connection.commit();
    }

    @Override
    public Optional<Player> findById(Long id) throws SQLException {
        String sql = "SELECT p.*, t.id as tournament_id, t.name as tournament_name, " +
                    "t.start_date, t.end_date, t.status, tp.ranking " +
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
                    
                    // Setze das Ranking für dieses Tournament
                    int ranking = rs.getInt("ranking");
                    player.setRanking(tournament, ranking);
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

    @Override
    public List<Player> findAll() throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT p.*, t.id as tournament_id, t.name as tournament_name, " +
                    "t.start_date, t.end_date, t.status, tp.ranking " +
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
                    
                    // Setze das Ranking für dieses Tournament
                    int ranking = rs.getInt("ranking");
                    currentPlayer.setRanking(tournament, ranking);
                }
            }
        }
        return players;
    }

    @Override
    public void delete(Long id) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            // First get the player to update tournament relationships
            Optional<Player> player = findById(id);
            
            // Update tournaments to remove this player from their lists
            if (player.isPresent()) {
                // Get list of tournaments this player belongs to
                List<Tournament> playerTournaments = new ArrayList<>(player.get().getTournaments());
                
                // Prepare Tournament objects for relationship removal
                for (Tournament tournament : playerTournaments) {
                    tournament.removePlayer(player.get());
                }
            }
            
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
        player.setGamesWon(rs.getInt("games_won"));
        player.setGamesLost(rs.getInt("games_lost"));
        return player;
    }
    
    @Override
    public void updatePlayerRanking(Player player, Tournament tournament, int ranking) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "UPDATE tournament_player SET ranking = ? WHERE player_id = ? AND tournament_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, ranking);
                pstmt.setLong(2, player.getId());
                pstmt.setLong(3, tournament.getId());
                int rowsAffected = pstmt.executeUpdate();
                
                // Wenn keine Zeile aktualisiert wurde, füge eine neue hinzu
                if (rowsAffected == 0) {
                    String insertSql = "INSERT INTO tournament_player (player_id, tournament_id, ranking) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setLong(1, player.getId());
                        insertStmt.setLong(2, tournament.getId());
                        insertStmt.setInt(3, ranking);
                        insertStmt.executeUpdate();
                    }
                }
            }
            connection.commit();
            
            // Aktualisiere das Objekt
            player.setRanking(tournament, ranking);
            
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}