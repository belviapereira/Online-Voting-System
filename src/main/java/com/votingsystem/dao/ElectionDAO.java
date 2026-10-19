package com.votingsystem.dao;

import com.votingsystem.config.Database;
import com.votingsystem.model.Candidate;
import com.votingsystem.model.Election;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ElectionDAO {
    public boolean createElection(String title) {
        String sql = "INSERT INTO elections(title, is_active) VALUES(?, 1)";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean addCandidate(int electionId, String name, String party) {
        String sql = "INSERT INTO candidates(election_id, name, party) VALUES(?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, electionId);
            pstmt.setString(2, name);
            pstmt.setString(3, party);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Election> getActiveElections() {
        List<Election> list = new ArrayList<>();
        String sql = "SELECT * FROM elections WHERE is_active = 1";
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Election(rs.getInt("id"), rs.getString("title"), rs.getInt("is_active") == 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Candidate> getCandidatesForElection(int electionId) {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT * FROM candidates WHERE election_id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, electionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Candidate(rs.getInt("id"), rs.getInt("election_id"), rs.getString("name"), rs.getString("party")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getTotalCandidatesCount() {
        String sql = "SELECT COUNT(*) FROM candidates";
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Election> getAllElections() {
        List<Election> list = new ArrayList<>();
        String sql = "SELECT * FROM elections ORDER BY id DESC";
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Election(rs.getInt("id"), rs.getString("title"), rs.getInt("is_active") == 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean toggleElectionStatus(int electionId, boolean isActive) {
        String sql = "UPDATE elections SET is_active = ? WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isActive ? 1 : 0);
            pstmt.setInt(2, electionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteElection(int electionId) {
        // Delete votes and candidates first to ensure clean cascade in SQLite
        String deleteVotesSql = "DELETE FROM votes WHERE election_id = ?";
        String deleteCandidatesSql = "DELETE FROM candidates WHERE election_id = ?";
        String deleteElectionSql = "DELETE FROM elections WHERE id = ?";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pVotes = conn.prepareStatement(deleteVotesSql);
                 PreparedStatement pCandidates = conn.prepareStatement(deleteCandidatesSql);
                 PreparedStatement pElection = conn.prepareStatement(deleteElectionSql)) {
                
                pVotes.setInt(1, electionId);
                pVotes.executeUpdate();

                pCandidates.setInt(1, electionId);
                pCandidates.executeUpdate();

                pElection.setInt(1, electionId);
                int affected = pElection.executeUpdate();

                conn.commit();
                return affected > 0;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCandidate(int candidateId) {
        String deleteVotesSql = "DELETE FROM votes WHERE candidate_id = ?";
        String deleteCandidateSql = "DELETE FROM candidates WHERE id = ?";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pVotes = conn.prepareStatement(deleteVotesSql);
                 PreparedStatement pCand = conn.prepareStatement(deleteCandidateSql)) {

                pVotes.setInt(1, candidateId);
                pVotes.executeUpdate();

                pCand.setInt(1, candidateId);
                int affected = pCand.executeUpdate();

                conn.commit();
                return affected > 0;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Election getElectionById(int electionId) {
        String sql = "SELECT * FROM elections WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, electionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Election(rs.getInt("id"), rs.getString("title"), rs.getInt("is_active") == 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}