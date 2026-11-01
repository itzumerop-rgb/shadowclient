package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.ClientSettings;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Anim;
import dev.shadowclient.util.Draw;
import net.minecraft.client.gui.DrawContext;

public class Keystrokes extends HudModule {
    private final Setting.Bool showSpace = add(new Setting.Bool("space", "Show Space", true));
    private final Setting.Bool showShift = add(new Setting.Bool("shift", "Show Shift", true));
    private final Setting.Bool showSprint = add(new Setting.Bool("sprint", "Show Sprint", true));
    private final Anim[] a = new Anim[7];
    private static final int K = 18, G = 2;

    public Keystrokes() {
        super("keystrokes", "Keystrokes", "Shows WASD, Space, Shift and Sprint presses.", "keys", ModuleCategory.PVP, true, 0.008f, 0.60f);
        for (int i = 0; i < a.length; i++) a[i] = new Anim(0f);
    }

    private void key(DrawContext c, int i, boolean down, String label, int x, int y, int kw, int kh, float ts) {
        float t = a[i].to(down ? 1f : 0f, 22f);
        int acc = ClientSettings.accent();
        int fill = Draw.lerp(0xA0101012, Draw.withAlpha(acc, 0.85f), t);
        Draw.rrect(c, x, y, kw, kh, 3, fill);
        Draw.border(c, x, y, kw, kh, 3, Draw.lerp(0xFF2A2B31, acc, t));
        int tw = (int) (Draw.w(label) * ts);
        Draw.textScaled(c, label, x + (kw - tw) / 2, y + (int) ((kh - 8 * ts) / 2f) + (ts < 1f ? 0 : 1), color(), ts, false);
    }

    @Override public void draw(DrawContext c, boolean preview) {
        boolean f = false, l = false, b = false, r = false, j = false, s = false, sp = false;
        if (preview) { f = true; }
        else if (mc.player != null) {
            var o = mc.options;
            f = o.forwardKey.isPressed(); l = o.leftKey.isPressed(); b = o.backKey.isPressed(); r = o.rightKey.isPressed();
            j = o.jumpKey.isPressed(); s = o.sneakKey.isPressed(); sp = mc.player.isSprinting();
        }
        int step = K + G, total = 3 * K + 2 * G, y = 0;
        key(c, 0, f, "W", step, y, K, K, 1f);
        y += step;
        key(c, 1, l, "A", 0, y, K, K, 1f); key(c, 2, b, "S", step, y, K, K, 1f); key(c, 3, r, "D", 2 * step, y, K, K, 1f);
        y += step;
        if (showSpace.value) { key(c, 4, j, "SPACE", 0, y, total, 12, 0.8f); y += 14; }
        if (showShift.value || showSprint.value) {
            int half = (total - G) / 2;
            if (showShift.value && showSprint.value) { key(c, 5, s, "SHIFT", 0, y, half, 12, 0.7f); key(c, 6, sp, "SPRINT", half + G, y, half, 12, 0.7f); }
            else if (showShift.value) key(c, 5, s, "SHIFT", 0, y, total, 12, 0.7f);
            else key(c, 6, sp, "SPRINT", 0, y, total, 12, 0.7f);
            y += 14;
        }
        w = total; h = y - 2;
    }
}
