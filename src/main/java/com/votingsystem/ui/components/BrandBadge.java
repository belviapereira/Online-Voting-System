package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BrandBadge extends JComponent {
    private int height;

    public BrandBadge() {
        this(38);
    }

    public BrandBadge(int height) {
        this.height = height;
        setPreferredSize(new Dimension((int) (height * 2.6), height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyRenderHints(g2);

        int w = getWidth();
        int h = getHeight();
        int radius = h;

        // Pill background
        GradientPaint gp = new GradientPaint(0, 0, Theme.PRIMARY_NAVY, w, 0, Theme.BRAND_GRADIENT_START);
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

        // Draw "iVOTE" text
        Font font = new Font("Segoe UI", Font.BOLD, (int) (h * 0.48));
        g2.setFont(font);
        g2.setColor(Color.WHITE);

        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth("iVOTE");
        int th = fm.getAscent();

        int x = (w - tw) / 2;
        int y = (h + th) / 2 - 2;

        g2.drawString("iVOTE", x, y);

        g2.dispose();
    }
}
