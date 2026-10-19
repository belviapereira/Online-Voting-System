package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class HeaderIcon extends JComponent {
    public enum Type {
        HAMBURGER,
        BELL,
        HELP
    }

    private final Type type;
    private final int size;
    private boolean isHovered = false;
    private boolean showBadge = false;

    public HeaderIcon(Type type, int size) {
        this.type = type;
        this.size = size;
        setPreferredSize(new Dimension(size + 6, size + 6));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setShowBadge(boolean show) {
        this.showBadge = show;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyRenderHints(g2);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        Color iconColor = isHovered ? Theme.BRAND_ACCENT : Theme.PRIMARY_NAVY;
        g2.setColor(iconColor);

        switch (type) {
            case HAMBURGER -> {
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int lineW = (int) (size * 0.7);
                int left = cx - lineW / 2;
                g2.drawLine(left, cy - 6, left + lineW, cy - 6);
                g2.drawLine(left, cy, left + lineW, cy);
                g2.drawLine(left, cy + 6, left + lineW, cy + 6);
            }
            case BELL -> {
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Bell dome
                Path2D.Float bell = new Path2D.Float();
                bell.moveTo(cx - 6, cy + 4);
                bell.curveTo(cx - 6, cy - 2, cx - 4, cy - 7, cx, cy - 7);
                bell.curveTo(cx + 4, cy - 7, cx + 6, cy - 2, cx + 6, cy + 4);
                bell.closePath();
                g2.draw(bell);

                // Bell rim & clapper
                g2.drawLine(cx - 8, cy + 5, cx + 8, cy + 5);
                g2.fill(new Ellipse2D.Float(cx - 2, cy + 6, 4, 3));

                if (showBadge) {
                    g2.setColor(Theme.STATUS_ERROR);
                    g2.fill(new Ellipse2D.Float(cx + 3, cy - 8, 7, 7));
                }
            }
            case HELP -> {
                // Circle border
                g2.setStroke(new BasicStroke(1.8f));
                int d = (int) (size * 0.85);
                float circleX = cx - (float) d / 2;
                float circleY = cy - (float) d / 2;
                g2.draw(new Ellipse2D.Float(circleX, circleY, d, d));

                // Pixel-perfect centered "?" glyph using visual ink bounds
                Font helpFont = new Font("Segoe UI", Font.BOLD, (int) (size * 0.52));
                g2.setFont(helpFont);

                GlyphVector gv = helpFont.createGlyphVector(g2.getFontRenderContext(), "?");
                Rectangle2D visualBounds = gv.getVisualBounds();
                float tx = (float) (cx - visualBounds.getWidth() / 2 - visualBounds.getX());
                float ty = (float) (cy - visualBounds.getHeight() / 2 - visualBounds.getY());

                g2.drawString("?", tx, ty);
            }
        }

        g2.dispose();
    }
}
