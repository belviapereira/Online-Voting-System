package com.votingsystem.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:voting_system.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found in classpath.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT CHECK(role IN ('ADMIN', 'VOTER')) NOT NULL
            );
        """;

        String createElectionsTable = """
            CREATE TABLE IF NOT EXISTS elections (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                is_active INTEGER DEFAULT 1
            );
        """;

        String createCandidatesTable = """
            CREATE TABLE IF NOT EXISTS candidates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                election_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                party TEXT NOT NULL,
                FOREIGN KEY (election_id) REFERENCES elections(id) ON DELETE CASCADE
            );
        """;

        String createVotesTable = """
            CREATE TABLE IF NOT EXISTS votes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                election_id INTEGER NOT NULL,
                voter_id INTEGER NOT NULL,
                candidate_id INTEGER NOT NULL,
                FOREIGN KEY (election_id) REFERENCES elections(id),
                FOREIGN KEY (voter_id) REFERENCES users(id),
                FOREIGN KEY (candidate_id) REFERENCES candidates(id),
                UNIQUE(election_id, voter_id)
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createElectionsTable);
            stmt.execute(createCandidatesTable);
            stmt.execute(createVotesTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}