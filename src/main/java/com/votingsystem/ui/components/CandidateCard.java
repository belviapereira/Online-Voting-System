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
        super(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        this.candidate = candidate;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(22, 18, 22, 18));
        setPreferredSize(new Dimension(200, 260));
        setMaximumSize(new Dimension(220, 280));

        // 1. Candidate Avatar
        CircularAvatar avatar = new CircularAvatar(candidate.getName(), 68);
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
        detailsBtn.setPreferredSize(new Dimension(150, 34));
        detailsBtn.setMaximumSize(new Dimension(160, 34));
        detailsBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Candidate: " + candidate.getName() + "\n" +
                    "Affiliation / Party: " + candidate.getParty() + "\n\n" +
                    "Platform Statement:\n\"Dedicated to transparent student leadership, digital access, and institutional advocacy.\"",
                    candidate.getName() + " - Profile Details",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        add(avatar);
        add(Box.createVerticalStrut(12));
        add(nameLabel);
        add(Box.createVerticalStrut(4));
        add(partyLabel);
        add(Box.createVerticalStrut(18));
        add(voteBtn);
        add(Box.createVerticalStrut(8));
        add(detailsBtn);
    }
}
