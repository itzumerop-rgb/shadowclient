package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;

public class ClockDisplay extends HudModule {
    private final Setting.Bool h24 = add(new Setting.Bool("h24", "24-Hour Format", false));
    private final Setting.Bool seconds = add(new Setting.Bool("seconds", "Show Seconds", false));
    private static final DateTimeFormatter[] F = {
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH), DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH), DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)};
    private long lastSec = -1; private int lastFmt = -1; private String str = "";

    public ClockDisplay() { super("clock", "Custom Clock", "Shows your local time.", "clock", ModuleCategory.HUD, false, 0.92f, 0.012f); }

    @Override public void draw(DrawContext c, boolean preview) {
        long sec = System.currentTimeMillis() / 1000L;
        int fmt = (h24.value ? 2 : 0) + (seconds.value ? 1 : 0);
        if (sec != lastSec || fmt != lastFmt) { lastSec = sec; lastFmt = fmt; str = LocalTime.now().format(F[fmt]); }
        label(c, str);
    }
}
