package com.votingsystem.ui.components;

import com.votingsystem.model.Candidate;
import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class CandidateCard extends RoundedPanel {
    private final Candidate candidate;
    private final PillButton voteBtn;
    private final PillButton detailsBtn;

    public CandidateCard(Candidate candidate, boolean hasVoted, Consumer<Candidate> onVote) {
        this(candidate, null, hasVoted, onVote);
    }

    public CandidateCard(Candidate candidate, String electionTitle, boolean hasVoted, Consumer<Candidate> onVote) {
        super(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        this.candidate = candidate;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(18, 16, 18, 16));
        int height = electionTitle != null ? 290 : 260;
        setPreferredSize(new Dimension(205, height));
        setMaximumSize(new Dimension(225, height + 20));

        // Optional election title pill
        if (electionTitle != null && !electionTitle.isEmpty()) {
            JLabel electPill = new JLabel(electionTitle);
            electPill.setFont(new Font("Segoe UI", Font.BOLD, 10));
            electPill.setForeground(Theme.BRAND_ACCENT);
            electPill.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(electPill);
            add(Box.createVerticalStrut(8));
        }

        // 1. Candidate Avatar
        CircularAvatar avatar = new CircularAvatar(candidate.getName(), 64);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Candidate Name
        JLabel nameLabel = new JLabel(candidate.getName());
        nameLabel.setFont(Theme.fontBodyBold());
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Party / Department
        JLabel partyLabel = new JLabel(candidate.getParty() != null ? candidate.getParty() : "Independent");
        partyLabel.setFont(Theme.fontCaption());
        partyLabel.setForeground(Theme.TEXT_SECONDARY);
        partyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Buttons
        voteBtn = new PillButton(hasVoted ? "Voted ✓" : "VOTE", PillButton.Style.GRADIENT);
        voteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        voteBtn.setPreferredSize(new Dimension(150, 36));
        voteBtn.setMaximumSize(new Dimension(160, 36));
        if (hasVoted) {
            voteBtn.setEnabled(false);
            voteBtn.setStyle(PillButton.Style.SECONDARY);
        } else {
            voteBtn.addActionListener(e -> onVote.accept(candidate));
        }

        detailsBtn = new PillButton("View Details", PillButton.Style.OUTLINE);
        detailsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        detailsBtn.setPreferredSize(new Dimension(150, 32));
        detailsBtn.setMaximumSize(new Dimension(160, 32));
        detailsBtn.addActionListener(e -> {
            String titleContext = electionTitle != null ? "Election: " + electionTitle + "\n" : "";
            JOptionPane.showMessageDialog(this,
                    titleContext +
                    "Candidate: " + candidate.getName() + "\n" +
                    "Affiliation / Party: " + candidate.getParty() + "\n\n" +
                    "Platform Statement:\n\"Dedicated to transparent student leadership, digital access, and institutional advocacy.\"",
                    candidate.getName() + " - Profile Details",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        add(avatar);
        add(Box.createVerticalStrut(10));
        add(nameLabel);
        add(Box.createVerticalStrut(4));
        add(partyLabel);
        add(Box.createVerticalStrut(14));
        add(voteBtn);
        add(Box.createVerticalStrut(8));
        add(detailsBtn);
    }
}
