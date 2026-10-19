package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;
    private Color borderColor;
    private boolean hasShadow;

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, null, false);
    }

    public RoundedPanel(int radius, Color bgColor, Color borderColor, boolean hasShadow) {
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.borderColor = borderColor;
        this.hasShadow = hasShadow;
        setOpaque(false);
    }

    public void setBackgroundColor(Color bgColor) {
        this.backgroundColor = bgColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyRenderHints(g2);

        int shadowOffset = hasShadow ? 3 : 0;
        int shadowBlur = hasShadow ? 6 : 0;
        int x = shadowBlur / 2;
        int y = shadowBlur / 2;
        int width = getWidth() - shadowBlur;
        int height = getHeight() - shadowBlur - shadowOffset;

        if (hasShadow) {
            g2.setColor(Theme.CARD_SHADOW);
            g2.fill(new RoundRectangle2D.Float(x, y + shadowOffset, width, height, cornerRadius, cornerRadius));
        }

        // Draw background
        g2.setColor(backgroundColor != null ? backgroundColor : Theme.CARD_BG);
        g2.fill(new RoundRectangle2D.Float(x, y, width, height, cornerRadius, cornerRadius));

        // Draw border if set
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, cornerRadius, cornerRadius));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
