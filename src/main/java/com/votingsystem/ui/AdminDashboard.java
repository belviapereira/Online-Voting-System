package com.votingsystem.ui;

import com.votingsystem.dao.ElectionDAO;
import com.votingsystem.dao.VoteDAO;
import com.votingsystem.model.Election;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {
    private ElectionDAO electionDAO = new ElectionDAO();
    private VoteDAO voteDAO = new VoteDAO();

    private JComboBox<Election> electionComboBox = new JComboBox<>();
    private JPanel chartContainer = new JPanel(new BorderLayout());

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Create Election", createElectionPanel());
        tabbedPane.add("Add Candidate", createCandidatePanel());
        tabbedPane.add("View Results", createResultsPanel());

        add(tabbedPane);
        loadElections();
    }

    private JPanel createElectionPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JTextField titleField = new JTextField(20);
        JButton createBtn = new JButton("Create Election");

        panel.add(new JLabel("Election Title:"));
        panel.add(titleField);
        panel.add(createBtn);

        createBtn.addActionListener(e -> {
            if (electionDAO.createElection(titleField.getText())) {
                JOptionPane.showMessageDialog(this, "Election Created");
                titleField.setText("");
                loadElections();
            }
        });
        return panel;
    }

    private JPanel createCandidatePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField nameField = new JTextField();
        JTextField partyField = new JTextField();
        JButton addBtn = new JButton("Add Candidate");

        panel.add(new JLabel("Select Election:"));
        panel.add(electionComboBox);
        panel.add(new JLabel("Candidate Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Party:"));
        panel.add(partyField);
        panel.add(new JLabel());
        panel.add(addBtn);

        addBtn.addActionListener(e -> {
            Election selected = (Election) electionComboBox.getSelectedItem();
            if (selected != null && electionDAO.addCandidate(selected.getId(), nameField.getText(), partyField.getText())) {
                JOptionPane.showMessageDialog(this, "Candidate Added");
                nameField.setText("");
                partyField.setText("");
            }
        });
        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JComboBox<Election> resultsCombo = new JComboBox<>();
        JButton refreshBtn = new JButton("View Chart");

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Election:"));
        topPanel.add(resultsCombo);
        topPanel.add(refreshBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(chartContainer, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> {
            Election sel = (Election) resultsCombo.getSelectedItem();
            if (sel != null) {
                renderChart(sel.getId(), sel.getTitle());
            }
        });

        // Sync election combos
        resultsCombo.setModel(electionComboBox.getModel());
        return mainPanel;
    }

    private void renderChart(int electionId, String title) {
        Map<String, Integer> data = voteDAO.getResults(electionId);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        data.forEach((candidate, votes) -> dataset.addValue(votes, "Votes", candidate));

        JFreeChart chart = ChartFactory.createBarChart(title + " Results", "Candidates", "Votes", dataset);
        ChartPanel chartPanel = new ChartPanel(chart);

        chartContainer.removeAll();
        chartContainer.add(chartPanel, BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void loadElections() {
        electionComboBox.removeAllItems();
        List<Election> list = electionDAO.getActiveElections();
        for (Election el : list) {
            electionComboBox.addItem(el);
        }
    }
}