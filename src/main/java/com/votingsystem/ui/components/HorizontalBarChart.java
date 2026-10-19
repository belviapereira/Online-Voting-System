package com.votingsystem.ui.components;

import com.votingsystem.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

public class HorizontalBarChart extends JPanel {
    private Map<String, Integer> data;

    public HorizontalBarChart(Map<String, Integer> data) {
        this.data = data;
        setOpaque(false);
        setPreferredSize(new Dimension(380, 180));
    }

    public void setData(Map<String, Integer> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyRenderHints(g2);

        int w = getWidth();
        int h = getHeight();

        if (data == null || data.isEmpty()) {
            g2.setFont(Theme.fontBody());
            g2.setColor(Theme.TEXT_MUTED);
            String msg = "No votes recorded yet for this election.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        int totalVotes = data.values().stream().mapToInt(Integer::intValue).sum();
        int maxVotes = Math.max(1, data.values().stream().mapToInt(Integer::intValue).max().orElse(1));

        int labelWidth = 110;
        int barStartX = labelWidth + 10;
        int barMaxWidth = w - barStartX - 50;
        int barHeight = 18;
        int startY = 15;
        int gap = 34;

        int index = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int y = startY + (index * gap);
            int votes = entry.getValue();
            double pct = totalVotes > 0 ? (double) votes / totalVotes * 100 : 0;
            int barWidth = maxVotes > 0 ? (int) ((double) votes / maxVotes * barMaxWidth) : 0;
            barWidth = Math.max(barWidth, 6); // Minimum visible sliver

            // Candidate Label
            g2.setFont(Theme.fontBody());
            g2.setColor(Theme.TEXT_PRIMARY);
            String name = entry.getKey();
            if (name.length() > 14) name = name.substring(0, 12) + "...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(name, 5, y + fm.getAscent() - 2);

            // Bar Background Track
            g2.setColor(new Color(0xEE, 0xF1, 0xFA));
            g2.fill(new RoundRectangle2D.Float(barStartX, y, barMaxWidth, barHeight, barHeight, barHeight));

            // Bar Fill (Pastel Indigo Gradient)
            GradientPaint barPaint = new GradientPaint(
                    barStartX, y, new Color(0x7D, 0x8C, 0xD9),
                    barStartX + barWidth, y, new Color(0x43, 0x55, 0xB9)
            );
            g2.setPaint(barPaint);
            g2.fill(new RoundRectangle2D.Float(barStartX, y, barWidth, barHeight, barHeight, barHeight));

            // Percentage Text
            String pctText = String.format("%.0f%%", pct);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            if (barWidth > 35) {
                g2.setColor(Color.WHITE);
                g2.drawString(pctText, barStartX + barWidth - 30, y + 13);
            } else {
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.drawString(pctText, barStartX + barWidth + 6, y + 13);
            }

            // Votes count to the right
            g2.setFont(Theme.fontCaption());
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(votes + " v", w - 38, y + 13);

            index++;
            if (index >= 4) break; // Display top 4 in compact widget
        }

        // X-Axis Axis line & scale
        int axisY = startY + (Math.min(data.size(), 4) * gap) + 6;
        g2.setColor(Theme.CARD_BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawLine(barStartX, axisY, barStartX + barMaxWidth, axisY);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.setColor(Theme.TEXT_MUTED);
        int[] ticks = {0, maxVotes / 4, maxVotes / 2, (maxVotes * 3) / 4, maxVotes};
        for (int tick : ticks) {
            int tx = barStartX + (maxVotes > 0 ? (int) ((double) tick / maxVotes * barMaxWidth) : 0);
            g2.drawString(String.valueOf(tick), tx - 4, axisY + 12);
        }

        g2.drawString("No. of Votes", barStartX + (barMaxWidth / 2) - 20, axisY + 24);

        g2.dispose();
    }
}
