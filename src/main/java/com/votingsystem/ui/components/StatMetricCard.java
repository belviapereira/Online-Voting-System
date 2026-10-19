package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class StatMetricCard extends JPanel {
    public StatMetricCard(int count, String label) {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // Circular badge component
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyRenderHints(g2);

                int d = Math.min(getWidth(), getHeight()) - 4;
                int x = 2;
                int y = 2;

                // White circle with dark navy border
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Float(x, y, d, d));

                g2.setColor(Theme.PRIMARY_NAVY);
                g2.setStroke(new BasicStroke(2.2f));
                g2.draw(new Ellipse2D.Float(x, y, d, d));

                // Number text
                String text = String.valueOf(count);
                Font font = new Font("Segoe UI", Font.BOLD, text.length() > 3 ? 12 : 14);
                g2.setFont(font);
                g2.setColor(Theme.PRIMARY_NAVY);

                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(text);
                int th = fm.getAscent();
                g2.drawString(text, x + (d - tw) / 2, y + (d + th) / 2 - 2);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(42, 42);
            }
        };

        // Two-line text container to avoid HTML layout truncations
        JPanel textContainer = new JPanel();
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
        textContainer.setOpaque(false);

        String line1 = label;
        String line2 = "";
        if (label.contains("registered voters")) {
            line1 = "Total number of";
            line2 = "registered voters";
        } else if (label.contains("votes")) {
            line1 = "Total number of";
            line2 = "votes cast";
        } else if (label.contains("registered candidates")) {
            line1 = "Total number of";
            line2 = "registered candidates";
        }

        JLabel l1 = new JLabel(line1);
        l1.setFont(Theme.fontCaption());
        l1.setForeground(Theme.TEXT_SECONDARY);

        JLabel l2 = new JLabel(line2);
        l2.setFont(Theme.fontCaption());
        l2.setForeground(Theme.TEXT_SECONDARY);

        textContainer.add(Box.createVerticalStrut(4));
        textContainer.add(l1);
        if (!line2.isEmpty()) {
            textContainer.add(l2);
        }

        add(badge, BorderLayout.WEST);
        add(textContainer, BorderLayout.CENTER);
    }
}
