package com.votingsystem.ui;

import com.votingsystem.dao.ElectionDAO;
import com.votingsystem.dao.UserDAO;
import com.votingsystem.dao.VoteDAO;
import com.votingsystem.model.Candidate;
import com.votingsystem.model.Election;
import com.votingsystem.model.User;
import com.votingsystem.ui.components.*;
import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VoterDashboard extends JFrame {
    private final User currentUser;
    private final ElectionDAO electionDAO = new ElectionDAO();
    private final VoteDAO voteDAO = new VoteDAO();
    private final UserDAO userDAO = new UserDAO();

    // Top-level containers for toggling
    private JPanel bodyPanel;
    private RoundedPanel sidebar;
    private PillTextField searchField;

    // Navigation and Views
    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel viewsContainer = new JPanel(contentCardLayout);

    // Sidebar navigation buttons
    private PillButton navDashboardBtn;
    private PillButton navVoteBtn;
    private PillButton navGuidelinesBtn;
    private PillButton navSettingsBtn;
    private PillButton navLogoutBtn;

    // Center widgets
    private JLabel ongoingElectionTitleLabel;
    private CardLayout resultsCardLayout = new CardLayout();
    private JPanel resultsDisplayContainer;
    private HorizontalBarChart liveResultsChart;
    private JLabel chartElectionTitleLabel;
    private int currentChartElectionIndex = 0;
    private List<Election> allElectionsList;

    // Voting screen widgets
    private JComboBox<Election> voteElectionCombo;
    private JPanel candidatesGridPanel;
    private JLabel searchStatusLabel;
    private PillButton clearSearchBtn;
    private String currentSearchQuery = "";

    // Global Search and Navigation State
    private String currentNav = "DASHBOARD";
    private String previousNav = "DASHBOARD";
    private JPanel globalSearchResultsContainer;
    private JLabel globalSearchCountLabel;

    // Right stats rail widgets
    private JPanel statsContainer;

    public VoterDashboard(User user) {
        this.currentUser = user;
        setTitle("iVOTE - Voter Portal (" + user.getUsername() + ")");
        setSize(1240, 820);
        setMinimumSize(new Dimension(1100, 740));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Background Canvas
        JPanel canvas = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyRenderHints(g2);
                g2.setColor(Theme.CANVAS_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        canvas.setBorder(new EmptyBorder(16, 24, 20, 24));

        // 1. Top Header Bar
        canvas.add(createTopBar(), BorderLayout.NORTH);

        // 2. Main Body Split: Left Sidebar + Center/Right Workspace
        bodyPanel = new JPanel(new BorderLayout(20, 0));
        bodyPanel.setOpaque(false);

        sidebar = createSidebar();
        bodyPanel.add(sidebar, BorderLayout.WEST);

        // Views setup in Center
        viewsContainer.setOpaque(false);
        viewsContainer.add(createDashboardView(), "DASHBOARD");
        viewsContainer.add(createVoteView(), "VOTE");
        viewsContainer.add(createGuidelinesView(), "GUIDELINES");
        viewsContainer.add(createSettingsView(), "SETTINGS");
        viewsContainer.add(createGlobalSearchView(), "SEARCH");

        bodyPanel.add(viewsContainer, BorderLayout.CENTER);
        canvas.add(bodyPanel, BorderLayout.CENTER);

        add(canvas);

        loadInitialData();
        setActiveNav("DASHBOARD");
    }

    // ==========================================
    // 1. TOP HEADER BAR
    // ==========================================
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(20, 0));
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));

        // Left: Hamburger Menu Icon + Brand Logo
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        left.setOpaque(false);

        HeaderIcon menuIcon = new HeaderIcon(HeaderIcon.Type.HAMBURGER, 20);
        menuIcon.setToolTipText("Toggle Navigation Sidebar");
        menuIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleSidebar();
            }
        });
        left.add(menuIcon);
        left.add(new BrandBadge(34));

        bar.add(left, BorderLayout.WEST);

        // Center: Interactive Search Field
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        center.setOpaque(false);
        searchField = new PillTextField("Search elections, candidates, party...", 30);
        searchField.setShowSearchIcon(true);
        searchField.setPreferredSize(new Dimension(420, 38));

        // Live real-time search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                triggerSearch();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                triggerSearch();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                triggerSearch();
            }
        });

        searchField.addActionListener(e -> triggerSearch());
        center.add(searchField);
        bar.add(center, BorderLayout.CENTER);

        // Right: Notification Bell, Help, Dynamic Profile Chip
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 2));
        right.setOpaque(false);

        // Notification Bell Button
        HeaderIcon bell = new HeaderIcon(HeaderIcon.Type.BELL, 22);
        bell.setShowBadge(true);
        bell.setToolTipText("Election Notifications (Click to view)");
        bell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNotificationsDialog();
            }
        });

        // Question Mark Help Button (with pixel-perfect centering)
        HeaderIcon help = new HeaderIcon(HeaderIcon.Type.HELP, 22);
        help.setToolTipText("Voting Help & Election FAQ");
        help.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showHelpDialog();
            }
        });

        // Dynamic Profile Chip (sized generously to prevent username clutter)
        JPanel userChip = createUserProfileChip();

        right.add(bell);
        right.add(help);
        right.add(userChip);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel createUserProfileChip() {
        Font chipFont = Theme.fontBodyBold();
        FontMetrics fm = getFontMetrics(chipFont);
        int textWidth = fm.stringWidth(currentUser.getUsername());
        // Dynamic chip width: avatar (30) + gap (10) + textWidth + horizontal padding (24)
        int chipWidth = Math.max(140, 30 + 10 + textWidth + 24);

        JPanel userChip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyRenderHints(g2);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(Theme.CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        userChip.setOpaque(false);
        userChip.setPreferredSize(new Dimension(chipWidth, 38));
        userChip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userChip.setToolTipText("Logged in as " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");

        userChip.add(new CircularAvatar(currentUser.getUsername(), 28));

        JLabel userNameLabel = new JLabel(currentUser.getUsername());
        userNameLabel.setFont(chipFont);
        userNameLabel.setForeground(Theme.TEXT_PRIMARY);
        userChip.add(userNameLabel);

        userChip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveNav("SETTINGS");
            }
        });

        return userChip;
    }

    private void toggleSidebar() {
        if (sidebar != null && bodyPanel != null) {
            sidebar.setVisible(!sidebar.isVisible());
            bodyPanel.revalidate();
            bodyPanel.repaint();
        }
    }

    private void triggerSearch() {
        String query = searchField.getText().trim();
        this.currentSearchQuery = query;

        if (!query.isEmpty()) {
            if (!"SEARCH".equals(currentNav)) {
                previousNav = currentNav;
            }
            performGlobalSearch(query);
            setActiveNav("SEARCH");
        } else {
            if ("SEARCH".equals(currentNav)) {
                setActiveNav(previousNav != null && !"SEARCH".equals(previousNav) ? previousNav : "DASHBOARD");
            }
            loadCandidatesForSelectedElection();
        }
    }

    private void showNotificationsDialog() {
        String msg = """
                <html><body style='width: 360px; font-family: Segoe UI; font-size: 12px; color: #1A2352;'>
                <h3 style='color: #3C4DAE; margin-bottom: 8px;'>🔔 Election Notifications</h3>
                <b>• Active Session Closing:</b> Polls close today at 6:00 PM.<br><br>
                <b>• Secret Ballot Integrity:</b> Tallies remain encrypted and hidden while voting is active to prevent election bias and voter suppression.<br><br>
                <b>• Verified Registration:</b> Your account <b>%s</b> is verified and eligible to cast one ballot per active contest.<br><br>
                <b>• Integrity Ledger:</b> All submissions are cryptographically hashed using BCrypt.
                </body></html>
                """.formatted(currentUser.getUsername());

        JOptionPane.showMessageDialog(this, msg, "iVOTE Notifications", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelpDialog() {
        String msg = """
                <html><body style='width: 400px; font-family: Segoe UI; font-size: 12px; color: #1A2352;'>
                <h3 style='color: #3C4DAE; margin-bottom: 8px;'>ⓘ Voter Help & Integrity Guidelines</h3>
                <b>1. How do I cast a ballot?</b><br>
                Go to the <b>Vote</b> tab on the left sidebar, choose an active election from the dropdown, review the candidate profiles, and click <b>VOTE</b>.<br><br>
                <b>2. Why are live results hidden?</b><br>
                To uphold standard digital election integrity protocols (e.g. RankedVote standards), rolling tallies remain confidential until voting concludes. This prevents the Bandwagon Effect, voter apathy, and tactical voting.<br><br>
                <b>3. Can I change my vote?</b><br>
                No. Once submitted, ballots are irreversibly recorded to prevent tampering.
                </body></html>
                """;

        JOptionPane.showMessageDialog(this, msg, "iVOTE Help & FAQ", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==========================================
    // 2. SIDEBAR NAVIGATION
    // ==========================================
    private RoundedPanel createSidebar() {
        RoundedPanel panel = new RoundedPanel(24, Theme.CARD_BG, Theme.CARD_BORDER, true);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(210, Integer.MAX_VALUE));
        panel.setBorder(new EmptyBorder(24, 16, 24, 16));

        // Profile Section
        CircularAvatar avatar = new CircularAvatar(currentUser.getUsername(), 64);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(currentUser.getUsername());
        nameLabel.setFont(Theme.fontSubheading());
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel profileLink = new JLabel("View Profile >");
        profileLink.setFont(Theme.fontCaption());
        profileLink.setForeground(Theme.BRAND_ACCENT);
        profileLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profileLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveNav("SETTINGS");
            }
        });

        panel.add(avatar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(profileLink);
        panel.add(Box.createVerticalStrut(28));

        // Nav Links
        navDashboardBtn = createNavButton("Dashboard", PillButton.Style.GRADIENT, e -> setActiveNav("DASHBOARD"));
        navVoteBtn = createNavButton("Vote", PillButton.Style.SECONDARY, e -> setActiveNav("VOTE"));
        navGuidelinesBtn = createNavButton("Voters Guideline", PillButton.Style.SECONDARY, e -> setActiveNav("GUIDELINES"));
        navSettingsBtn = createNavButton("Settings", PillButton.Style.SECONDARY, e -> setActiveNav("SETTINGS"));

        panel.add(navDashboardBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(navVoteBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(navGuidelinesBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(navSettingsBtn);

        panel.add(Box.createVerticalGlue());

        // Logout Button
        navLogoutBtn = createNavButton("Log out", PillButton.Style.SECONDARY, e -> handleLogout());
        panel.add(navLogoutBtn);

        return panel;
    }

    private PillButton createNavButton(String text, PillButton.Style style, java.awt.event.ActionListener action) {
        PillButton btn = new PillButton(text, style);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(178, 40));
        btn.addActionListener(action);
        return btn;
    }

    private void setActiveNav(String viewName) {
        if (!"SEARCH".equals(viewName)) {
            this.previousNav = viewName;
        }
        this.currentNav = viewName;
        contentCardLayout.show(viewsContainer, viewName);

        navDashboardBtn.setStyle("DASHBOARD".equals(viewName) ? PillButton.Style.GRADIENT : PillButton.Style.SECONDARY);
        navVoteBtn.setStyle("VOTE".equals(viewName) ? PillButton.Style.GRADIENT : PillButton.Style.SECONDARY);
        navGuidelinesBtn.setStyle("GUIDELINES".equals(viewName) ? PillButton.Style.GRADIENT : PillButton.Style.SECONDARY);
        navSettingsBtn.setStyle("SETTINGS".equals(viewName) ? PillButton.Style.GRADIENT : PillButton.Style.SECONDARY);

        if (!"SEARCH".equals(viewName)) {
            refreshData();
        }
    }

    // ==========================================
    // VIEW 1: DASHBOARD
    // ==========================================
    private JPanel createDashboardView() {
        JPanel dash = new JPanel(new BorderLayout(20, 0));
        dash.setOpaque(false);

        // Center Column (Greeting + Ongoing Elections Hero + Election Results & Activities)
        JPanel centerCol = new JPanel();
        centerCol.setLayout(new BoxLayout(centerCol, BoxLayout.Y_AXIS));
        centerCol.setOpaque(false);

        // Greeting
        JLabel greeting = new JLabel("Hello, " + currentUser.getUsername() + "!");
        greeting.setFont(Theme.fontHero());
        greeting.setForeground(Theme.PRIMARY_NAVY);
        greeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcomeSub = new JLabel("Welcome to iVOTE Online Voting System");
        welcomeSub.setFont(Theme.fontBody());
        welcomeSub.setForeground(Theme.TEXT_SECONDARY);
        welcomeSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerCol.add(greeting);
        centerCol.add(Box.createVerticalStrut(4));
        centerCol.add(welcomeSub);
        centerCol.add(Box.createVerticalStrut(16));

        // 1. Ongoing Elections Hero Card
        centerCol.add(createOngoingElectionsHeroCard());
        centerCol.add(Box.createVerticalStrut(16));

        // 2. Election Results Card (With Secret Ballot Integrity) + Election Activities row
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        bottomRow.add(createResultsCard());
        bottomRow.add(createElectionActivitiesCard());

        centerCol.add(bottomRow);

        dash.add(centerCol, BorderLayout.CENTER);

        // Right Column (Calendar + Voting Process Stats)
        dash.add(createRightStatsRail(), BorderLayout.EAST);

        return dash;
    }

    private JPanel createOngoingElectionsHeroCard() {
        RoundedPanel hero = new RoundedPanel(22, Theme.CARD_BG, Theme.CARD_BORDER, true);
        hero.setLayout(new BorderLayout(20, 0));
        hero.setBorder(new EmptyBorder(20, 24, 20, 24));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        hero.setPreferredSize(new Dimension(560, 160));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel sectionTitle = new JLabel("Ongoing Elections");
        sectionTitle.setFont(Theme.fontCaption());
        sectionTitle.setForeground(Theme.TEXT_MUTED);

        ongoingElectionTitleLabel = new JLabel("President Student Council");
        ongoingElectionTitleLabel.setFont(Theme.fontTitle());
        ongoingElectionTitleLabel.setForeground(Theme.PRIMARY_NAVY);

        PillButton voteNowBtn = new PillButton("Vote now", PillButton.Style.OUTLINE);
        voteNowBtn.setPreferredSize(new Dimension(140, 36));
        voteNowBtn.setMaximumSize(new Dimension(140, 36));
        voteNowBtn.addActionListener(e -> setActiveNav("VOTE"));

        textPanel.add(sectionTitle);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(ongoingElectionTitleLabel);
        textPanel.add(Box.createVerticalStrut(16));
        textPanel.add(voteNowBtn);

        hero.add(textPanel, BorderLayout.CENTER);

        // Illustration
        VotingIllustrationPanel heroIllustration = new VotingIllustrationPanel();
        heroIllustration.setPreferredSize(new Dimension(140, 120));
        hero.add(heroIllustration, BorderLayout.EAST);

        return hero;
    }

    // ==========================================
    // ELECTION RESULTS & INTEGRITY CARD
    // ==========================================
    private JPanel createResultsCard() {
        RoundedPanel card = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Header with title and election navigator on clean separate rows
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("Election Results");
        title.setFont(Theme.fontSubheading());
        title.setForeground(Theme.PRIMARY_NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Election selector row with BorderLayout to guarantee title is centered and visible
        JPanel navRow = new JPanel(new BorderLayout(8, 0));
        navRow.setOpaque(false);
        navRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        navRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel prevArrow = new JLabel("< ");
        prevArrow.setFont(Theme.fontBodyBold());
        prevArrow.setForeground(Theme.BRAND_ACCENT);
        prevArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevArrow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cycleChartElection(-1);
            }
        });

        chartElectionTitleLabel = new JLabel("Select Election", SwingConstants.CENTER);
        chartElectionTitleLabel.setFont(Theme.fontBodyBold());
        chartElectionTitleLabel.setForeground(Theme.BRAND_ACCENT);

        JLabel nextArrow = new JLabel(" >");
        nextArrow.setFont(Theme.fontBodyBold());
        nextArrow.setForeground(Theme.BRAND_ACCENT);
        nextArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextArrow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cycleChartElection(1);
            }
        });

        navRow.add(prevArrow, BorderLayout.WEST);
        navRow.add(chartElectionTitleLabel, BorderLayout.CENTER);
        navRow.add(nextArrow, BorderLayout.EAST);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(navRow);

        card.add(header, BorderLayout.NORTH);

        // Dynamic results container: switches between Sealed/Integrity Notice and Chart
        resultsDisplayContainer = new JPanel(resultsCardLayout);
        resultsDisplayContainer.setOpaque(false);

        // Panel A: Ongoing Election (Hidden Tallies / Secret Ballot Protocol)
        JPanel sealedPanel = createSealedResultsPanel();

        // Panel B: Concluded Election (Final Certified Bar Chart)
        liveResultsChart = new HorizontalBarChart(null);

        resultsDisplayContainer.add(sealedPanel, "SEALED");
        resultsDisplayContainer.add(liveResultsChart, "CHART");

        card.add(resultsDisplayContainer, BorderLayout.CENTER);

        JLabel caption = new JLabel("*Results are certified and decrypted after active session closes (6:00 PM)");
        caption.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        caption.setForeground(Theme.TEXT_MUTED);
        card.add(caption, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createSealedResultsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 6, 8, 6));

        // Lock Badge
        JLabel lockBadge = new JLabel("🔒 Secret Ballot Active • Polls Open (6:00 PM)");
        lockBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lockBadge.setForeground(Theme.BRAND_ACCENT);
        lockBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Main Integrity Statement
        JLabel statement = new JLabel("<html><body style='width: 235px; color: #1A2352; font-size: 11px;'>" +
                "<b>Interim tallies are strictly confidential</b> while voting is ongoing.<br>" +
                "<p style='color: #7D87A6; margin-top: 4px;'>" +
                "Results remain sealed to prevent the <b>Bandwagon Effect</b>, <b>voter suppression</b>, and <b>tactical voting</b>. " +
                "Official certified tallies unlock once voting concludes." +
                "</p></body></html>");
        statement.setFont(Theme.fontCaption());
        statement.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Learn more link
        JLabel learnMoreLink = new JLabel("Why are live results hidden? ⓘ");
        learnMoreLink.setFont(new Font("Segoe UI", Font.BOLD, 10));
        learnMoreLink.setForeground(Theme.BRAND_ACCENT);
        learnMoreLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        learnMoreLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        learnMoreLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showIntegrityRationaleDialog();
            }
        });

        panel.add(lockBadge);
        panel.add(Box.createVerticalStrut(8));
        panel.add(statement);
        panel.add(Box.createVerticalStrut(10));
        panel.add(learnMoreLink);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void showIntegrityRationaleDialog() {
        String msg = """
                <html><body style='width: 440px; font-family: Segoe UI; font-size: 12px; color: #1A2352;'>
                <h3 style='color: #3C4DAE; margin-bottom: 8px;'>🛡️ Why Live Voting Results are Hidden</h3>
                Displaying rolling tallies before voting closes is classified as an election vulnerability because it distorts natural voter behavior:<br><br>
                <b>1. The Bandwagon Effect:</b> Seeing an early frontrunner encourages undecided voters to vote for them simply because they are leading.<br><br>
                <b>2. Voter Suppression & Apathy:</b> When voters see candidates trailing significantly, they often conclude their vote won't matter and abstain.<br><br>
                <b>3. Underdog Effect:</b> Artificially triggers sympathy voting, skewing genuine preference.<br><br>
                <b>4. Strategic & Tactical Voting:</b> Voters abandon their primary choice just to block a leading candidate they dislike.<br><br>
                <b>Platform Standard:</b> Industry benchmarks (e.g. RankedVote digital protocols) require tallies to remain encrypted until the contest creator concludes the election or polls close.
                </body></html>
                """;

        JOptionPane.showMessageDialog(this, msg, "Election Integrity Standard", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createElectionActivitiesCard() {
        RoundedPanel card = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Election Activities");
        title.setFont(Theme.fontSubheading());
        title.setForeground(Theme.PRIMARY_NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Legend row
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        legend.setOpaque(false);
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);
        legend.add(createLegendDot("● Ongoing", Theme.BRAND_ACCENT));
        legend.add(createLegendDot("● Pending", new Color(0x32, 0x38, 0x62)));
        legend.add(createLegendDot("● Concluded", new Color(0x8D, 0x96, 0xB5)));

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(legend);
        card.add(Box.createVerticalStrut(12));

        // List of elections
        List<Election> all = electionDAO.getAllElections();
        if (all.isEmpty()) {
            JLabel empty = new JLabel("No active election cycles registered.");
            empty.setFont(Theme.fontCaption());
            empty.setForeground(Theme.TEXT_MUTED);
            card.add(empty);
        } else {
            for (Election el : all) {
                JPanel item = new JPanel(new BorderLayout());
                item.setOpaque(false);
                item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

                JLabel name = new JLabel(el.getTitle());
                name.setFont(Theme.fontBody());
                name.setForeground(Theme.TEXT_PRIMARY);

                JLabel status = new JLabel(el.isActive() ? "Ongoing" : "Concluded");
                status.setFont(Theme.fontCaption());
                status.setForeground(el.isActive() ? Theme.BRAND_ACCENT : Theme.TEXT_MUTED);

                item.add(name, BorderLayout.WEST);
                item.add(status, BorderLayout.EAST);
                card.add(item);
                card.add(Box.createVerticalStrut(6));
            }
        }

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JLabel createLegendDot(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(color);
        return l;
    }

    // ==========================================
    // RIGHT COLUMN: CALENDAR + STATS
    // ==========================================
    private JPanel createRightStatsRail() {
        JPanel rail = new JPanel();
        rail.setLayout(new BoxLayout(rail, BoxLayout.Y_AXIS));
        rail.setOpaque(false);
        rail.setPreferredSize(new Dimension(280, Integer.MAX_VALUE));

        // 1. Calendar Widget
        RoundedPanel calCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        calCard.setLayout(new BoxLayout(calCard, BoxLayout.Y_AXIS));
        calCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        calCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel calTitle = new JLabel("Calendar");
        calTitle.setFont(Theme.fontSubheading());
        calTitle.setForeground(Theme.PRIMARY_NAVY);
        calTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tabs = new JLabel("Today    Next week    This Month");
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tabs.setForeground(Theme.TEXT_MUTED);
        tabs.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel eventChip = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyRenderHints(g2);
                g2.setColor(Theme.BRAND_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        eventChip.setOpaque(false);
        eventChip.setBorder(new EmptyBorder(8, 12, 8, 12));
        eventChip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel dateLabel = new JLabel("<html><b>Active Session</b><br><font size='2' color='#7D87A6'>Polls open until 6:00 PM</font></html>");
        dateLabel.setFont(Theme.fontCaption());
        eventChip.add(dateLabel, BorderLayout.CENTER);

        calCard.add(calTitle);
        calCard.add(Box.createVerticalStrut(6));
        calCard.add(tabs);
        calCard.add(Box.createVerticalStrut(12));
        calCard.add(eventChip);

        rail.add(calCard);
        rail.add(Box.createVerticalStrut(16));

        // 2. Voting Process Stats Card
        RoundedPanel statsCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        statsCard.setLayout(new BoxLayout(statsCard, BoxLayout.Y_AXIS));
        statsCard.setBorder(new EmptyBorder(20, 18, 20, 18));

        JLabel statsTitle = new JLabel("Voting Process");
        statsTitle.setFont(Theme.fontSubheading());
        statsTitle.setForeground(Theme.PRIMARY_NAVY);
        statsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsContainer = new JPanel();
        statsContainer.setLayout(new BoxLayout(statsContainer, BoxLayout.Y_AXIS));
        statsContainer.setOpaque(false);
        statsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsCard.add(statsTitle);
        statsCard.add(Box.createVerticalStrut(18));
        statsCard.add(statsContainer);
        statsCard.add(Box.createVerticalGlue());

        rail.add(statsCard);

        return rail;
    }

    // ==========================================
    // VIEW 2: VOTE ("NOW CAST YOUR VOTES!")
    // ==========================================
    private JPanel createVoteView() {
        JPanel votePanel = new JPanel(new BorderLayout(0, 14));
        votePanel.setOpaque(false);

        // Header Section
        RoundedPanel topBanner = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        topBanner.setLayout(new BorderLayout(20, 0));
        topBanner.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setOpaque(false);

        JLabel bannerTitle = new JLabel("NOW CAST YOUR VOTES!");
        bannerTitle.setFont(Theme.fontHero());
        bannerTitle.setForeground(Theme.PRIMARY_NAVY);

        JLabel bannerSub = new JLabel("You can only vote for one candidate per election cycle. Your ballot is securely hashed and verified.");
        bannerSub.setFont(Theme.fontBody());
        bannerSub.setForeground(Theme.TEXT_SECONDARY);

        bannerText.add(bannerTitle);
        bannerText.add(Box.createVerticalStrut(4));
        bannerText.add(bannerSub);

        // Election selector dropdown + Search Filter Indicator
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightControls.setOpaque(false);

        searchStatusLabel = new JLabel("");
        searchStatusLabel.setFont(Theme.fontCaption());
        searchStatusLabel.setForeground(Theme.BRAND_ACCENT);

        clearSearchBtn = new PillButton("✕ Clear", PillButton.Style.OUTLINE);
        clearSearchBtn.setPreferredSize(new Dimension(80, 30));
        clearSearchBtn.setVisible(false);
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            currentSearchQuery = "";
            clearSearchBtn.setVisible(false);
            searchStatusLabel.setText("");
            loadCandidatesForSelectedElection();
        });

        rightControls.add(searchStatusLabel);
        rightControls.add(clearSearchBtn);

        rightControls.add(new JLabel("Select Election:"));

        voteElectionCombo = new JComboBox<>();
        voteElectionCombo.setFont(Theme.fontBody());
        voteElectionCombo.setPreferredSize(new Dimension(240, 36));
        voteElectionCombo.addActionListener(e -> {
            if (currentSearchQuery.isEmpty()) {
                loadCandidatesForSelectedElection();
            } else {
                filterCandidates(currentSearchQuery);
            }
        });
        rightControls.add(voteElectionCombo);

        topBanner.add(bannerText, BorderLayout.WEST);
        topBanner.add(rightControls, BorderLayout.EAST);

        votePanel.add(topBanner, BorderLayout.NORTH);

        // Candidate Cards Scrollable Flow Grid
        candidatesGridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        candidatesGridPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(candidatesGridPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        votePanel.add(scrollPane, BorderLayout.CENTER);

        return votePanel;
    }

    private void filterCandidates(String query) {
        candidatesGridPanel.removeAll();
        Election selected = (Election) voteElectionCombo.getSelectedItem();

        if (selected == null) {
            JLabel empty = new JLabel("No active elections available.");
            empty.setFont(Theme.fontSubheading());
            empty.setForeground(Theme.TEXT_MUTED);
            candidatesGridPanel.add(empty);
            candidatesGridPanel.revalidate();
            candidatesGridPanel.repaint();
            return;
        }

        boolean hasVoted = voteDAO.hasVoted(selected.getId(), currentUser.getId());
        List<Candidate> allCandidates = electionDAO.getCandidatesForElection(selected.getId());

        String lowerQuery = query.toLowerCase();
        List<Candidate> filtered = allCandidates.stream()
                .filter(c -> c.getName().toLowerCase().contains(lowerQuery) ||
                             (c.getParty() != null && c.getParty().toLowerCase().contains(lowerQuery)))
                .toList();

        searchStatusLabel.setText("Found " + filtered.size() + " matching candidate(s)");
        clearSearchBtn.setVisible(true);

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("No candidates found matching \"" + query + "\" in " + selected.getTitle() + ".");
            empty.setFont(Theme.fontSubheading());
            empty.setForeground(Theme.TEXT_MUTED);
            candidatesGridPanel.add(empty);
        } else {
            for (Candidate c : filtered) {
                candidatesGridPanel.add(new CandidateCard(c, hasVoted, this::handleCastVote));
            }
        }

        candidatesGridPanel.revalidate();
        candidatesGridPanel.repaint();
    }

    private void loadCandidatesForSelectedElection() {
        searchStatusLabel.setText("");
        clearSearchBtn.setVisible(false);
        candidatesGridPanel.removeAll();
        Election selected = (Election) voteElectionCombo.getSelectedItem();

        if (selected == null) {
            JLabel empty = new JLabel("No active elections available for voting at this time.");
            empty.setFont(Theme.fontSubheading());
            empty.setForeground(Theme.TEXT_MUTED);
            candidatesGridPanel.add(empty);
        } else {
            boolean hasVoted = voteDAO.hasVoted(selected.getId(), currentUser.getId());
            List<Candidate> candidates = electionDAO.getCandidatesForElection(selected.getId());

            if (candidates.isEmpty()) {
                JLabel empty = new JLabel("No candidates registered yet for " + selected.getTitle() + ".");
                empty.setFont(Theme.fontSubheading());
                empty.setForeground(Theme.TEXT_MUTED);
                candidatesGridPanel.add(empty);
            } else {
                for (Candidate c : candidates) {
                    candidatesGridPanel.add(new CandidateCard(c, hasVoted, this::handleCastVote));
                }
            }
        }

        candidatesGridPanel.revalidate();
        candidatesGridPanel.repaint();
    }

    private void handleCastVote(Candidate candidate) {
        Election selected = (Election) voteElectionCombo.getSelectedItem();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cast your vote for:\n\n" +
                "Candidate: " + candidate.getName() + " (" + candidate.getParty() + ")\n" +
                "Election: " + selected.getTitle() + "\n\n" +
                "Note: Votes cannot be changed once submitted.",
                "Confirm Ballot Submission",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (voteDAO.castVote(selected.getId(), currentUser.getId(), candidate.getId())) {
                JOptionPane.showMessageDialog(this,
                        "Vote successfully registered and recorded in the secure ledger!",
                        "Ballot Cast Verified",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshData();
                loadCandidatesForSelectedElection();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Vote submission failed. You may have already voted in this election.",
                        "Submission Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // VIEW 3: GUIDELINES
    // ==========================================
    private JPanel createGuidelinesView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        RoundedPanel card = new RoundedPanel(22, Theme.CARD_BG, Theme.CARD_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Voters Guideline & Code of Conduct");
        title.setFont(Theme.fontHero());
        title.setForeground(Theme.PRIMARY_NAVY);

        JLabel content = new JLabel("<html><body style='width: 680px; font-size: 12px; color: #1A2352;'>" +
                "<p style='color: #7D87A6;'>Please read the guidelines carefully before submitting your ballot.</p><br>" +
                "<b>1. One Ballot Per Voter</b><br>" +
                "Each voter is allocated exactly one cryptographically verifiable vote per active election cycle. Duplicate submissions are strictly prevented by the integrity ledger.<br><br>" +
                "<b>2. Ballot Secrecy & Verification</b><br>" +
                "Your ballot choice is decoupled from your profile identity to maintain voter anonymity while ensuring mathematical audibility.<br><br>" +
                "<b>3. Election Schedule & Deadlines</b><br>" +
                "Polls remain active until the published closing timestamp (6:00 PM). Interim tallies remain sealed to prevent cognitive bias and ensure fair outcomes.<br><br>" +
                "<b>4. Integrity Protection</b><br>" +
                "All user authentication is protected by BCrypt salt rounds, and election records are stored in ACID-compliant transactional tables." +
                "</body></html>");
        content.setFont(Theme.fontBody());

        card.add(title);
        card.add(Box.createVerticalStrut(16));
        card.add(content);
        card.add(Box.createVerticalGlue());

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // VIEW 4: SETTINGS
    // ==========================================
    private JPanel createSettingsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        RoundedPanel card = new RoundedPanel(22, Theme.CARD_BG, Theme.CARD_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Voter Account Profile");
        title.setFont(Theme.fontHero());
        title.setForeground(Theme.PRIMARY_NAVY);

        JLabel userDetails = new JLabel("<html><body style='width: 500px; font-size: 13px; color: #1A2352;'>" +
                "<b>Username:</b> " + currentUser.getUsername() + "<br><br>" +
                "<b>Role:</b> " + currentUser.getRole() + "<br><br>" +
                "<b>Security Status:</b> Verified & Active<br><br>" +
                "<b>Database Storage:</b> SQLite ACID Local Instance<br><br>" +
                "<b>Encryption:</b> BCrypt Hashed Passwords<br><br>" +
                "<b>Ballot Privacy Status:</b> Sealed & Confidential" +
                "</body></html>");

        PillButton backToDashBtn = new PillButton("Back to Dashboard", PillButton.Style.GRADIENT);
        backToDashBtn.setPreferredSize(new Dimension(180, 40));
        backToDashBtn.setMaximumSize(new Dimension(180, 40));
        backToDashBtn.addActionListener(e -> setActiveNav("DASHBOARD"));

        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(userDetails);
        card.add(Box.createVerticalStrut(30));
        card.add(backToDashBtn);
        card.add(Box.createVerticalGlue());

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // VIEW 5: GLOBAL SEARCH
    // ==========================================
    private JPanel createGlobalSearchView() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 24, 20, 24));

        // Top Search Header Card
        RoundedPanel headerCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        headerCard.setLayout(new BorderLayout(16, 0));
        headerCard.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel title = new JLabel("Search Results");
        title.setFont(Theme.fontHero());
        title.setForeground(Theme.PRIMARY_NAVY);

        globalSearchCountLabel = new JLabel("Search across elections, candidates, and parties in real time.");
        globalSearchCountLabel.setFont(Theme.fontBody());
        globalSearchCountLabel.setForeground(Theme.TEXT_SECONDARY);

        headerText.add(title);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(globalSearchCountLabel);

        PillButton clearBtn = new PillButton("✕ Clear Search", PillButton.Style.OUTLINE);
        clearBtn.setPreferredSize(new Dimension(130, 36));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            setActiveNav(previousNav != null && !"SEARCH".equals(previousNav) ? previousNav : "DASHBOARD");
        });

        headerCard.add(headerText, BorderLayout.CENTER);
        headerCard.add(clearBtn, BorderLayout.EAST);

        panel.add(headerCard, BorderLayout.NORTH);

        // Scrollable Results Panel
        globalSearchResultsContainer = new JPanel();
        globalSearchResultsContainer.setLayout(new BoxLayout(globalSearchResultsContainer, BoxLayout.Y_AXIS));
        globalSearchResultsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(globalSearchResultsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void performGlobalSearch(String query) {
        if (globalSearchResultsContainer == null) return;
        globalSearchResultsContainer.removeAll();

        String q = query.toLowerCase().trim();
        List<Election> allElections = electionDAO.getAllElections();
        List<Election> matchedElections = allElections.stream()
                .filter(el -> el.getTitle().toLowerCase().contains(q))
                .toList();

        class Match {
            final Candidate candidate;
            final Election election;
            Match(Candidate c, Election el) { this.candidate = c; this.election = el; }
        }
        List<Match> matchedCandidates = new ArrayList<>();

        for (Election el : allElections) {
            List<Candidate> candidates = electionDAO.getCandidatesForElection(el.getId());
            for (Candidate c : candidates) {
                boolean nameMatch = c.getName().toLowerCase().contains(q);
                boolean partyMatch = c.getParty() != null && c.getParty().toLowerCase().contains(q);
                boolean electionMatch = el.getTitle().toLowerCase().contains(q);
                if (nameMatch || partyMatch || electionMatch) {
                    matchedCandidates.add(new Match(c, el));
                }
            }
        }

        if (globalSearchCountLabel != null) {
            globalSearchCountLabel.setText("Found " + matchedElections.size() + " election(s) and " + matchedCandidates.size() + " candidate(s) matching \"" + query + "\"");
        }

        if (matchedElections.isEmpty() && matchedCandidates.isEmpty()) {
            RoundedPanel emptyCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
            emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
            emptyCard.setBorder(new EmptyBorder(32, 24, 32, 24));
            emptyCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            emptyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

            JLabel emptyTitle = new JLabel("No matches found for \"" + query + "\"");
            emptyTitle.setFont(Theme.fontSubheading());
            emptyTitle.setForeground(Theme.PRIMARY_NAVY);
            emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptySub = new JLabel("Try searching by candidate full name, political party, or election title.");
            emptySub.setFont(Theme.fontBody());
            emptySub.setForeground(Theme.TEXT_MUTED);
            emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

            PillButton clearBtn = new PillButton("Clear Search", PillButton.Style.PRIMARY);
            clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            clearBtn.setPreferredSize(new Dimension(140, 36));
            clearBtn.setMaximumSize(new Dimension(140, 36));
            clearBtn.addActionListener(e -> {
                searchField.setText("");
                setActiveNav(previousNav != null && !"SEARCH".equals(previousNav) ? previousNav : "DASHBOARD");
            });

            emptyCard.add(emptyTitle);
            emptyCard.add(Box.createVerticalStrut(8));
            emptyCard.add(emptySub);
            emptyCard.add(Box.createVerticalStrut(18));
            emptyCard.add(clearBtn);

            globalSearchResultsContainer.add(emptyCard);
        } else {
            // 1. Matching Elections
            if (!matchedElections.isEmpty()) {
                JLabel secElections = new JLabel("Matching Elections (" + matchedElections.size() + ")");
                secElections.setFont(Theme.fontSubheading());
                secElections.setForeground(Theme.PRIMARY_NAVY);
                secElections.setAlignmentX(Component.LEFT_ALIGNMENT);
                globalSearchResultsContainer.add(secElections);
                globalSearchResultsContainer.add(Box.createVerticalStrut(10));

                for (Election el : matchedElections) {
                    RoundedPanel elCard = new RoundedPanel(16, Theme.CARD_BG, Theme.CARD_BORDER, true);
                    elCard.setLayout(new BorderLayout(16, 0));
                    elCard.setBorder(new EmptyBorder(14, 18, 14, 18));
                    elCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
                    elCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
                    info.setOpaque(false);
                    JLabel elTitle = new JLabel(el.getTitle());
                    elTitle.setFont(Theme.fontBodyBold());
                    elTitle.setForeground(Theme.TEXT_PRIMARY);

                    JLabel statusPill = new JLabel(el.isActive() ? "● Ongoing" : "● Concluded");
                    statusPill.setFont(Theme.fontCaption());
                    statusPill.setForeground(el.isActive() ? Theme.SUCCESS_GREEN : Theme.TEXT_MUTED);

                    List<Candidate> cList = electionDAO.getCandidatesForElection(el.getId());
                    JLabel countLbl = new JLabel("(" + cList.size() + " candidates)");
                    countLbl.setFont(Theme.fontCaption());
                    countLbl.setForeground(Theme.TEXT_MUTED);

                    info.add(elTitle);
                    info.add(statusPill);
                    info.add(countLbl);

                    elCard.add(info, BorderLayout.WEST);

                    if (el.isActive()) {
                        PillButton openBtn = new PillButton("Vote in this Election", PillButton.Style.GRADIENT);
                        openBtn.setPreferredSize(new Dimension(170, 34));
                        openBtn.addActionListener(e -> {
                            searchField.setText("");
                            voteElectionCombo.setSelectedItem(el);
                            setActiveNav("VOTE");
                        });
                        elCard.add(openBtn, BorderLayout.EAST);
                    } else {
                        JLabel closedLbl = new JLabel("Polls Closed");
                        closedLbl.setFont(Theme.fontCaption());
                        closedLbl.setForeground(Theme.TEXT_MUTED);
                        elCard.add(closedLbl, BorderLayout.EAST);
                    }

                    globalSearchResultsContainer.add(elCard);
                    globalSearchResultsContainer.add(Box.createVerticalStrut(8));
                }
                globalSearchResultsContainer.add(Box.createVerticalStrut(16));
            }

            // 2. Matching Candidates
            if (!matchedCandidates.isEmpty()) {
                JLabel secCandidates = new JLabel("Matching Candidates (" + matchedCandidates.size() + ")");
                secCandidates.setFont(Theme.fontSubheading());
                secCandidates.setForeground(Theme.PRIMARY_NAVY);
                secCandidates.setAlignmentX(Component.LEFT_ALIGNMENT);
                globalSearchResultsContainer.add(secCandidates);
                globalSearchResultsContainer.add(Box.createVerticalStrut(10));

                JPanel cardsFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
                cardsFlow.setOpaque(false);
                cardsFlow.setAlignmentX(Component.LEFT_ALIGNMENT);

                for (Match match : matchedCandidates) {
                    boolean hasVoted = voteDAO.hasVoted(match.election.getId(), currentUser.getId());
                    cardsFlow.add(new CandidateCard(match.candidate, match.election.getTitle(), hasVoted, c -> {
                        handleCastVote(c);
                        performGlobalSearch(currentSearchQuery);
                    }));
                }
                globalSearchResultsContainer.add(cardsFlow);
            }
        }

        globalSearchResultsContainer.revalidate();
        globalSearchResultsContainer.repaint();
    }

    // ==========================================
    // DATA BINDING & REFRESH
    // ==========================================
    private void loadInitialData() {
        allElectionsList = electionDAO.getAllElections();
        List<Election> activeList = electionDAO.getActiveElections();

        voteElectionCombo.removeAllItems();
        for (Election el : activeList) {
            voteElectionCombo.addItem(el);
        }

        refreshData();
        loadCandidatesForSelectedElection();
    }

    private void refreshData() {
        allElectionsList = electionDAO.getAllElections();
        List<Election> activeList = electionDAO.getActiveElections();

        // 1. Hero ongoing election card
        if (!activeList.isEmpty()) {
            ongoingElectionTitleLabel.setText(activeList.get(0).getTitle());
        } else {
            ongoingElectionTitleLabel.setText("No Active Elections");
        }

        // 2. Refresh Election Results display (Sealed during voting, or Final Chart if concluded)
        updateChartElectionDisplay();

        // 3. Refresh Stats Rail
        statsContainer.removeAll();
        int votersCount = userDAO.getTotalVotersCount();
        int votesCount = voteDAO.getTotalVotesCount();
        int candidatesCount = electionDAO.getTotalCandidatesCount();

        statsContainer.add(new StatMetricCard(votersCount, "Total number of registered voters"));
        statsContainer.add(Box.createVerticalStrut(14));
        statsContainer.add(new StatMetricCard(votesCount, "Total number of votes"));
        statsContainer.add(Box.createVerticalStrut(14));
        statsContainer.add(new StatMetricCard(candidatesCount, "Total number of registered candidates"));

        statsContainer.revalidate();
        statsContainer.repaint();
    }

    private void cycleChartElection(int delta) {
        if (allElectionsList.isEmpty()) return;
        currentChartElectionIndex += delta;
        if (currentChartElectionIndex < 0) {
            currentChartElectionIndex = allElectionsList.size() - 1;
        } else if (currentChartElectionIndex >= allElectionsList.size()) {
            currentChartElectionIndex = 0;
        }
        updateChartElectionDisplay();
    }

    private void updateChartElectionDisplay() {
        if (!allElectionsList.isEmpty() && currentChartElectionIndex < allElectionsList.size()) {
            Election el = allElectionsList.get(currentChartElectionIndex);
            chartElectionTitleLabel.setText(el.getTitle());

            // ELECTION INTEGRITY RULE:
            // If active, keep tallies confidential to prevent voter bias and tactical voting.
            // If concluded, show final official certified bar chart.
            if (el.isActive()) {
                resultsCardLayout.show(resultsDisplayContainer, "SEALED");
            } else {
                Map<String, Integer> results = voteDAO.getResults(el.getId());
                liveResultsChart.setData(results);
                resultsCardLayout.show(resultsDisplayContainer, "CHART");
            }
        } else {
            chartElectionTitleLabel.setText("No Elections");
            resultsCardLayout.show(resultsDisplayContainer, "SEALED");
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}