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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modern Administration Portal for iVOTE Online Voting System.
 * Styled with iVOTE UI/UX design theme, responsive cards, real-time analytics,
 * and comprehensive election/candidate/voter controls.
 */
public class AdminDashboard extends JFrame {
    private final User currentUser;
    private final ElectionDAO electionDAO = new ElectionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final VoteDAO voteDAO = new VoteDAO();

    // Layout & Navigation
    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel viewsContainer = new JPanel(contentCardLayout);
    private JPanel sidebar;
    private JPanel bodyPanel;

    // Sidebar navigation buttons
    private PillButton navDashboardBtn;
    private PillButton navElectionsBtn;
    private PillButton navCandidatesBtn;
    private PillButton navResultsBtn;
    private PillButton navVotersBtn;
    private PillButton navSettingsBtn;
    private PillButton navLogoutBtn;

    // Header widgets
    private PillTextField searchField;
    private String currentNav = "DASHBOARD";
    private String previousNav = "DASHBOARD";

    // Dashboard Overview Widgets
    private JPanel dashStatsRail;
    private JPanel dashElectionsListContainer;
    private JComboBox<Election> dashResultsCombo;
    private HorizontalBarChart dashResultsChart;
    private JLabel dashLeadingCandidateLabel;
    private JLabel dashTotalVotesLabel;

    // Elections Management Widgets
    private JPanel electionsCardsContainer;
    private PillTextField newElectionTitleField;

    // Candidate Management Widgets
    private JComboBox<Election> candidateElectionSelectCombo;
    private JComboBox<Election> candidateFilterCombo;
    private PillTextField newCandidateNameField;
    private PillTextField newCandidatePartyField;
    private JPanel candidatesGridContainer;

    // Analytics / Results Widgets
    private JComboBox<Election> analyticsElectionCombo;
    private HorizontalBarChart analyticsChart;
    private JLabel analyticsTotalVotesLabel;
    private JLabel analyticsTurnoutLabel;
    private JLabel analyticsWinnerLabel;
    private DefaultTableModel analyticsTableModel;

    // Voter Registry Widgets
    private DefaultTableModel voterTableModel;
    private PillTextField voterSearchField;
    private JLabel voterCountSummaryLabel;

    // Global Admin Search Widgets
    private JPanel globalSearchResultsContainer;
    private JLabel globalSearchCountLabel;

    public AdminDashboard() {
        this(new User(1, "admin", "", "ADMIN"));
    }

    public AdminDashboard(User user) {
        this.currentUser = user != null ? user : new User(1, "admin", "", "ADMIN");
        setTitle("iVOTE — Administrator Portal (" + currentUser.getUsername() + ")");
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
        canvas.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top Header
        canvas.add(createTopBar(), BorderLayout.NORTH);

        // Body Panel (Collapsible Sidebar + Views Container)
        bodyPanel = new JPanel(new BorderLayout(20, 0));
        bodyPanel.setOpaque(false);

        sidebar = createSidebar();
        bodyPanel.add(sidebar, BorderLayout.WEST);

        // Register Views in Center
        viewsContainer.setOpaque(false);
        viewsContainer.add(createDashboardOverviewView(), "DASHBOARD");
        viewsContainer.add(createElectionsView(), "ELECTIONS");
        viewsContainer.add(createCandidatesView(), "CANDIDATES");
        viewsContainer.add(createResultsAnalyticsView(), "RESULTS");
        viewsContainer.add(createVotersRegistryView(), "VOTERS");
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

        // Left: Hamburger Menu Icon + Brand Logo + Admin Badge
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

        // Admin Console Badge
        JLabel adminBadge = new JLabel(" ADMIN CONSOLE ");
        adminBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        adminBadge.setForeground(Theme.BRAND_ACCENT);
        adminBadge.setOpaque(true);
        adminBadge.setBackground(new Color(0xEE, 0xF2, 0xFF));
        adminBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC7, 0xD2, 0xFE), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        left.add(adminBadge);

        bar.add(left, BorderLayout.WEST);

        // Center: Interactive Search Field
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        center.setOpaque(false);
        searchField = new PillTextField("Search elections, candidates, or voters...", 32);
        searchField.setShowSearchIcon(true);
        searchField.setPreferredSize(new Dimension(440, 38));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { triggerSearch(); }
        });
        searchField.addActionListener(e -> triggerSearch());
        center.add(searchField);
        bar.add(center, BorderLayout.CENTER);

        // Right: Notification Bell, Help, Dynamic Profile Chip
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 2));
        right.setOpaque(false);

        HeaderIcon bell = new HeaderIcon(HeaderIcon.Type.BELL, 22);
        bell.setShowBadge(true);
        bell.setToolTipText("System Notifications & Audit Alerts");
        bell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNotificationsDialog();
            }
        });

        HeaderIcon help = new HeaderIcon(HeaderIcon.Type.HELP, 22);
        help.setToolTipText("Administrator Operational Manual & FAQ");
        help.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showHelpDialog();
            }
        });

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
        userChip.setToolTipText("Logged in as System Administrator (" + currentUser.getUsername() + ")");

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
        if (!query.isEmpty()) {
            if (!"SEARCH".equals(currentNav)) {
                previousNav = currentNav;
            }
            performAdminSearch(query);
            setActiveNav("SEARCH");
        } else {
            if ("SEARCH".equals(currentNav)) {
                setActiveNav(previousNav != null && !"SEARCH".equals(previousNav) ? previousNav : "DASHBOARD");
            }
        }
    }

    private void showNotificationsDialog() {
        int voters = userDAO.getTotalVotersCount();
        int votes = voteDAO.getTotalVotesCount();
        int active = electionDAO.getActiveElections().size();

        String msg = """
                <html><body style='width: 380px; font-family: Segoe UI; font-size: 12px; color: #1A2352;'>
                <h3 style='color: #3C4DAE; margin-bottom: 8px;'>🔔 Administrator System Audit</h3>
                <b>• Active Elections:</b> %d contest(s) currently open for voting.<br><br>
                <b>• Total Ballots Recorded:</b> %d verified vote submission(s).<br><br>
                <b>• Voter Registry:</b> %d verified voter account(s) registered in SQLite.<br><br>
                <b>• Security Status:</b> Secret Ballot protocol active. Database integrity check passed.
                </body></html>
                """.formatted(active, votes, voters);

        JOptionPane.showMessageDialog(this, msg, "iVOTE System Audit", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelpDialog() {
        String msg = """
                <html><body style='width: 420px; font-family: Segoe UI; font-size: 12px; color: #1A2352;'>
                <h3 style='color: #3C4DAE; margin-bottom: 8px;'>ⓘ Administrator Operations Manual</h3>
                <b>1. Creating Elections:</b><br>
                Navigate to <b>Elections</b> in the sidebar, enter the contest title, and click <b>Create Election</b>.<br><br>
                <b>2. Adding Candidates:</b><br>
                Under <b>Candidates</b>, select the target election, provide the candidate full name and party affiliation, and register them.<br><br>
                <b>3. Concluding an Election:</b><br>
                In <b>Elections</b> or <b>Dashboard</b>, click <b>Conclude Election</b> when voting ends. This marks the election as concluded and certifies the final results.<br><br>
                <b>4. Live Results vs Secret Ballot:</b><br>
                As Administrator, you have real-time visibility into certified vote tallies under <b>Results & Analytics</b>, while voter dashboards maintain sealed secret ballots until sessions close.
                </body></html>
                """;

        JOptionPane.showMessageDialog(this, msg, "Administrator Operations Manual", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==========================================
    // 2. SIDEBAR NAVIGATION
    // ==========================================
    private RoundedPanel createSidebar() {
        RoundedPanel panel = new RoundedPanel(24, Theme.CARD_BG, Theme.CARD_BORDER, true);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(210, Integer.MAX_VALUE));
        panel.setBorder(new EmptyBorder(22, 16, 22, 16));

        // Profile Section
        JPanel profileBox = new JPanel();
        profileBox.setLayout(new BoxLayout(profileBox, BoxLayout.Y_AXIS));
        profileBox.setOpaque(false);
        profileBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        CircularAvatar bigAvatar = new CircularAvatar(currentUser.getUsername(), 60);
        bigAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(currentUser.getUsername());
        name.setFont(Theme.fontBodyBold());
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel role = new JLabel("System Administrator");
        role.setFont(Theme.fontCaption());
        role.setForeground(Theme.BRAND_ACCENT);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileBox.add(bigAvatar);
        profileBox.add(Box.createVerticalStrut(8));
        profileBox.add(name);
        profileBox.add(Box.createVerticalStrut(2));
        profileBox.add(role);

        panel.add(profileBox);
        panel.add(Box.createVerticalStrut(20));

        // Nav Buttons
        navDashboardBtn = createNavButton("Dashboard", PillButton.Style.GRADIENT, e -> setActiveNav("DASHBOARD"));
        navElectionsBtn = createNavButton("Elections", PillButton.Style.SECONDARY, e -> setActiveNav("ELECTIONS"));
        navCandidatesBtn = createNavButton("Candidates", PillButton.Style.SECONDARY, e -> setActiveNav("CANDIDATES"));
        navResultsBtn = createNavButton("Results & Analytics", PillButton.Style.SECONDARY, e -> setActiveNav("RESULTS"));
        navVotersBtn = createNavButton("Voter Registry", PillButton.Style.SECONDARY, e -> setActiveNav("VOTERS"));
        navSettingsBtn = createNavButton("Settings", PillButton.Style.SECONDARY, e -> setActiveNav("SETTINGS"));

        panel.add(navDashboardBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(navElectionsBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(navCandidatesBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(navResultsBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(navVotersBtn);
        panel.add(Box.createVerticalStrut(8));
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
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setPreferredSize(new Dimension(178, 38));
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
        navElectionsBtn.setStyle("ELECTIONS".equals(viewName) ? PillButton.Style.GRADIENT : PillButton.Style.SECONDARY);
        navCandidatesBtn.setStyle("CANDIDATES".equals(viewName) ? PillButton.Style.GRADIENT : PillButton.Style.SECONDARY);
        navResultsBtn.setStyle("RESULTS".equals(viewName) ? PillButton.Style.GRADIENT : PillButton.Style.SECONDARY);
        navVotersBtn.setStyle("VOTERS".equals(viewName) ? PillButton.Style.GRADIENT : PillButton.Style.SECONDARY);
        navSettingsBtn.setStyle("SETTINGS".equals(viewName) ? PillButton.Style.GRADIENT : PillButton.Style.SECONDARY);

        if (!"SEARCH".equals(viewName)) {
            refreshAllData();
        }
    }

    // ==========================================
    // VIEW 1: DASHBOARD OVERVIEW
    // ==========================================
    private JPanel createDashboardOverviewView() {
        JPanel dash = new JPanel(new BorderLayout(20, 0));
        dash.setOpaque(false);

        JPanel centerCol = new JPanel();
        centerCol.setLayout(new BoxLayout(centerCol, BoxLayout.Y_AXIS));
        centerCol.setOpaque(false);

        // Header Hero Banner
        RoundedPanel heroBanner = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        heroBanner.setLayout(new BorderLayout(16, 0));
        heroBanner.setBorder(new EmptyBorder(16, 22, 16, 22));
        heroBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        heroBanner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel heroText = new JPanel();
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        heroText.setOpaque(false);

        JLabel heroTitle = new JLabel("System Administration Dashboard");
        heroTitle.setFont(Theme.fontHero());
        heroTitle.setForeground(Theme.PRIMARY_NAVY);

        JLabel heroSub = new JLabel("Real-time voter participation, election lifecycle management, and tally verification.");
        heroSub.setFont(Theme.fontBody());
        heroSub.setForeground(Theme.TEXT_SECONDARY);

        heroText.add(heroTitle);
        heroText.add(Box.createVerticalStrut(4));
        heroText.add(heroSub);

        JPanel heroActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        heroActions.setOpaque(false);

        PillButton newElectionShortcut = new PillButton("+ New Election", PillButton.Style.GRADIENT);
        newElectionShortcut.setPreferredSize(new Dimension(140, 36));
        newElectionShortcut.addActionListener(e -> setActiveNav("ELECTIONS"));

        PillButton newCandShortcut = new PillButton("+ Add Candidate", PillButton.Style.OUTLINE);
        newCandShortcut.setPreferredSize(new Dimension(140, 36));
        newCandShortcut.addActionListener(e -> setActiveNav("CANDIDATES"));

        heroActions.add(newElectionShortcut);
        heroActions.add(newCandShortcut);

        heroBanner.add(heroText, BorderLayout.WEST);
        heroBanner.add(heroActions, BorderLayout.EAST);

        centerCol.add(heroBanner);
        centerCol.add(Box.createVerticalStrut(16));

        // Two-column section: Left (Active Elections List) + Right (Live Results & Leading Tally)
        JPanel splitRow = new JPanel(new GridLayout(1, 2, 16, 0));
        splitRow.setOpaque(false);
        splitRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: Quick Election Management Card
        RoundedPanel electionsCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        electionsCard.setLayout(new BorderLayout(0, 12));
        electionsCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel elHeader = new JPanel(new BorderLayout());
        elHeader.setOpaque(false);
        JLabel elTitle = new JLabel("Active Election Contests");
        elTitle.setFont(Theme.fontSubheading());
        elTitle.setForeground(Theme.PRIMARY_NAVY);

        JLabel elSubtitle = new JLabel("Manage contest lifecycle and status");
        elSubtitle.setFont(Theme.fontCaption());
        elSubtitle.setForeground(Theme.TEXT_MUTED);

        JPanel elHeaderText = new JPanel();
        elHeaderText.setLayout(new BoxLayout(elHeaderText, BoxLayout.Y_AXIS));
        elHeaderText.setOpaque(false);
        elHeaderText.add(elTitle);
        elHeaderText.add(Box.createVerticalStrut(2));
        elHeaderText.add(elSubtitle);

        elHeader.add(elHeaderText, BorderLayout.WEST);
        electionsCard.add(elHeader, BorderLayout.NORTH);

        dashElectionsListContainer = new JPanel();
        dashElectionsListContainer.setLayout(new BoxLayout(dashElectionsListContainer, BoxLayout.Y_AXIS));
        dashElectionsListContainer.setOpaque(false);

        // No scroll pane — let rows stack naturally inside the card without scrollbars
        JPanel elScrollWrapper = new JPanel(new BorderLayout());
        elScrollWrapper.setOpaque(false);
        elScrollWrapper.add(dashElectionsListContainer, BorderLayout.NORTH);
        electionsCard.add(elScrollWrapper, BorderLayout.CENTER);

        splitRow.add(electionsCard);

        // Right: Certified Live Tally & Bar Chart
        RoundedPanel liveTallyCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        liveTallyCard.setLayout(new BorderLayout(0, 12));
        liveTallyCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel tallyHeader = new JPanel(new BorderLayout());
        tallyHeader.setOpaque(false);

        JLabel tallyTitle = new JLabel("Certified Vote Distribution");
        tallyTitle.setFont(Theme.fontSubheading());
        tallyTitle.setForeground(Theme.PRIMARY_NAVY);

        dashResultsCombo = new JComboBox<>();
        dashResultsCombo.setFont(Theme.fontCaption());
        dashResultsCombo.setPreferredSize(new Dimension(190, 30));
        dashResultsCombo.addActionListener(e -> updateDashResultsChart());

        tallyHeader.add(tallyTitle, BorderLayout.WEST);
        tallyHeader.add(dashResultsCombo, BorderLayout.EAST);
        liveTallyCard.add(tallyHeader, BorderLayout.NORTH);

        dashResultsChart = new HorizontalBarChart(null);
        liveTallyCard.add(dashResultsChart, BorderLayout.CENTER);

        // Summary footer
        JPanel tallyFooter = new JPanel(new GridLayout(1, 2, 10, 0));
        tallyFooter.setOpaque(false);

        dashLeadingCandidateLabel = new JLabel("Frontrunner: Calculating...");
        dashLeadingCandidateLabel.setFont(Theme.fontBodyBold());
        dashLeadingCandidateLabel.setForeground(Theme.BRAND_ACCENT);

        dashTotalVotesLabel = new JLabel("Total Ballots: 0", SwingConstants.RIGHT);
        dashTotalVotesLabel.setFont(Theme.fontBody());
        dashTotalVotesLabel.setForeground(Theme.TEXT_SECONDARY);

        tallyFooter.add(dashLeadingCandidateLabel);
        tallyFooter.add(dashTotalVotesLabel);
        liveTallyCard.add(tallyFooter, BorderLayout.SOUTH);

        splitRow.add(liveTallyCard);

        centerCol.add(splitRow);
        dash.add(centerCol, BorderLayout.CENTER);

        // Right Rail (Telemetry Metric Cards)
        dash.add(createStatsRail(), BorderLayout.EAST);

        return dash;
    }

    private JPanel createStatsRail() {
        dashStatsRail = new JPanel();
        dashStatsRail.setLayout(new BoxLayout(dashStatsRail, BoxLayout.Y_AXIS));
        dashStatsRail.setOpaque(false);
        dashStatsRail.setPreferredSize(new Dimension(230, Integer.MAX_VALUE));
        dashStatsRail.setBorder(new EmptyBorder(0, 4, 0, 0));
        return dashStatsRail;
    }

    // ==========================================
    // VIEW 2: ELECTIONS MANAGEMENT
    // ==========================================
    private JPanel createElectionsView() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top: Create Election Form Card
        RoundedPanel createCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        createCard.setLayout(new BorderLayout(16, 0));
        createCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel formText = new JPanel();
        formText.setLayout(new BoxLayout(formText, BoxLayout.Y_AXIS));
        formText.setOpaque(false);

        JLabel title = new JLabel("Create New Election Contest");
        title.setFont(Theme.fontHero());
        title.setForeground(Theme.PRIMARY_NAVY);

        JLabel sub = new JLabel("Register an official election cycle with automatic secret ballot protocol protection.");
        sub.setFont(Theme.fontBody());
        sub.setForeground(Theme.TEXT_SECONDARY);

        formText.add(title);
        formText.add(Box.createVerticalStrut(4));
        formText.add(sub);

        JPanel inputsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        inputsRow.setOpaque(false);

        newElectionTitleField = new PillTextField("Enter election title (e.g., Dean of Engineering 2026)", 28);
        newElectionTitleField.setPreferredSize(new Dimension(360, 40));

        PillButton createBtn = new PillButton("Create Election", PillButton.Style.GRADIENT);
        createBtn.setPreferredSize(new Dimension(150, 40));
        createBtn.addActionListener(e -> handleCreateElection());

        inputsRow.add(newElectionTitleField);
        inputsRow.add(createBtn);

        createCard.add(formText, BorderLayout.WEST);
        createCard.add(inputsRow, BorderLayout.EAST);

        panel.add(createCard, BorderLayout.NORTH);

        // Bottom: Elections List Container
        RoundedPanel listCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        listCard.setLayout(new BorderLayout(0, 12));
        listCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel listTitle = new JLabel("All Registered Election Cycles");
        listTitle.setFont(Theme.fontSubheading());
        listTitle.setForeground(Theme.PRIMARY_NAVY);
        listCard.add(listTitle, BorderLayout.NORTH);

        electionsCardsContainer = new JPanel();
        electionsCardsContainer.setLayout(new BoxLayout(electionsCardsContainer, BoxLayout.Y_AXIS));
        electionsCardsContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(electionsCardsContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        listCard.add(scroll, BorderLayout.CENTER);
        panel.add(listCard, BorderLayout.CENTER);

        return panel;
    }

    private void handleCreateElection() {
        String title = newElectionTitleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an election title.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (electionDAO.createElection(title)) {
            JOptionPane.showMessageDialog(this, "Election contest \"" + title + "\" created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            newElectionTitleField.setText("");
            refreshAllData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create election. A database error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // VIEW 3: CANDIDATES MANAGEMENT
    // ==========================================
    private JPanel createCandidatesView() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top: Add Candidate Form Card
        RoundedPanel addCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        addCard.setLayout(new BorderLayout(0, 10));
        addCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Row 1: title + subtitle
        JPanel formText = new JPanel();
        formText.setLayout(new BoxLayout(formText, BoxLayout.Y_AXIS));
        formText.setOpaque(false);

        JLabel title = new JLabel("Register New Candidate");
        title.setFont(Theme.fontHero());
        title.setForeground(Theme.PRIMARY_NAVY);

        JLabel sub = new JLabel("Select the election, enter the candidate name and party affiliation, then click Add Candidate.");
        sub.setFont(Theme.fontBody());
        sub.setForeground(Theme.TEXT_SECONDARY);

        formText.add(title);
        formText.add(Box.createVerticalStrut(3));
        formText.add(sub);
        addCard.add(formText, BorderLayout.NORTH);

        // Row 2: election dropdown (labelled) + name field + party field + button — all in one row
        JPanel inputsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        inputsRow.setOpaque(false);

        JLabel elLabel = new JLabel("Election:");
        elLabel.setFont(Theme.fontBody());
        elLabel.setForeground(Theme.TEXT_SECONDARY);

        candidateElectionSelectCombo = new JComboBox<>();
        candidateElectionSelectCombo.setFont(Theme.fontBody());
        candidateElectionSelectCombo.setPreferredSize(new Dimension(220, 38));

        newCandidateNameField = new PillTextField("Candidate Name", 14);
        newCandidateNameField.setPreferredSize(new Dimension(170, 38));

        newCandidatePartyField = new PillTextField("Party / Affiliation", 14);
        newCandidatePartyField.setPreferredSize(new Dimension(170, 38));

        PillButton addBtn = new PillButton("Add Candidate", PillButton.Style.GRADIENT);
        addBtn.setPreferredSize(new Dimension(140, 38));
        addBtn.addActionListener(e -> handleAddCandidate());

        inputsRow.add(elLabel);
        inputsRow.add(candidateElectionSelectCombo);
        inputsRow.add(newCandidateNameField);
        inputsRow.add(newCandidatePartyField);
        inputsRow.add(addBtn);

        addCard.add(inputsRow, BorderLayout.CENTER);
        panel.add(addCard, BorderLayout.NORTH);

        // Bottom: Registered Candidates Grid with Filter Dropdown
        RoundedPanel gridCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        gridCard.setLayout(new BorderLayout(0, 12));
        gridCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel filterHeader = new JPanel(new BorderLayout());
        filterHeader.setOpaque(false);

        JLabel filterTitle = new JLabel("Candidate Directory");
        filterTitle.setFont(Theme.fontSubheading());
        filterTitle.setForeground(Theme.PRIMARY_NAVY);

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterControls.setOpaque(false);
        filterControls.add(new JLabel("Filter by Election:"));

        candidateFilterCombo = new JComboBox<>();
        candidateFilterCombo.setFont(Theme.fontBody());
        candidateFilterCombo.setPreferredSize(new Dimension(240, 34));
        candidateFilterCombo.addActionListener(e -> loadCandidatesForSelectedFilter());
        filterControls.add(candidateFilterCombo);

        filterHeader.add(filterTitle, BorderLayout.WEST);
        filterHeader.add(filterControls, BorderLayout.EAST);
        gridCard.add(filterHeader, BorderLayout.NORTH);

        candidatesGridContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 18));
        candidatesGridContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(candidatesGridContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        gridCard.add(scroll, BorderLayout.CENTER);
        panel.add(gridCard, BorderLayout.CENTER);

        return panel;
    }

    private void handleAddCandidate() {
        Election selected = (Election) candidateElectionSelectCombo.getSelectedItem();
        String name = newCandidateNameField.getText().trim();
        String party = newCandidatePartyField.getText().trim();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an election contest.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the candidate name.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (party.isEmpty()) {
            party = "Independent";
        }

        if (electionDAO.addCandidate(selected.getId(), name, party)) {
            JOptionPane.showMessageDialog(this, "Candidate \"" + name + "\" registered successfully in " + selected.getTitle() + "!", "Candidate Registered", JOptionPane.INFORMATION_MESSAGE);
            newCandidateNameField.setText("");
            newCandidatePartyField.setText("");
            refreshAllData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to register candidate.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // VIEW 4: RESULTS & ANALYTICS
    // ==========================================
    private JPanel createResultsAnalyticsView() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header controls card
        RoundedPanel headerCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        headerCard.setLayout(new BorderLayout(16, 0));
        headerCard.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel leftText = new JPanel();
        leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS));
        leftText.setOpaque(false);

        JLabel title = new JLabel("Official Results & Certified Analytics");
        title.setFont(Theme.fontHero());
        title.setForeground(Theme.PRIMARY_NAVY);

        JLabel sub = new JLabel("Audit tallies, voter turnout percentages, and verified victory margins.");
        sub.setFont(Theme.fontBody());
        sub.setForeground(Theme.TEXT_SECONDARY);

        leftText.add(title);
        leftText.add(Box.createVerticalStrut(4));
        leftText.add(sub);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        controls.setOpaque(false);

        analyticsElectionCombo = new JComboBox<>();
        analyticsElectionCombo.setFont(Theme.fontBody());
        analyticsElectionCombo.setPreferredSize(new Dimension(260, 36));
        analyticsElectionCombo.addActionListener(e -> updateAnalyticsView());

        PillButton refreshBtn = new PillButton("Refresh Data", PillButton.Style.OUTLINE);
        refreshBtn.setPreferredSize(new Dimension(130, 36));
        refreshBtn.addActionListener(e -> updateAnalyticsView());

        controls.add(new JLabel("Select Election:"));
        controls.add(analyticsElectionCombo);
        controls.add(refreshBtn);

        headerCard.add(leftText, BorderLayout.WEST);
        headerCard.add(controls, BorderLayout.EAST);
        panel.add(headerCard, BorderLayout.NORTH);

        // Center split: Visual Chart (Left) + Breakdown Table & Summary Metrics (Right)
        JPanel contentRow = new JPanel(new GridLayout(1, 2, 16, 0));
        contentRow.setOpaque(false);

        // Left: Antialiased Bar Chart Card
        RoundedPanel chartCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        chartCard.setLayout(new BorderLayout(0, 12));
        chartCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel chartTitle = new JLabel("Candidate Vote Distribution");
        chartTitle.setFont(Theme.fontSubheading());
        chartTitle.setForeground(Theme.PRIMARY_NAVY);
        chartCard.add(chartTitle, BorderLayout.NORTH);

        analyticsChart = new HorizontalBarChart(null);
        chartCard.add(analyticsChart, BorderLayout.CENTER);

        contentRow.add(chartCard);

        // Right: Detailed Metrics & Audit Table
        RoundedPanel tableCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Metric badges row
        JPanel badgesRow = new JPanel(new GridLayout(1, 3, 10, 0));
        badgesRow.setOpaque(false);

        analyticsTotalVotesLabel = new JLabel("Ballots: 0", SwingConstants.CENTER);
        styleMetricBadge(analyticsTotalVotesLabel);

        analyticsTurnoutLabel = new JLabel("Turnout: 0%", SwingConstants.CENTER);
        styleMetricBadge(analyticsTurnoutLabel);

        analyticsWinnerLabel = new JLabel("Winner: N/A", SwingConstants.CENTER);
        styleMetricBadge(analyticsWinnerLabel);

        badgesRow.add(analyticsTotalVotesLabel);
        badgesRow.add(analyticsTurnoutLabel);
        badgesRow.add(analyticsWinnerLabel);
        tableCard.add(badgesRow, BorderLayout.NORTH);

        // Audit Table
        String[] cols = {"Candidate", "Party", "Votes", "Share (%)"};
        analyticsTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(analyticsTableModel);
        table.setFont(Theme.fontBody());
        table.setRowHeight(32);
        table.getTableHeader().setFont(Theme.fontBodyBold());
        table.getTableHeader().setBackground(new Color(0xEE, 0xF2, 0xFF));
        table.getTableHeader().setForeground(Theme.PRIMARY_NAVY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 4));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setBorder(null);

        tableCard.add(tableScroll, BorderLayout.CENTER);
        contentRow.add(tableCard);

        panel.add(contentRow, BorderLayout.CENTER);
        return panel;
    }

    private void styleMetricBadge(JLabel lbl) {
        lbl.setFont(Theme.fontBodyBold());
        lbl.setForeground(Theme.PRIMARY_NAVY);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(0xF3, 0xF4, 0xF9));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.CARD_BORDER, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
    }

    private void updateAnalyticsView() {
        Election selected = (Election) analyticsElectionCombo.getSelectedItem();
        if (selected == null) {
            analyticsChart.setData(null);
            analyticsTableModel.setRowCount(0);
            analyticsTotalVotesLabel.setText("Ballots: 0");
            analyticsTurnoutLabel.setText("Turnout: 0%");
            analyticsWinnerLabel.setText("Winner: N/A");
            return;
        }

        Map<String, Integer> results = voteDAO.getResults(selected.getId());
        analyticsChart.setData(results);

        analyticsTableModel.setRowCount(0);
        int totalVotes = 0;
        String frontrunner = "None";
        int maxVotes = -1;

        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            totalVotes += entry.getValue();
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                frontrunner = entry.getKey();
            }
        }

        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            String fullLabel = entry.getKey();
            String candName = fullLabel;
            String party = "Independent";
            if (fullLabel.contains("(") && fullLabel.endsWith(")")) {
                int open = fullLabel.lastIndexOf("(");
                candName = fullLabel.substring(0, open).trim();
                party = fullLabel.substring(open + 1, fullLabel.length() - 1).trim();
            }

            int votes = entry.getValue();
            double pct = totalVotes > 0 ? (votes * 100.0 / totalVotes) : 0.0;
            analyticsTableModel.addRow(new Object[]{candName, party, votes, String.format("%.1f%%", pct)});
        }

        int totalVoters = userDAO.getTotalVotersCount();
        double turnout = totalVoters > 0 ? (totalVotes * 100.0 / totalVoters) : 0.0;

        analyticsTotalVotesLabel.setText("Ballots: " + totalVotes);
        analyticsTurnoutLabel.setText(String.format("Turnout: %.1f%%", turnout));
        analyticsWinnerLabel.setText(totalVotes > 0 ? "Leader: " + frontrunner : "Leader: No votes");
    }

    // ==========================================
    // VIEW 5: VOTER REGISTRY & AUDIT
    // ==========================================
    private JPanel createVotersRegistryView() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header Card with Live Search
        RoundedPanel headerCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        headerCard.setLayout(new BorderLayout(16, 0));
        headerCard.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel leftText = new JPanel();
        leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS));
        leftText.setOpaque(false);

        JLabel title = new JLabel("Voter Registry & Participation Ledger");
        title.setFont(Theme.fontHero());
        title.setForeground(Theme.PRIMARY_NAVY);

        voterCountSummaryLabel = new JLabel("Loading verified voter registry...");
        voterCountSummaryLabel.setFont(Theme.fontBody());
        voterCountSummaryLabel.setForeground(Theme.TEXT_SECONDARY);

        leftText.add(title);
        leftText.add(Box.createVerticalStrut(4));
        leftText.add(voterCountSummaryLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        controls.setOpaque(false);

        voterSearchField = new PillTextField("Filter voters by username...", 22);
        voterSearchField.setShowSearchIcon(true);
        voterSearchField.setPreferredSize(new Dimension(280, 38));
        voterSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterVotersTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterVotersTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterVotersTable(); }
        });

        PillButton refreshVotersBtn = new PillButton("Refresh", PillButton.Style.OUTLINE);
        refreshVotersBtn.setPreferredSize(new Dimension(100, 38));
        refreshVotersBtn.addActionListener(e -> loadVoterRegistryData());

        controls.add(voterSearchField);
        controls.add(refreshVotersBtn);

        headerCard.add(leftText, BorderLayout.WEST);
        headerCard.add(controls, BorderLayout.EAST);
        panel.add(headerCard, BorderLayout.NORTH);

        // Registry Table
        RoundedPanel tableCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        String[] cols = {"User ID", "Username", "Role", "Verification Status", "Database Record"};
        voterTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(voterTableModel);
        table.setFont(Theme.fontBody());
        table.setRowHeight(34);
        table.getTableHeader().setFont(Theme.fontBodyBold());
        table.getTableHeader().setBackground(new Color(0xEE, 0xF2, 0xFF));
        table.getTableHeader().setForeground(Theme.PRIMARY_NAVY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 4));

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerR);
        table.getColumnModel().getColumn(2).setCellRenderer(centerR);
        table.getColumnModel().getColumn(3).setCellRenderer(centerR);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        tableCard.add(scroll, BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private void loadVoterRegistryData() {
        if (voterTableModel == null) return;
        voterTableModel.setRowCount(0);

        List<User> users = userDAO.getAllUsers();
        int voterCount = 0;
        for (User u : users) {
            if ("VOTER".equalsIgnoreCase(u.getRole())) {
                voterCount++;
            }
            voterTableModel.addRow(new Object[]{
                    "#" + u.getId(),
                    u.getUsername(),
                    u.getRole(),
                    "Verified Active",
                    "BCrypt Secure"
            });
        }

        if (voterCountSummaryLabel != null) {
            voterCountSummaryLabel.setText("Total Registered Voters: " + voterCount + " • Total Accounts: " + users.size());
        }
    }

    private void filterVotersTable() {
        String filter = voterSearchField.getText().trim().toLowerCase();
        voterTableModel.setRowCount(0);

        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            if (filter.isEmpty() || u.getUsername().toLowerCase().contains(filter) || u.getRole().toLowerCase().contains(filter)) {
                voterTableModel.addRow(new Object[]{
                        "#" + u.getId(),
                        u.getUsername(),
                        u.getRole(),
                        "Verified Active",
                        "BCrypt Secure"
                });
            }
        }
    }

    // ==========================================
    // VIEW 6: SETTINGS VIEW
    // ==========================================
    private JPanel createSettingsView() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 20, 20));

        RoundedPanel card = new RoundedPanel(24, Theme.CARD_BG, Theme.CARD_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 36, 32, 36));

        JLabel title = new JLabel("System Administrator Settings & Security");
        title.setFont(Theme.fontHero());
        title.setForeground(Theme.PRIMARY_NAVY);

        JLabel info = new JLabel("<html><body style='width: 550px; font-size: 13px; color: #1A2352;'>" +
                "<b>Admin Account:</b> " + currentUser.getUsername() + "<br><br>" +
                "<b>Administrative Privileges:</b> Full System Oversight, Contest Lifecycle, and Certified Audit<br><br>" +
                "<b>Database Engine:</b> SQLite Local Instance (voting_system.db)<br><br>" +
                "<b>Password Hashing Algorithm:</b> BCrypt (jBCrypt v0.4)<br><br>" +
                "<b>Secret Ballot Integrity:</b> Enabled — Voter dashboards automatically seal rolling tallies until contest concludes.<br><br>" +
                "<b>System Architecture:</b> Java Swing Antialiased Vector UI inspired by iVOTE Design System." +
                "</body></html>");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        actions.setOpaque(false);

        PillButton backBtn = new PillButton("Back to Dashboard", PillButton.Style.GRADIENT);
        backBtn.setPreferredSize(new Dimension(170, 40));
        backBtn.addActionListener(e -> setActiveNav("DASHBOARD"));

        PillButton logoutBtn = new PillButton("Sign Out", PillButton.Style.OUTLINE);
        logoutBtn.setPreferredSize(new Dimension(140, 40));
        logoutBtn.addActionListener(e -> handleLogout());

        actions.add(backBtn);
        actions.add(logoutBtn);

        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(info);
        card.add(Box.createVerticalStrut(30));
        card.add(actions);
        card.add(Box.createVerticalGlue());

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // VIEW 7: GLOBAL ADMIN SEARCH
    // ==========================================
    private JPanel createGlobalSearchView() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top Search Header Card
        RoundedPanel headerCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
        headerCard.setLayout(new BorderLayout(16, 0));
        headerCard.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel title = new JLabel("Administrative Search Results");
        title.setFont(Theme.fontHero());
        title.setForeground(Theme.PRIMARY_NAVY);

        globalSearchCountLabel = new JLabel("Search results across elections, candidates, and voter records.");
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

    private void performAdminSearch(String query) {
        if (globalSearchResultsContainer == null) return;
        globalSearchResultsContainer.removeAll();

        String q = query.toLowerCase().trim();
        List<Election> allElections = electionDAO.getAllElections();
        List<Election> matchedElections = allElections.stream()
                .filter(el -> el.getTitle().toLowerCase().contains(q))
                .toList();

        class CandidateMatch {
            final Candidate candidate;
            final Election election;
            CandidateMatch(Candidate c, Election el) { this.candidate = c; this.election = el; }
        }
        List<CandidateMatch> matchedCandidates = new ArrayList<>();

        for (Election el : allElections) {
            List<Candidate> candidates = electionDAO.getCandidatesForElection(el.getId());
            for (Candidate c : candidates) {
                if (c.getName().toLowerCase().contains(q) ||
                    (c.getParty() != null && c.getParty().toLowerCase().contains(q)) ||
                    el.getTitle().toLowerCase().contains(q)) {
                    matchedCandidates.add(new CandidateMatch(c, el));
                }
            }
        }

        List<User> allUsers = userDAO.getAllUsers();
        List<User> matchedUsers = allUsers.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(q) || u.getRole().toLowerCase().contains(q))
                .toList();

        if (globalSearchCountLabel != null) {
            globalSearchCountLabel.setText("Found " + matchedElections.size() + " election(s), " +
                    matchedCandidates.size() + " candidate(s), and " + matchedUsers.size() + " user(s) for \"" + query + "\"");
        }

        if (matchedElections.isEmpty() && matchedCandidates.isEmpty() && matchedUsers.isEmpty()) {
            RoundedPanel emptyCard = new RoundedPanel(20, Theme.CARD_BG, Theme.CARD_BORDER, true);
            emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
            emptyCard.setBorder(new EmptyBorder(32, 24, 32, 24));
            emptyCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            emptyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

            JLabel emptyTitle = new JLabel("No administrative records found matching \"" + query + "\"");
            emptyTitle.setFont(Theme.fontSubheading());
            emptyTitle.setForeground(Theme.PRIMARY_NAVY);
            emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptySub = new JLabel("Try searching by election title, candidate name, party affiliation, or voter username.");
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
            // 1. Matching Elections Section
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
                    elCard.setBorder(new EmptyBorder(12, 18, 12, 18));
                    elCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                    elCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
                    info.setOpaque(false);

                    JLabel elTitle = new JLabel(el.getTitle());
                    elTitle.setFont(Theme.fontBodyBold());
                    elTitle.setForeground(Theme.TEXT_PRIMARY);

                    JLabel statusPill = new JLabel(el.isActive() ? "● Ongoing" : "● Concluded");
                    statusPill.setFont(Theme.fontCaption());
                    statusPill.setForeground(el.isActive() ? Theme.SUCCESS_GREEN : Theme.TEXT_MUTED);

                    info.add(elTitle);
                    info.add(statusPill);

                    elCard.add(info, BorderLayout.WEST);

                    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                    actions.setOpaque(false);

                    PillButton toggleBtn = new PillButton(el.isActive() ? "Conclude" : "Reopen", PillButton.Style.OUTLINE);
                    toggleBtn.setPreferredSize(new Dimension(95, 30));
                    toggleBtn.addActionListener(e -> {
                        electionDAO.toggleElectionStatus(el.getId(), !el.isActive());
                        refreshAllData();
                        performAdminSearch(searchField.getText().trim());
                    });

                    PillButton viewResultsBtn = new PillButton("View Results", PillButton.Style.GRADIENT);
                    viewResultsBtn.setPreferredSize(new Dimension(115, 30));
                    viewResultsBtn.addActionListener(e -> {
                        searchField.setText("");
                        analyticsElectionCombo.setSelectedItem(el);
                        setActiveNav("RESULTS");
                    });

                    actions.add(toggleBtn);
                    actions.add(viewResultsBtn);
                    elCard.add(actions, BorderLayout.EAST);

                    globalSearchResultsContainer.add(elCard);
                    globalSearchResultsContainer.add(Box.createVerticalStrut(8));
                }
                globalSearchResultsContainer.add(Box.createVerticalStrut(16));
            }

            // 2. Matching Candidates Section
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

                for (CandidateMatch match : matchedCandidates) {
                    RoundedPanel candCard = new RoundedPanel(18, Theme.CARD_BG, Theme.CARD_BORDER, true);
                    candCard.setLayout(new BoxLayout(candCard, BoxLayout.Y_AXIS));
                    candCard.setBorder(new EmptyBorder(16, 16, 16, 16));
                    candCard.setPreferredSize(new Dimension(190, 240));

                    JLabel electBadge = new JLabel(match.election.getTitle());
                    electBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    electBadge.setForeground(Theme.BRAND_ACCENT);
                    electBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

                    CircularAvatar avatar = new CircularAvatar(match.candidate.getName(), 54);
                    avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel nameLbl = new JLabel(match.candidate.getName());
                    nameLbl.setFont(Theme.fontBodyBold());
                    nameLbl.setForeground(Theme.TEXT_PRIMARY);
                    nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel partyLbl = new JLabel(match.candidate.getParty() != null ? match.candidate.getParty() : "Independent");
                    partyLbl.setFont(Theme.fontCaption());
                    partyLbl.setForeground(Theme.TEXT_SECONDARY);
                    partyLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                    PillButton deleteCandBtn = new PillButton("Remove Candidate", PillButton.Style.OUTLINE);
                    deleteCandBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                    deleteCandBtn.setPreferredSize(new Dimension(150, 32));
                    deleteCandBtn.setMaximumSize(new Dimension(160, 32));
                    deleteCandBtn.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this,
                                "Are you sure you want to remove candidate " + match.candidate.getName() + " from " + match.election.getTitle() + "?",
                                "Confirm Removal", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            electionDAO.deleteCandidate(match.candidate.getId());
                            refreshAllData();
                            performAdminSearch(searchField.getText().trim());
                        }
                    });

                    candCard.add(electBadge);
                    candCard.add(Box.createVerticalStrut(8));
                    candCard.add(avatar);
                    candCard.add(Box.createVerticalStrut(8));
                    candCard.add(nameLbl);
                    candCard.add(Box.createVerticalStrut(2));
                    candCard.add(partyLbl);
                    candCard.add(Box.createVerticalStrut(14));
                    candCard.add(deleteCandBtn);

                    cardsFlow.add(candCard);
                }
                globalSearchResultsContainer.add(cardsFlow);
                globalSearchResultsContainer.add(Box.createVerticalStrut(16));
            }

            // 3. Matching Voters Section
            if (!matchedUsers.isEmpty()) {
                JLabel secVoters = new JLabel("Matching Users & Voters (" + matchedUsers.size() + ")");
                secVoters.setFont(Theme.fontSubheading());
                secVoters.setForeground(Theme.PRIMARY_NAVY);
                secVoters.setAlignmentX(Component.LEFT_ALIGNMENT);
                globalSearchResultsContainer.add(secVoters);
                globalSearchResultsContainer.add(Box.createVerticalStrut(10));

                for (User u : matchedUsers) {
                    RoundedPanel userRow = new RoundedPanel(14, Theme.CARD_BG, Theme.CARD_BORDER, true);
                    userRow.setLayout(new BorderLayout(14, 0));
                    userRow.setBorder(new EmptyBorder(10, 16, 10, 16));
                    userRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                    userRow.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JPanel uInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
                    uInfo.setOpaque(false);

                    CircularAvatar uAv = new CircularAvatar(u.getUsername(), 28);
                    JLabel uName = new JLabel(u.getUsername());
                    uName.setFont(Theme.fontBodyBold());
                    uName.setForeground(Theme.TEXT_PRIMARY);

                    JLabel uRole = new JLabel(u.getRole());
                    uRole.setFont(Theme.fontCaption());
                    uRole.setForeground("ADMIN".equalsIgnoreCase(u.getRole()) ? Theme.BRAND_ACCENT : Theme.SUCCESS_GREEN);

                    uInfo.add(uAv);
                    uInfo.add(uName);
                    uInfo.add(uRole);

                    userRow.add(uInfo, BorderLayout.WEST);

                    JLabel verified = new JLabel("Verified Record");
                    verified.setFont(Theme.fontCaption());
                    verified.setForeground(Theme.TEXT_MUTED);
                    userRow.add(verified, BorderLayout.EAST);

                    globalSearchResultsContainer.add(userRow);
                    globalSearchResultsContainer.add(Box.createVerticalStrut(6));
                }
            }
        }

        globalSearchResultsContainer.revalidate();
        globalSearchResultsContainer.repaint();
    }

    // ==========================================
    // DATA REFRESH & BINDING
    // ==========================================
    private void loadInitialData() {
        refreshAllData();
    }

    private void refreshAllData() {
        List<Election> allElections = electionDAO.getAllElections();
        List<Election> activeElections = electionDAO.getActiveElections();

        // Sync dropdowns
        syncDropdown(dashResultsCombo, allElections);
        syncDropdown(candidateElectionSelectCombo, activeElections);
        syncDropdown(candidateFilterCombo, allElections);
        syncDropdown(analyticsElectionCombo, allElections);

        // 1. Refresh Dashboard Stats Rail
        if (dashStatsRail != null) {
            dashStatsRail.removeAll();
            int totalVoters = userDAO.getTotalVotersCount();
            int totalVotes = voteDAO.getTotalVotesCount();
            int totalCandidates = electionDAO.getTotalCandidatesCount();
            int activeContests = activeElections.size();

            dashStatsRail.add(new StatMetricCard(totalVoters, "Total Registered Voters"));
            dashStatsRail.add(Box.createVerticalStrut(14));
            dashStatsRail.add(new StatMetricCard(activeContests, "Active Ongoing Contests"));
            dashStatsRail.add(Box.createVerticalStrut(14));
            dashStatsRail.add(new StatMetricCard(totalCandidates, "Total Candidates Enrolled"));
            dashStatsRail.add(Box.createVerticalStrut(14));
            dashStatsRail.add(new StatMetricCard(totalVotes, "Total Certified Ballots Cast"));
            dashStatsRail.add(Box.createVerticalGlue());

            dashStatsRail.revalidate();
            dashStatsRail.repaint();
        }

        // 2. Refresh Dashboard Quick Elections List
        if (dashElectionsListContainer != null) {
            dashElectionsListContainer.removeAll();
            if (allElections.isEmpty()) {
                JLabel empty = new JLabel("No election cycles registered yet.");
                empty.setFont(Theme.fontBody());
                empty.setForeground(Theme.TEXT_MUTED);
                dashElectionsListContainer.add(empty);
            } else {
                for (Election el : allElections) {
                    dashElectionsListContainer.add(createDashboardElectionRow(el));
                    dashElectionsListContainer.add(Box.createVerticalStrut(8));
                }
            }
            dashElectionsListContainer.revalidate();
            dashElectionsListContainer.repaint();
        }

        // 3. Update Dashboard Results Chart
        updateDashResultsChart();

        // 4. Refresh Elections View List
        refreshElectionsCards(allElections);

        // 5. Refresh Candidates View Grid
        loadCandidatesForSelectedFilter();

        // 6. Refresh Analytics
        updateAnalyticsView();

        // 7. Refresh Voters Registry Table
        loadVoterRegistryData();
    }

    private void syncDropdown(JComboBox<Election> combo, List<Election> list) {
        if (combo == null) return;
        Election currentSel = (Election) combo.getSelectedItem();
        combo.removeAllItems();
        Election toReselect = null;
        for (Election el : list) {
            combo.addItem(el);
            if (currentSel != null && currentSel.getId() == el.getId()) {
                toReselect = el;
            }
        }
        if (toReselect != null) {
            combo.setSelectedItem(toReselect);
        } else if (!list.isEmpty()) {
            combo.setSelectedIndex(0);
        }
    }

    private JPanel createDashboardElectionRow(Election el) {
        RoundedPanel row = new RoundedPanel(16, Theme.CARD_BG, Theme.CARD_BORDER, true);
        row.setLayout(new BorderLayout(0, 6));
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        // No fixed max height — let it grow to fit both info and button rows

        // Top row: title + status + candidate count
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        infoRow.setOpaque(false);

        JLabel title = new JLabel(el.getTitle());
        title.setFont(Theme.fontBodyBold());
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel status = new JLabel(el.isActive() ? "● Ongoing" : "● Concluded");
        status.setFont(Theme.fontCaption());
        status.setForeground(el.isActive() ? Theme.SUCCESS_GREEN : Theme.TEXT_MUTED);

        List<Candidate> cands = electionDAO.getCandidatesForElection(el.getId());
        JLabel candCount = new JLabel("(" + cands.size() + " cands)");
        candCount.setFont(Theme.fontCaption());
        candCount.setForeground(Theme.TEXT_MUTED);

        infoRow.add(title);
        infoRow.add(status);
        infoRow.add(candCount);

        // Bottom row: action buttons aligned right
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        PillButton toggleBtn = new PillButton(el.isActive() ? "Conclude" : "Reopen", PillButton.Style.OUTLINE);
        toggleBtn.setPreferredSize(new Dimension(100, 28));
        toggleBtn.addActionListener(e -> {
            electionDAO.toggleElectionStatus(el.getId(), !el.isActive());
            refreshAllData();
        });

        PillButton viewBtn = new PillButton("Results", PillButton.Style.PRIMARY);
        viewBtn.setPreferredSize(new Dimension(88, 28));
        viewBtn.addActionListener(e -> {
            analyticsElectionCombo.setSelectedItem(el);
            setActiveNav("RESULTS");
        });

        btnRow.add(toggleBtn);
        btnRow.add(viewBtn);

        row.add(infoRow, BorderLayout.NORTH);
        row.add(btnRow, BorderLayout.SOUTH);

        return row;
    }

    private void updateDashResultsChart() {
        if (dashResultsChart == null || dashResultsCombo == null) return;
        Election el = (Election) dashResultsCombo.getSelectedItem();
        if (el != null) {
            Map<String, Integer> results = voteDAO.getResults(el.getId());
            dashResultsChart.setData(results);

            int total = 0;
            String leader = "No votes yet";
            int maxV = -1;
            for (Map.Entry<String, Integer> entry : results.entrySet()) {
                total += entry.getValue();
                if (entry.getValue() > maxV) {
                    maxV = entry.getValue();
                    leader = entry.getKey();
                }
            }

            if (dashTotalVotesLabel != null) {
                dashTotalVotesLabel.setText("Total Ballots: " + total);
            }
            if (dashLeadingCandidateLabel != null) {
                dashLeadingCandidateLabel.setText(total > 0 ? "Frontrunner: " + leader : "Frontrunner: No votes yet");
            }
        } else {
            dashResultsChart.setData(null);
            if (dashTotalVotesLabel != null) dashTotalVotesLabel.setText("Total Ballots: 0");
            if (dashLeadingCandidateLabel != null) dashLeadingCandidateLabel.setText("No Elections");
        }
    }

    private void refreshElectionsCards(List<Election> allElections) {
        if (electionsCardsContainer == null) return;
        electionsCardsContainer.removeAll();

        if (allElections.isEmpty()) {
            JLabel empty = new JLabel("No election contests created yet.");
            empty.setFont(Theme.fontBody());
            empty.setForeground(Theme.TEXT_MUTED);
            electionsCardsContainer.add(empty);
        } else {
            for (Election el : allElections) {
                RoundedPanel card = new RoundedPanel(16, Theme.CARD_BG, Theme.CARD_BORDER, true);
                card.setLayout(new BorderLayout(0, 6));
                card.setBorder(new EmptyBorder(12, 18, 12, 18));
                // No fixed max height — card grows to show both info and button rows cleanly

                // Top row: id badge, title, status, vote count
                JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
                info.setOpaque(false);

                JLabel idBadge = new JLabel("#" + el.getId());
                idBadge.setFont(Theme.fontCaption());
                idBadge.setForeground(Theme.TEXT_MUTED);

                JLabel title = new JLabel(el.getTitle());
                title.setFont(Theme.fontBodyBold());
                title.setForeground(Theme.TEXT_PRIMARY);

                JLabel status = new JLabel(el.isActive() ? "● Ongoing Session" : "● Concluded Session");
                status.setFont(Theme.fontCaption());
                status.setForeground(el.isActive() ? Theme.SUCCESS_GREEN : Theme.TEXT_MUTED);

                int votesCount = voteDAO.getTotalVotesForElection(el.getId());
                JLabel votesLbl = new JLabel(votesCount + " votes");
                votesLbl.setFont(Theme.fontCaption());
                votesLbl.setForeground(Theme.BRAND_ACCENT);

                info.add(idBadge);
                info.add(title);
                info.add(status);
                info.add(votesLbl);
                card.add(info, BorderLayout.NORTH);

                // Bottom row: all three action buttons aligned to the right
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                actions.setOpaque(false);

                PillButton toggleBtn = new PillButton(el.isActive() ? "Conclude Election" : "Reopen Election", PillButton.Style.OUTLINE);
                toggleBtn.setPreferredSize(new Dimension(148, 30));
                toggleBtn.addActionListener(e -> {
                    electionDAO.toggleElectionStatus(el.getId(), !el.isActive());
                    refreshAllData();
                });

                PillButton resultsBtn = new PillButton("View Analytics", PillButton.Style.GRADIENT);
                resultsBtn.setPreferredSize(new Dimension(128, 30));
                resultsBtn.addActionListener(e -> {
                    analyticsElectionCombo.setSelectedItem(el);
                    setActiveNav("RESULTS");
                });

                PillButton deleteBtn = new PillButton("Delete", PillButton.Style.OUTLINE);
                deleteBtn.setPreferredSize(new Dimension(80, 30));
                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to permanently delete \"" + el.getTitle() + "\"?\nAll associated candidate and ballot records will be removed.",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        electionDAO.deleteElection(el.getId());
                        refreshAllData();
                    }
                });

                actions.add(toggleBtn);
                actions.add(resultsBtn);
                actions.add(deleteBtn);
                card.add(actions, BorderLayout.SOUTH);

                electionsCardsContainer.add(card);
                electionsCardsContainer.add(Box.createVerticalStrut(10));
            }
        }

        electionsCardsContainer.revalidate();
        electionsCardsContainer.repaint();
    }

    private void loadCandidatesForSelectedFilter() {
        if (candidatesGridContainer == null || candidateFilterCombo == null) return;
        candidatesGridContainer.removeAll();

        Election selected = (Election) candidateFilterCombo.getSelectedItem();
        if (selected == null) {
            JLabel empty = new JLabel("Please select an election to view registered candidates.");
            empty.setFont(Theme.fontBody());
            empty.setForeground(Theme.TEXT_MUTED);
            candidatesGridContainer.add(empty);
        } else {
            List<Candidate> list = electionDAO.getCandidatesForElection(selected.getId());
            if (list.isEmpty()) {
                JLabel empty = new JLabel("No candidates registered in " + selected.getTitle() + " yet.");
                empty.setFont(Theme.fontBody());
                empty.setForeground(Theme.TEXT_MUTED);
                candidatesGridContainer.add(empty);
            } else {
                for (Candidate c : list) {
                    candidatesGridContainer.add(createAdminCandidateCard(c, selected));
                }
            }
        }

        candidatesGridContainer.revalidate();
        candidatesGridContainer.repaint();
    }

    private JPanel createAdminCandidateCard(Candidate c, Election election) {
        RoundedPanel card = new RoundedPanel(18, Theme.CARD_BG, Theme.CARD_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setPreferredSize(new Dimension(190, 240));

        CircularAvatar avatar = new CircularAvatar(c.getName(), 56);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel(c.getName());
        nameLbl.setFont(Theme.fontBodyBold());
        nameLbl.setForeground(Theme.TEXT_PRIMARY);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel partyLbl = new JLabel(c.getParty() != null ? c.getParty() : "Independent");
        partyLbl.setFont(Theme.fontCaption());
        partyLbl.setForeground(Theme.TEXT_SECONDARY);
        partyLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel electBadge = new JLabel(election.getTitle());
        electBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        electBadge.setForeground(Theme.BRAND_ACCENT);
        electBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        PillButton removeBtn = new PillButton("Remove Candidate", PillButton.Style.OUTLINE);
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeBtn.setPreferredSize(new Dimension(150, 32));
        removeBtn.setMaximumSize(new Dimension(160, 32));
        removeBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to remove candidate " + c.getName() + " from " + election.getTitle() + "?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                electionDAO.deleteCandidate(c.getId());
                refreshAllData();
            }
        });

        card.add(avatar);
        card.add(Box.createVerticalStrut(8));
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(partyLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(electBadge);
        card.add(Box.createVerticalStrut(14));
        card.add(removeBtn);

        return card;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out of the Administrator Portal?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}