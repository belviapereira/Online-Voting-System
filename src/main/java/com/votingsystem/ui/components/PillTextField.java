package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class PillTextField extends JTextField {
    private String placeholder = "";
    private boolean isFocused = false;

    public PillTextField(int columns) {
        super(columns);
        init();
    }

    public PillTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        init();
    }

    private void init() {
        setFont(Theme.fontBody());
        setForeground(Theme.TEXT_PRIMARY);
        setCaretColor(Theme.BRAND_ACCENT);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyRenderHints(g2);

        int width = getWidth();
        int height = getHeight();
        int radius = height; // Capsule

        // Draw background
        g2.setColor(Theme.INPUT_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, radius, radius));

        // Draw border
        g2.setColor(isFocused ? Theme.INPUT_FOCUS_BORDER : Theme.INPUT_BORDER);
        g2.setStroke(new BasicStroke(isFocused ? 1.8f : 1.2f));
        g2.draw(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, radius, radius));

        super.paintComponent(g);

        // Draw placeholder if text is empty and not focused
        if (getText().isEmpty() && !placeholder.isEmpty() && !isFocused) {
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(Theme.fontBody());
            FontMetrics fm = g2.getFontMetrics();
            int y = (height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(placeholder, 18, y);
        }

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 42));
    }
}
