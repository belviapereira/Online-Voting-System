package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class CircularAvatar extends JComponent {
    private final int diameter;
    private final String name;

    public CircularAvatar(String name, int diameter) {
        this.name = name != null && !name.isEmpty() ? name : "User";
        this.diameter = diameter;
        setPreferredSize(new Dimension(diameter, diameter));
        setMaximumSize(new Dimension(diameter, diameter));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyRenderHints(g2);

        int d = Math.min(getWidth(), getHeight());
        int x = (getWidth() - d) / 2;
        int y = (getHeight() - d) / 2;

        // Circular background
        g2.setColor(new Color(0xE5, 0xE9, 0xFA));
        g2.fill(new Ellipse2D.Float(x, y, d, d));

        // Border ring
        g2.setColor(new Color(0xCC, 0xD4, 0xF2));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Float(x + 1, y + 1, d - 2, d - 2));

        // Stylized head/shoulders or initials
        if (d >= 48) {
            // Stylized illustrated avatar (sunglasses & hairstyle like Behance)
            int cx = x + d / 2;
            int cy = y + d / 2;

            // Head
            g2.setColor(new Color(0xF7, 0xD2, 0xB7)); // Skin tone
            g2.fill(new Ellipse2D.Float(cx - 11, cy - 13, 22, 22));

            // Hair
            g2.setColor(new Color(0x3B, 0x2A, 0x22)); // Dark hair
            g2.fillArc(cx - 12, cy - 18, 24, 20, 0, 180);

            // Sunglasses
            g2.setColor(Theme.PRIMARY_NAVY);
            g2.fill(new RoundRectangle2D.Float(cx - 9, cy - 6, 8, 5, 2, 2));
            g2.fill(new RoundRectangle2D.Float(cx + 1, cy - 6, 8, 5, 2, 2));
            g2.fillRect(cx - 2, cy - 5, 4, 2);

            // Torso / shirt
            g2.setColor(Theme.BRAND_ACCENT);
            g2.fillArc(cx - 16, cy + 5, 32, 28, 0, 180);
        } else {
            // Initials for smaller avatar chips
            g2.setColor(Theme.BRAND_ACCENT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (d * 0.45)));
            FontMetrics fm = g2.getFontMetrics();
            String initial = name.substring(0, 1).toUpperCase();
            int tw = fm.stringWidth(initial);
            int th = fm.getAscent();
            g2.drawString(initial, x + (d - tw) / 2, y + (d + th) / 2 - 2);
        }

        g2.dispose();
    }
}
