package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SegmentedRoleSelector extends JPanel {
    private String selectedRole = "VOTER";
    private JButton voterBtn;
    private JButton adminBtn;

    public SegmentedRoleSelector() {
        setLayout(new GridLayout(1, 2, 8, 0));
        setOpaque(false);

        voterBtn = createSegmentButton("Voter", "VOTER");
        adminBtn = createSegmentButton("Admin", "ADMIN");

        add(voterBtn);
        add(adminBtn);

        updateStyles();
    }

    private JButton createSegmentButton(String label, String role) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyRenderHints(g2);

                int w = getWidth();
                int h = getHeight();
                int radius = h;

                boolean isSelected = selectedRole.equals(role);
                RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, w, h, radius, radius);

                if (isSelected) {
                    GradientPaint gp = new GradientPaint(0, 0, Theme.BRAND_GRADIENT_START, w, 0, Theme.BRAND_GRADIENT_END);
                    g2.setPaint(gp);
                    g2.fill(shape);
                    setForeground(Color.WHITE);
                } else {
                    g2.setColor(Theme.INPUT_BG);
                    g2.fill(shape);
                    g2.setColor(Theme.INPUT_BORDER);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius, radius));
                    setForeground(Theme.TEXT_SECONDARY);
                }

                // Text
                g2.setFont(Theme.fontBodyBold());
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(getText());
                int th = fm.getAscent();
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

        btn.addActionListener(e -> {
            selectedRole = role;
            updateStyles();
        });

        return btn;
    }

    private void updateStyles() {
        voterBtn.repaint();
        adminBtn.repaint();
    }

    public String getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(String role) {
        this.selectedRole = role;
        updateStyles();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(240, 38);
    }
}
