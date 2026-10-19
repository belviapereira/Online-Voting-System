package com.votingsystem.dao;

import com.votingsystem.config.Database;
import com.votingsystem.model.User;
import com.votingsystem.util.PasswordUtil;

import java.sql.*;

public class UserDAO {
    public boolean registerUser(String username, String plainPassword, String role) {
        String sql = "INSERT INTO users(username, password, role) VALUES(?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, PasswordUtil.hashPassword(plainPassword));
            pstmt.setString(3, role != null ? role.trim().toUpperCase() : "VOTER");
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User authenticate(String username, String plainPassword) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String hashed = rs.getString("password");
                if (PasswordUtil.checkPassword(plainPassword, hashed)) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        hashed,
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getTotalVotersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'VOTER'";
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