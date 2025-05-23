package com.turniermanagement.db;

import com.turniermanagement.model.Round;
import com.turniermanagement.model.Match;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteRoundDAO implements RoundDAO {
    private final MatchDAO matchDAO;
    private Connection connection;

    public SQLiteRoundDAO() {
        this(DatabaseConnection.getInstance().getConnection());
    }
    
    public SQLiteRoundDAO(Connection connection) {
        this.connection = connection;
        this.matchDAO = new SQLiteMatchDAO(connection);
    }

    protected Connection getConnection() {
        return connection;
    }

    @Override
    public void save(Round round) throws SQLException {
        throw new UnsupportedOperationException("Bitte save(Round, Long) verwenden, um eine Verbindung zum Turnier herzustellen");
    }
    
    @Override
    public void save(Round round, Long tournamentId) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "INSERT INTO round (tournament_id, round_number, completed) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, tournamentId);
                pstmt.setInt(2, round.getRoundNumber());
                pstmt.setBoolean(3, round.isCompleted());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        round.setId(generatedKeys.getLong(1));
                    }
                }
            }

            // Speichere alle Matches der Runde
            for (Match match : round.getMatches()) {
                matchDAO.save(match, round.getId());
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
    public void update(Round round) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            String sql = "UPDATE round SET round_number = ?, completed = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, round.getRoundNumber());
                pstmt.setBoolean(2, round.isCompleted());
                pstmt.setLong(3, round.getId());
                pstmt.executeUpdate();
            }

            // Update existierende Matches
            for (Match match : round.getMatches()) {
                if (match.getId() != null) {
                    matchDAO.update(match);
                } else {
                    matchDAO.save(match, round.getId());
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

    @Override
    public Optional<Round> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM round WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Round round = createRoundFromResultSet(rs);
                loadRoundMatches(round);
                return Optional.of(round);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Round> findByTournamentId(Long tournamentId) throws SQLException {
        List<Round> rounds = new ArrayList<>();
        String sql = "SELECT * FROM round WHERE tournament_id = ? ORDER BY round_number";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, tournamentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Round round = createRoundFromResultSet(rs);
                loadRoundMatches(round);
                rounds.add(round);
            }
        }
        return rounds;
    }

    @Override
    public List<Round> findAll() throws SQLException {
        List<Round> rounds = new ArrayList<>();
        String sql = "SELECT * FROM round ORDER BY tournament_id, round_number";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Round round = createRoundFromResultSet(rs);
                loadRoundMatches(round);
                rounds.add(round);
            }
        }
        return rounds;
    }

    @Override
    public void delete(Long id) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            // Lösche erst alle Matches der Runde
            List<Match> matches = matchDAO.findByRoundId(id);
            for (Match match : matches) {
                matchDAO.delete(match.getId());
            }

            // Dann lösche die Runde selbst
            String sql = "DELETE FROM round WHERE id = ?";
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

    private Round createRoundFromResultSet(ResultSet rs) throws SQLException {
        Round round = new Round();
        round.setId(rs.getLong("id"));
        round.setRoundNumber(rs.getInt("round_number"));
        round.setCompleted(rs.getBoolean("completed"));
        return round;
    }

    private void loadRoundMatches(Round round) throws SQLException {
        List<Match> matches = matchDAO.findByRoundId(round.getId());
        for (Match match : matches) {
            round.addMatch(match);
        }
    }
}