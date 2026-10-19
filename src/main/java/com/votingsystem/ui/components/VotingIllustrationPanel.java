package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class VotingIllustrationPanel extends JPanel {
    public VotingIllustrationPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyRenderHints(g2);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2 + 10;

        // Background decorative circles
        g2.setColor(new Color(0xE0, 0xE5, 0xF9, 140));
        g2.fill(new Ellipse2D.Float(cx - 100, cy - 90, 200, 200));

        g2.setColor(new Color(0xD4, 0xDC, 0xF8, 100));
        g2.fill(new Ellipse2D.Float(cx + 20, cy - 100, 80, 80));

        // Ballot Box Base
        int boxW = 140;
        int boxH = 100;
        int boxX = cx - boxW / 2;
        int boxY = cy - 20;

        // Box shadow
        g2.setColor(new Color(0x2B, 0x3B, 0x82, 30));
        g2.fill(new RoundRectangle2D.Float(boxX + 4, boxY + 8, boxW, boxH, 16, 16));

        // Box body (Gradient navy/indigo)
        GradientPaint boxPaint = new GradientPaint(boxX, boxY, Theme.PRIMARY_NAVY, boxX + boxW, boxY + boxH, Theme.BRAND_GRADIENT_START);
        g2.setPaint(boxPaint);
        g2.fill(new RoundRectangle2D.Float(boxX, boxY, boxW, boxH, 16, 16));

        // Box slot on top
        g2.setColor(new Color(0x13, 0x1A, 0x40));
        g2.fill(new RoundRectangle2D.Float(cx - 35, boxY + 12, 70, 8, 8, 8));

        // Ballot paper inserting into the box
        int paperW = 80;
        int paperH = 65;
        int paperX = cx - paperW / 2;
        int paperY = boxY - 38;

        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(paperX, paperY, paperW, paperH, 8, 8));
        g2.setColor(Theme.CARD_BORDER);
        g2.draw(new RoundRectangle2D.Float(paperX, paperY, paperW, paperH, 8, 8));

        // Ballot lines
        g2.setColor(new Color(0xCC, 0xD4, 0xEE));
        g2.fill(new RoundRectangle2D.Float(paperX + 12, paperY + 14, 56, 4, 2, 2));
        g2.fill(new RoundRectangle2D.Float(paperX + 12, paperY + 24, 40, 4, 2, 2));

        // Checkmark badge on ballot
        int badgeX = paperX + 46;
        int badgeY = paperY + 28;
        g2.setColor(Theme.BRAND_GRADIENT_START);
        g2.fill(new Ellipse2D.Float(badgeX, badgeY, 22, 22));

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Float check = new Path2D.Float();
        check.moveTo(badgeX + 6, badgeY + 11);
        check.lineTo(badgeX + 9.5f, badgeY + 15);
        check.lineTo(badgeX + 16, badgeY + 7);
        g2.draw(check);

        // Security Shield Badge on bottom right of the box
        int shieldX = boxX + boxW - 22;
        int shieldY = boxY + boxH - 24;
        g2.setColor(new Color(0x2E, 0x7D, 0x32)); // Green verified badge
        g2.fill(new Ellipse2D.Float(shieldX, shieldY, 26, 26));
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Float shieldCheck = new Path2D.Float();
        shieldCheck.moveTo(shieldX + 7, shieldY + 13);
        shieldCheck.lineTo(shieldX + 11, shieldY + 17);
        shieldCheck.lineTo(shieldX + 19, shieldY + 9);
        g2.draw(shieldCheck);

        g2.dispose();
    }
}
