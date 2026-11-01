package dev.shadowclient.util;

import dev.shadowclient.settings.ClientSettings;

public final class Theme {
    private Theme() { }
    public static final int PANEL = 0xEE0F1013, CARD = 0xFF16171B, CARD_H = 0xFF1E1F25, BTN = 0xFF1B1C21, BTN_H = 0xFF272830,
            BORDER = 0xFF282930, TEXT = 0xFFFFFFFF, DIM = 0xFF9A9BA3, OFF = 0xFF2A2B30, ICON_OFF = 0xFF5C5D65, ROW = 0xFF14151A;
    public static int accent() { return ClientSettings.accent(); }
    public static int accentSoft() { return Draw.lerp(accent(), 0xFFFFFFFF, 0.35f); }
}
