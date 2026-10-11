package com.alm.ui.theme;

import java.awt.*;

/**
 * Central design tokens for the Airport Lounge Manager UI.
 * Clean white/light theme with a professional blue accent (#1976D2).
 * NOTE: This file uses only ASCII characters to avoid Windows-1252 encoding
 * issues.
 */
public final class ALMTheme {

    private ALMTheme() {
    }

    // -------------------------------------------------------------------------
    // Core Backgrounds
    // -------------------------------------------------------------------------
    public static final Color BG_DEEP = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    public static final Color BG_DARK = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF (alias)
    public static final Color BG_DEEPEST = new Color(0xF8, 0xF9, 0xFA); // #F8F9FA sidebar
    public static final Color BG_CARD = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF card
    public static final Color BG_CARD_HOVER = new Color(0xF0, 0xF4, 0xFF); // #F0F4FF hover
    public static final Color BG_INPUT = new Color(0xF8, 0xF9, 0xFA); // #F8F9FA input

    // -------------------------------------------------------------------------
    // Accent Colors
    // -------------------------------------------------------------------------
    public static final Color ACCENT_BLUE = new Color(0x19, 0x76, 0xD2); // #1976D2
    public static final Color ACCENT_BLUE_HOVER = new Color(0x15, 0x65, 0xC0); // #1565C0
    public static final Color ACCENT_BLUE_LIGHT = new Color(0xE3, 0xF2, 0xFD); // #E3F2FD
    public static final Color ACCENT_INDIGO = new Color(0x39, 0x49, 0xAB); // #3949AB
    public static final Color ACCENT_PURPLE = new Color(0x7B, 0x1F, 0xA2); // #7B1FA2
    public static final Color ACCENT_CYAN = new Color(0x00, 0x97, 0xA7); // #0097A7

    // -------------------------------------------------------------------------
    // Status Colors
    // -------------------------------------------------------------------------
    public static final Color SUCCESS = new Color(0x2E, 0x7D, 0x32); // #2E7D32
    public static final Color WARNING = new Color(0xF5, 0x7C, 0x00); // #F57C00
    public static final Color DANGER = new Color(0xC6, 0x28, 0x28); // #C62828
    public static final Color INFO = new Color(0x01, 0x57, 0x9B); // #01579B

    // -------------------------------------------------------------------------
    // Text Colors
    // -------------------------------------------------------------------------
    public static final Color TEXT_PRIMARY = new Color(0x21, 0x21, 0x21); // #212121
    public static final Color TEXT_SECONDARY = new Color(0x61, 0x61, 0x61); // #616161
    public static final Color TEXT_MUTED = new Color(0x9E, 0x9E, 0x9E); // #9E9E9E
    public static final Color TEXT_ACCENT = new Color(0x19, 0x76, 0xD2); // #1976D2
    public static final Color TEXT_ON_ACCENT = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF

    // -------------------------------------------------------------------------
    // Chart Colors
    // -------------------------------------------------------------------------
    public static final Color CHART_GREEN = new Color(0x43, 0xA0, 0x47); // #43A047
    public static final Color CHART_BLUE = new Color(0x19, 0x76, 0xD2); // #1976D2
    public static final Color CHART_YELLOW = new Color(0xFB, 0xC0, 0x2D); // #FBC02D
    public static final Color CHART_PURPLE = new Color(0x7B, 0x1F, 0xA2); // #7B1FA2
    public static final Color CHART_ORANGE = new Color(0xF5, 0x7C, 0x00); // #F57C00
    public static final Color CHART_PINK = new Color(0xC2, 0x18, 0x5B); // #C2185B

    // -------------------------------------------------------------------------
    // Borders and Dividers
    // -------------------------------------------------------------------------
    public static final Color BORDER = new Color(0xE0, 0xE0, 0xE0); // #E0E0E0
    public static final Color BORDER_LIGHT = new Color(0xEE, 0xEE, 0xEE); // #EEEEEE
    public static final Color GLOW_BLUE = new Color(0x19, 0x76, 0xD2, 30); // translucent

    // -------------------------------------------------------------------------
    // Dimensions
    // -------------------------------------------------------------------------
    public static final int SIDEBAR_WIDTH = 210;
    public static final int TOPBAR_HEIGHT = 56;
    public static final int CARD_RADIUS = 12;
    public static final int CARD_PADDING = 20;

    // -------------------------------------------------------------------------
    // Flat Icon Set
    // All characters specified as Java Unicode escapes (no raw non-ASCII bytes).
    // -------------------------------------------------------------------------
    public static final String ICON_HOME = "\u2302"; // house
    public static final String ICON_DASHBOARD = "\u22A0"; // grid box
    public static final String ICON_KEYBOARD = "\u2328"; // keyboard
    public static final String ICON_DOCUMENT = "\u2751"; // document
    public static final String ICON_LOUNGE = "\u2663"; // club / lounge
    public static final String ICON_PERSON = "\u25D4"; // person
    public static final String ICON_PEOPLE = "\u25CF"; // group
    public static final String ICON_LOCK = "\u26BF"; // lock
    public static final String ICON_CHART = "\u2767"; // analytics
    public static final String ICON_MONEY = "\u0024"; // dollar
    public static final String ICON_MAIL = "\u2709"; // envelope
    public static final String ICON_STAFF = "\u2767"; // staff
    public static final String ICON_SETTINGS = "\u2699"; // gear
    public static final String ICON_BUILDING = "\u2302"; // building
    public static final String ICON_BELL = "\u2605"; // bell / star
    public static final String ICON_CALENDAR = "\u25A6"; // calendar
    public static final String ICON_LOGOUT = "\u21B1"; // exit

    // -------------------------------------------------------------------------
    // Fonts
    // -------------------------------------------------------------------------
    public static Font fontBold(float size) {
        return new Font("Segoe UI", Font.BOLD, (int) size);
    }

    public static Font fontRegular(float size) {
        return new Font("Segoe UI", Font.PLAIN, (int) size);
    }

    public static Font fontMono(float size) {
        return new Font("Consolas", Font.PLAIN, (int) size);
    }

    /** Returns a copy of the color with a specific alpha (0-255). */
    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}
