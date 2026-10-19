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
        String sql = "SELECT * FROM elections";
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Election(rs.getInt("id"), rs.getString("title"), rs.getInt("is_active") == 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}