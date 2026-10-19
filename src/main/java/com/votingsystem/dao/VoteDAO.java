package com.votingsystem.dao;

import com.votingsystem.config.Database;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class VoteDAO {
    public boolean castVote(int electionId, int voterId, int candidateId) {
        String sql = "INSERT INTO votes(election_id, voter_id, candidate_id) VALUES(?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, electionId);
            pstmt.setInt(2, voterId);
            pstmt.setInt(3, candidateId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // Unique constraint hit or database error
        }
    }

    public boolean hasVoted(int electionId, int voterId) {
        String sql = "SELECT id FROM votes WHERE election_id = ? AND voter_id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, electionId);
            pstmt.setInt(2, voterId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public Map<String, Integer> getResults(int electionId) {
        Map<String, Integer> results = new HashMap<>();
        String sql = """
            SELECT c.name || ' (' || c.party || ')' as candidate_label, COUNT(v.id) as vote_count
            FROM candidates c
            LEFT JOIN votes v ON c.id = v.candidate_id
            WHERE c.election_id = ?
            GROUP BY c.id
        """;
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, electionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.put(rs.getString("candidate_label"), rs.getInt("vote_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int getTotalVotesCount() {
        String sql = "SELECT COUNT(*) FROM votes";
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}