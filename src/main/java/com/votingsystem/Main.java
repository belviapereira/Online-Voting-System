package com.votingsystem;

import com.votingsystem.config.Database;
import com.votingsystem.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Database.initializeDatabase();
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}