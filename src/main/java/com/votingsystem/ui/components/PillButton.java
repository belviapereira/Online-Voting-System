package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class PillButton extends JButton {
    public enum Style {
        GRADIENT,
        OUTLINE,
        SECONDARY,
        DARK,
        PRIMARY  // Alias for GRADIENT — used by admin/search clear buttons
    }

    private Style style;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public PillButton(String text) {
        this(text, Style.GRADIENT);
    }

    public PillButton(String text, Style style) {
        super(text);
        this.style = style;
        setFont(Theme.fontButton());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

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

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setStyle(Style style) {
        this.style = style;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyRenderHints(g2);

        int width = getWidth();
        int height = getHeight();
        int radius = height; // Full capsule radius

        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, width, height, radius, radius);

        switch (style) {
            case GRADIENT -> {
                Color start = isPressed ? Theme.BRAND_GRADIENT_START.darker() :
                              isHovered ? Theme.BRAND_GRADIENT_START.brighter() : Theme.BRAND_GRADIENT_START;
                Color end = isPressed ? Theme.BRAND_GRADIENT_END.darker() :
                            isHovered ? Theme.BRAND_GRADIENT_END.brighter() : Theme.BRAND_GRADIENT_END;
                GradientPaint gp = new GradientPaint(0, 0, start, width, 0, end);
                g2.setPaint(gp);
                g2.fill(shape);
                setForeground(Color.WHITE);
            }
            case OUTLINE -> {
                if (isHovered) {
                    g2.setColor(Theme.BRAND_LIGHT);
                    g2.fill(shape);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fill(shape);
                }
                g2.setColor(isHovered ? Theme.BRAND_ACCENT : Theme.BUTTON_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, radius, radius));
                setForeground(Theme.BRAND_ACCENT);
            }
            case SECONDARY -> {
                Color bg = isPressed ? Theme.BRAND_LIGHT.darker() :
                           isHovered ? new Color(0xE2, 0xE7, 0xFA) : Theme.BRAND_LIGHT;
                g2.setColor(bg);
                g2.fill(shape);
                setForeground(Theme.BRAND_ACCENT);
            }
            case DARK -> {
                Color bg = isPressed ? Theme.PRIMARY_NAVY.darker() :
                           isHovered ? new Color(0x27, 0x33, 0x6E) : Theme.PRIMARY_NAVY;
                g2.setColor(bg);
                g2.fill(shape);
                setForeground(Color.WHITE);
            }
            case PRIMARY -> {
                // Renders like GRADIENT but with solid BRAND_ACCENT fill — used for emphasis actions
                Color bg = isPressed ? Theme.BRAND_ACCENT.darker() :
                           isHovered ? Theme.BRAND_GRADIENT_START.brighter() : Theme.BRAND_ACCENT;
                g2.setColor(bg);
                g2.fill(shape);
                setForeground(Color.WHITE);
            }
        }

        // Draw Text
        FontMetrics fm = g2.getFontMetrics(getFont());
        int textWidth = fm.stringWidth(getText());
        int textHeight = fm.getAscent();
        int x = (width - textWidth) / 2;
        int y = (height + textHeight) / 2 - 2;

        g2.setFont(getFont());
        g2.setColor(getForeground());
        g2.drawString(getText(), x, y);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 36, 120), Math.max(d.height + 16, 42));
    }
}
