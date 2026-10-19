package com.votingsystem.ui;

import com.votingsystem.dao.UserDAO;
import com.votingsystem.model.User;
import com.votingsystem.ui.components.*;
import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {
    private final UserDAO userDAO = new UserDAO();

    private boolean isRegisterMode = false;

    // Form inputs
    private final PillTextField usernameField = new PillTextField("Enter your username", 20);
    private final PillPasswordField passwordField = new PillPasswordField("Enter your password", 20);
    private final SegmentedRoleSelector roleSelector = new SegmentedRoleSelector();
    private final PillButton submitBtn = new PillButton("Sign In", PillButton.Style.GRADIENT);

    // Containers & Labels
    private JPanel roleContainer;
    private RoundedPanel authCard;
    private final JLabel formTitleLabel = new JLabel("Welcome Back");
    private final JLabel formSubtitleLabel = new JLabel("Please enter your credentials to access the system");
    private final JLabel statusLabel = new JLabel(" ");
    private JButton tabSignInBtn;
    private JButton tabRegisterBtn;
    private final JButton toggleModeLink = new JButton("Don't have an account? Register as a Voter");

    public LoginFrame() {
        setTitle("iVOTE - Online Voting System");
        setSize(1040, 680);
        setMinimumSize(new Dimension(960, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Background Canvas
        JPanel canvas = new JPanel(new BorderLayout(0, 20)) {
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
        canvas.setBorder(new EmptyBorder(25, 40, 30, 40));

        // 1. Top Navigation Bar
        canvas.add(createTopNavbar(), BorderLayout.NORTH);

        // 2. Main Content Split Pane (Left Hero + Right Card)
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(createLeftHeroPanel());
        contentPanel.add(createRightCardPanel());

        canvas.add(contentPanel, BorderLayout.CENTER);
        add(canvas);

        setupEventHandlers();
        updateMode(false);
    }

    private JPanel createTopNavbar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);

        // Left brand emblem
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);
        brandPanel.add(new BrandBadge(36));

        JLabel brandText = new JLabel("Online Voting System");
        brandText.setFont(Theme.fontSubheading());
        brandText.setForeground(Theme.PRIMARY_NAVY);
        brandPanel.add(brandText);

        nav.add(brandPanel, BorderLayout.WEST);

        // Right navigation links
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 6));
        linksPanel.setOpaque(false);

        addNavLink(linksPanel, "Home", e -> updateMode(false));
        addNavLink(linksPanel, "About", e -> JOptionPane.showMessageDialog(this,
                "iVOTE Online Voting System\nVersion 1.0 Snapshot\nSecure, Transparent, and Verified Digital Elections.",
                "About iVOTE", JOptionPane.INFORMATION_MESSAGE));
        addNavLink(linksPanel, "Security", e -> JOptionPane.showMessageDialog(this,
                "Security Features:\n• BCrypt Cryptographic Password Hashing\n• SQLite ACID-Compliant Transaction Storage\n• One-Vote-Per-Electorate Integrity Verification",
                "System Security", JOptionPane.INFORMATION_MESSAGE));
        addNavLink(linksPanel, "Contact Us", e -> JOptionPane.showMessageDialog(this,
                "Support Desk:\nsupport@ivote.org\nMonday - Friday, 9:00 AM - 6:00 PM",
                "Contact Support", JOptionPane.INFORMATION_MESSAGE));

        nav.add(linksPanel, BorderLayout.EAST);
        return nav;
    }

    private void addNavLink(JPanel container, String text, java.awt.event.ActionListener action) {
        JLabel link = new JLabel(text);
        link.setFont(Theme.fontBody());
        link.setForeground(Theme.TEXT_SECONDARY);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(null);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                link.setForeground(Theme.BRAND_ACCENT);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                link.setForeground(Theme.TEXT_SECONDARY);
            }
        });
        container.add(link);
    }

    private JPanel createLeftHeroPanel() {
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(25, 10, 10, 20));

        JLabel title1 = new JLabel("iVOTE");
        title1.setFont(Theme.fontHero());
        title1.setForeground(Theme.BRAND_ACCENT);

        JLabel title2 = new JLabel("Online Voting System");
        title2.setFont(Theme.fontHero());
        title2.setForeground(Theme.PRIMARY_NAVY);

        JLabel subtitle = new JLabel("<html><body style='width: 320px;'>Let's make voting and elections easy for you. Designed to ensure a secured, transparent, and verified voting session.</body></html>");
        subtitle.setFont(Theme.fontBody());
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        VotingIllustrationPanel illustration = new VotingIllustrationPanel();
        illustration.setAlignmentX(Component.LEFT_ALIGNMENT);

        hero.add(Box.createVerticalStrut(20));
        hero.add(title1);
        hero.add(title2);
        hero.add(Box.createVerticalStrut(16));
        hero.add(subtitle);
        hero.add(Box.createVerticalStrut(30));
        hero.add(illustration);
        hero.add(Box.createVerticalGlue());

        return hero;
    }

    private JPanel createRightCardPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        // Floating Card
        authCard = new RoundedPanel(24, Theme.CARD_BG, Theme.CARD_BORDER, true);
        authCard.setLayout(new BoxLayout(authCard, BoxLayout.Y_AXIS));
        authCard.setBorder(new EmptyBorder(32, 36, 32, 36));
        authCard.setPreferredSize(new Dimension(420, 520));
        authCard.setMaximumSize(new Dimension(440, 540));

        // Tab Pill Switcher (Sign In vs Register)
        JPanel tabContainer = createTabSwitcher();
        tabContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Headers
        formTitleLabel.setFont(Theme.fontTitle());
        formTitleLabel.setForeground(Theme.TEXT_PRIMARY);
        formTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        formSubtitleLabel.setFont(Theme.fontCaption());
        formSubtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        formSubtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Status Label
        statusLabel.setFont(Theme.fontCaption());
        statusLabel.setForeground(Theme.STATUS_ERROR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Fields Container
        JPanel inputsPanel = new JPanel();
        inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));
        inputsPanel.setOpaque(false);
        inputsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role Container (Only shown in Register mode)
        roleContainer = new JPanel();
        roleContainer.setLayout(new BoxLayout(roleContainer, BoxLayout.Y_AXIS));
        roleContainer.setOpaque(false);
        roleContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("Register As:");
        roleLabel.setFont(Theme.fontBodyBold());
        roleLabel.setForeground(Theme.TEXT_PRIMARY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleContainer.add(roleLabel);
        roleContainer.add(Box.createVerticalStrut(6));
        roleContainer.add(roleSelector);
        roleContainer.add(Box.createVerticalStrut(14));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(Theme.fontBodyBold());
        userLabel.setForeground(Theme.TEXT_PRIMARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(Theme.fontBodyBold());
        passLabel.setForeground(Theme.TEXT_PRIMARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        inputsPanel.add(roleContainer);
        inputsPanel.add(userLabel);
        inputsPanel.add(Box.createVerticalStrut(6));
        inputsPanel.add(usernameField);
        inputsPanel.add(Box.createVerticalStrut(14));
        inputsPanel.add(passLabel);
        inputsPanel.add(Box.createVerticalStrut(6));
        inputsPanel.add(passwordField);

        // Submit Button
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Bottom toggle link
        toggleModeLink.setFont(Theme.fontCaption());
        toggleModeLink.setForeground(Theme.BRAND_ACCENT);
        toggleModeLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleModeLink.setBorderPainted(false);
        toggleModeLink.setContentAreaFilled(false);
        toggleModeLink.setFocusPainted(false);
        toggleModeLink.setAlignmentX(Component.CENTER_ALIGNMENT);

        authCard.add(tabContainer);
        authCard.add(Box.createVerticalStrut(18));
        authCard.add(formTitleLabel);
        authCard.add(Box.createVerticalStrut(4));
        authCard.add(formSubtitleLabel);
        authCard.add(Box.createVerticalStrut(10));
        authCard.add(statusLabel);
        authCard.add(Box.createVerticalStrut(10));
        authCard.add(inputsPanel);
        authCard.add(Box.createVerticalStrut(22));
        authCard.add(submitBtn);
        authCard.add(Box.createVerticalStrut(14));
        authCard.add(toggleModeLink);

        wrapper.add(authCard);
        return wrapper;
    }

    private JPanel createTabSwitcher() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyRenderHints(g2);
                g2.setColor(Theme.INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(Theme.INPUT_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 42));
        panel.setMaximumSize(new Dimension(280, 42));
        panel.setBorder(new EmptyBorder(3, 4, 3, 4));

        tabSignInBtn = createTabButton("Sign In", true);
        tabRegisterBtn = createTabButton("Register", false);

        panel.add(tabSignInBtn);
        panel.add(tabRegisterBtn);

        return panel;
    }

    private JButton createTabButton(String text, boolean isSignIn) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyRenderHints(g2);

                boolean isActive = isSignIn ? !isRegisterMode : isRegisterMode;
                int w = getWidth();
                int h = getHeight();

                if (isActive) {
                    GradientPaint gp = new GradientPaint(0, 0, Theme.BRAND_GRADIENT_START, w, 0, Theme.BRAND_GRADIENT_END);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, h, h);
                    setForeground(Color.WHITE);
                } else {
                    setForeground(Theme.TEXT_SECONDARY);
                }

                FontMetrics fm = g2.getFontMetrics(getFont());
                int tw = fm.stringWidth(getText());
                int th = fm.getAscent();
                g2.setFont(getFont());
                g2.setColor(getForeground());
                g2.drawString(getText(), (w - tw) / 2, (h + th) / 2 - 2);

                g2.dispose();
            }
        };

        btn.setFont(Theme.fontBodyBold());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void setupEventHandlers() {
        // Tab click
        tabSignInBtn.addActionListener(e -> updateMode(false));
        tabRegisterBtn.addActionListener(e -> updateMode(true));

        // Bottom link toggle
        toggleModeLink.addActionListener(e -> updateMode(!isRegisterMode));

        // Submit action
        submitBtn.addActionListener(e -> {
            if (isRegisterMode) {
                handleRegister();
            } else {
                handleLogin();
            }
        });

        // Enter key submits form
        passwordField.addActionListener(e -> submitBtn.doClick());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
    }

    private void updateMode(boolean registerMode) {
        this.isRegisterMode = registerMode;
        statusLabel.setText(" ");

        if (registerMode) {
            formTitleLabel.setText("Create an Account");
            formSubtitleLabel.setText("Sign up as a verified voter or administrator");
            submitBtn.setText("Register Now");
            toggleModeLink.setText("Already have an account? Sign In");
            roleContainer.setVisible(true);
        } else {
            formTitleLabel.setText("Welcome Back");
            formSubtitleLabel.setText("Please enter your credentials to access the system");
            submitBtn.setText("Sign In");
            toggleModeLink.setText("Don't have an account? Register as a Voter");
            roleContainer.setVisible(false);
        }

        tabSignInBtn.repaint();
        tabRegisterBtn.repaint();

        if (authCard != null) {
            authCard.revalidate();
            authCard.repaint();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both username and password.", true);
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.authenticate(username, password);

        if (user != null) {
            dispose();
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                new AdminDashboard().setVisible(true);
            } else {
                new VoterDashboard(user).setVisible(true);
            }
        } else {
            showStatus("Invalid username or password.", true);
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.\nPlease verify your credentials and try again.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = roleSelector.getSelectedRole();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Fields cannot be empty.", true);
            JOptionPane.showMessageDialog(this,
                    "Please fill in both the username and password fields.",
                    "Fields Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.length() < 3) {
            showStatus("Username must be at least 3 characters.", true);
            JOptionPane.showMessageDialog(this,
                    "Username must be at least 3 characters long.",
                    "Username Too Short",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.registerUser(username, password, role)) {
            JOptionPane.showMessageDialog(this,
                    "Registration successful!\nYou can now sign in with your username: " + username,
                    "Account Created",
                    JOptionPane.INFORMATION_MESSAGE);
            updateMode(false);
            usernameField.setText(username);
            passwordField.setText("");
            showStatus("Registration successful! Please sign in.", false);
            passwordField.requestFocusInWindow();
        } else {
            showStatus("Registration failed. Username may already exist.", true);
            JOptionPane.showMessageDialog(this,
                    "Registration failed.\nThis username may already be registered. Please choose another username.",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Theme.STATUS_ERROR : Theme.STATUS_SUCCESS);
    }
}