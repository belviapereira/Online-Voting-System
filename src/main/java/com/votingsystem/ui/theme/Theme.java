package com.votingsystem.ui.theme;

import java.awt.*;

public class Theme {
    // Canvas & Surfaces
    public static final Color CANVAS_BG = new Color(0xEF, 0xF1, 0xFA); // Soft periwinkle canvas
    public static final Color CARD_BG = Color.WHITE;
    public static final Color CARD_BORDER = new Color(0xDC, 0xE1, 0xF2);
    public static final Color CARD_SHADOW = new Color(0x1A, 0x23, 0x52, 18);

    // Brand Colors
    public static final Color PRIMARY_NAVY = new Color(0x1A, 0x23, 0x52);
    public static final Color BRAND_GRADIENT_START = new Color(0x3C, 0x4D, 0xAE);
    public static final Color BRAND_GRADIENT_END = new Color(0x5C, 0x6B, 0xC8);
    public static final Color BRAND_ACCENT = new Color(0x43, 0x55, 0xB9);
    public static final Color BRAND_LIGHT = new Color(0xEE, 0xF1, 0xFC);

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(0x1A, 0x23, 0x52);
    public static final Color TEXT_SECONDARY = new Color(0x7D, 0x87, 0xA6);
    public static final Color TEXT_MUTED = new Color(0xA4, 0xAD, 0xC4);

    // Form Controls
    public static final Color INPUT_BG = new Color(0xF7, 0xF9, 0xFD);
    public static final Color INPUT_BORDER = new Color(0xD8, 0xDD, 0xEE);
    public static final Color INPUT_FOCUS_BORDER = new Color(0x3C, 0x4D, 0xAE);
    public static final Color BUTTON_BORDER = new Color(0xCC, 0xD3, 0xF0);

    // Status Badges
    public static final Color STATUS_SUCCESS = new Color(0x2E, 0x7D, 0x32);
    public static final Color STATUS_ERROR = new Color(0xD3, 0x2F, 0x2F);

    // Typography
    private static final String FONT_NAME = "Segoe UI";

    public static Font fontHero() {
        return new Font(FONT_NAME, Font.BOLD, 28);
    }

    public static Font fontTitle() {
        return new Font(FONT_NAME, Font.BOLD, 22);
    }

    public static Font fontSection() {
        return new Font(FONT_NAME, Font.BOLD, 17);
    }

    public static Font fontSubheading() {
        return new Font(FONT_NAME, Font.BOLD, 15);
    }

    public static Font fontBody() {
        return new Font(FONT_NAME, Font.PLAIN, 13);
    }

    public static Font fontBodyBold() {
        return new Font(FONT_NAME, Font.BOLD, 13);
    }

    public static Font fontCaption() {
        return new Font(FONT_NAME, Font.PLAIN, 12);
    }

    public static Font fontButton() {
        return new Font(FONT_NAME, Font.BOLD, 14);
    }

    public static void applyRenderHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
