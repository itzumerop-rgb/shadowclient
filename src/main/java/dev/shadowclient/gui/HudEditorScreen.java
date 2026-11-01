package dev.shadowclient.gui;

import dev.shadowclient.HudManager;
import dev.shadowclient.config.ConfigManager;
import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleManager;
import dev.shadowclient.settings.ClientSettings;
import dev.shadowclient.util.*;
import net.minecraft.client.gui.DrawContext;

/**
 * HUD editor: drag widgets, resize from the corner handle, snap to grid / edges / centre, hide/show (red minus),
 * per-widget settings (gear), preview mode, scale controls and reset. Positions save when the screen closes.
 */
public class HudEditorScreen extends ShadowScreen {
    private final Anim open = new Anim(0f);
    private boolean closing, previewMode, help;
    private HudModule selected, drag;
    private boolean resizing;
    private float offX, offY, tx, ty;
    private float guideX = -1, guideY = -1;
    private final int[] rb = new int[4];
    private final int[] rMain = new int[4], rSnap = new int[4], rPrev = new int[4], rReset = new int[4], rMenu = new int[4], rHelp = new int[4],
            tbMinus = new int[4], tbPlus = new int[4], tbHide = new int[4], tbSet = new int[4], tbBox = new int[4];
    private String hoverTip = "";

    public HudEditorScreen() { super("Shadow HUD Editor"); }

    @Override protected void init() {
        int cx = width / 2, cy = height / 2;
        set(rMain, cx - 62, cy - 4, 124, 20);
        int bx = cx - (4 * 22 + 3 * 4) / 2;
        set(rSnap, bx, cy + 22, 22, 22); set(rPrev, bx + 26, cy + 22, 22, 22);
        set(rReset, bx + 52, cy + 22, 22, 22); set(rMenu, bx + 78, cy + 22, 22, 22);
        set(rHelp, 8, height - 36, 28, 28);
    }

    private void bounds(HudModule m) {
        rb[0] = m.pixelX(width); rb[1] = m.pixelY(height); rb[2] = m.boxW(); rb[3] = m.boxH();
    }

    // ------------------------------------------------------------ render
    @Override public void render(DrawContext c, int mx, int my, float delta) {
        Anim.frame();
        super.render(c, mx, my, delta);
        float a = open.to(closing ? 0f : 1f, 16f);
        if (closing && a <= 0.02f) { client.setScreen(null); return; }
        int acc = Theme.accent();

        // smooth dragging towards the target position
        if (drag != null && !resizing) {
            float k = ClientSettings.anim() ? 1f - (float) Math.exp(-32 * Anim.dt) : 1f;
            float cx = drag.pixelX(width), cy = drag.pixelY(height);
            cx += (tx - cx) * k; cy += (ty - cy) * k;
            drag.setPixel(Math.round(cx), Math.round(cy), width, height);
        }

        hoverTip = "";
        HudModule[] huds = ModuleManager.huds();
        for (HudModule m : huds) {
            if (!m.isEnabled()) continue;
            Draw.alpha = a * (m.visible.value ? 1f : 0.35f);
            HudManager.drawAt(c, m, true);
        }
        Draw.alpha = a;
        try {
            if (guideX >= 0) Draw.rect(c, Math.round(guideX), 0, 1, height, Draw.withAlpha(acc, 0.7f));
            if (guideY >= 0) Draw.rect(c, 0, Math.round(guideY), width, 1, Draw.withAlpha(acc, 0.7f));
            if (!previewMode) {
                for (HudModule m : huds) {
                    if (!m.isEnabled()) continue;
                    bounds(m);
                    chrome(c, m, mx, my, acc);
                }
            }
            centerPanel(c, mx, my, acc);
            if (!previewMode && selected != null && selected.isEnabled()) toolbar(c, mx, my, acc);
            helpButton(c, mx, my, acc);
        } finally { Draw.alpha = 1f; }
    }

    private void chrome(DrawContext c, HudModule m, int mx, int my, int acc) {
        int x = rb[0], y = rb[1], w = rb[2], h = rb[3];
        boolean sel = m == selected, hov = in(mx, my, x, y, w, h);
        if (sel) {
            Draw.rect(c, x, y, w, h, Draw.withAlpha(acc, 0.12f));
            if (ClientSettings.glow()) Draw.glow(c, x, y, w, h, 2, acc, 2);
            Draw.border(c, x, y, w, h, 0, acc); Draw.border(c, x + 1, y + 1, w - 2, h - 2, 0, Draw.withAlpha(acc, 0.6f));
        } else Draw.border(c, x, y, w, h, 0, hov ? Draw.withAlpha(acc, 0.8f) : 0x66FFFFFF);
        Draw.textScaled(c, m.name, x, y > 10 ? y - 9 : y + h + 2, sel ? 0xFFFFFFFF : 0xFFB5B6BE, 0.75f, true);
        // gear (bottom-left) and hide/show button (bottom-right)
        Icons.draw(c, "gear", x + 2, y + h - 11, 1, 0xFF4DB8FF);
        int bx = x + w - 11, by = y + h - 11;
        Draw.rrect(c, bx, by, 9, 9, 3, m.visible.value ? 0xFFE53935 : 0xFF3FA34D);
        Icons.draw(c, m.visible.value ? "minus" : "plus", bx + 1, by + 1, 1, 0xFFFFFFFF);
        // resize handle
        Draw.rect(c, x + w - 2, y + h - 2, 5, 5, sel || hov ? 0xFFFFFFFF : 0x88FFFFFF);
    }

    private void centerPanel(DrawContext c, int mx, int my, int acc) {
        int cx = width / 2, cy = height / 2;
        Logo.draw(c, cx - 24, cy - 60, 2, acc);
        boolean hm = in(mx, my, rMain);
        Draw.rrect(c, rMain[0], rMain[1], rMain[2], rMain[3], 3, hm ? 0xF0262730 : 0xF0111114);
        Draw.border(c, rMain[0], rMain[1], rMain[2], rMain[3], 3, hm ? acc : Theme.BORDER);
        Draw.ctext(c, "\u00a7lSHADOW SETTINGS", cx, rMain[1] + 6, 0xFFFFFFFF, false);
        iconBtn(c, rSnap, "grid", ClientSettings.snap.value, mx, my, "Snap to Grid: " + (ClientSettings.snap.value ? "On" : "Off"), acc);
        iconBtn(c, rPrev, "eye", previewMode, mx, my, "Preview Mode: " + (previewMode ? "On" : "Off"), acc);
        iconBtn(c, rReset, "reset", false, mx, my, "Reset Positions", acc);
        iconBtn(c, rMenu, "hud", false, mx, my, "Open Mod Menu", acc);
        if (!hoverTip.isEmpty()) Draw.ctext(c, hoverTip, cx, cy + 50, 0xFFFFFFFF, true);
    }

    private void iconBtn(DrawContext c, int[] r, String icon, boolean on, int mx, int my, String tip, int acc) {
        boolean hov = in(mx, my, r);
        Draw.rrect(c, r[0], r[1], r[2], r[3], 3, on ? Draw.lerp(0xF0111114, acc, 0.45f) : hov ? 0xF0262730 : 0xF0111114);
        Draw.border(c, r[0], r[1], r[2], r[3], 3, on || hov ? acc : Theme.BORDER);
        Icons.drawCentered(c, icon, r[0] + r[2] / 2, r[1] + r[3] / 2, 1, 0xFFFFFFFF);
        if (hov) hoverTip = tip;
    }

    private void toolbar(DrawContext c, int mx, int my, int acc) {
        String sc = String.format(java.util.Locale.ROOT, "x%.2f", selected.scale.value);
        int nameW = Draw.w(selected.name), total = 12 + nameW + 14 + 18 + Draw.w("x0.00") + 18 + 12 + 50 + 50 + 12;
        int x0 = (width - total) / 2, y0 = 6;
        set(tbBox, x0, y0, total, 24);
        Draw.rrect(c, x0, y0, total, 24, 4, 0xF0111114); Draw.border(c, x0, y0, total, 24, 4, acc);
        int x = x0 + 8;
        Draw.text(c, selected.name, x, y0 + 8, 0xFFFFFFFF, false); x += nameW + 12;
        set(tbMinus, x, y0 + 4, 16, 16); x += 20;
        Draw.ctext(c, sc, x + Draw.w("x0.00") / 2, y0 + 8, 0xFFFFFFFF, false); x += Draw.w("x0.00") + 4;
        set(tbPlus, x, y0 + 4, 16, 16); x += 24;
        set(tbHide, x, y0 + 4, 46, 16); x += 50;
        set(tbSet, x, y0 + 4, 46, 16);
        small(c, tbMinus, "-", mx, my); small(c, tbPlus, "+", mx, my);
        small(c, tbHide, selected.visible.value ? "Hide" : "Show", mx, my); small(c, tbSet, "Config", mx, my);
    }

    private void small(DrawContext c, int[] r, String t, int mx, int my) {
        Draw.rrect(c, r[0], r[1], r[2], r[3], 3, in(mx, my, r) ? Theme.BTN_H : Theme.BTN);
        Draw.ctext(c, t, r[0] + r[2] / 2, r[1] + 4, 0xFFFFFFFF, false);
    }

    private void helpButton(DrawContext c, int mx, int my, int acc) {
        boolean hov = in(mx, my, rHelp);
        Draw.rrect(c, rHelp[0], rHelp[1], 28, 28, 4, hov ? 0xF0262730 : 0xF0111114);
        Draw.border(c, rHelp[0], rHelp[1], 28, 28, 4, help ? acc : Theme.BORDER);
        Draw.ctext(c, "?", rHelp[0] + 14, rHelp[1] + 10, 0xFFFFFFFF, false);
        if (help) {
            String[] lines = {"Drag a widget to move it", "Drag the white corner to resize", "Mouse wheel / +- changes scale", "Arrow keys nudge (Shift = 5px)",
                    "Red minus hides, Delete hides selected", "Blue gear opens widget settings", "Esc saves and closes"};
            int w = 0; for (String l : lines) w = Math.max(w, Draw.w(l));
            int hh = lines.length * 11 + 10, x = rHelp[0], y = rHelp[1] - hh - 4;
            Draw.rrect(c, x, y, w + 16, hh, 4, 0xF0111114); Draw.border(c, x, y, w + 16, hh, 4, acc);
            for (int i = 0; i < lines.length; i++) Draw.text(c, lines[i], x + 8, y + 6 + i * 11, 0xFFE6E6E6, false);
        }
    }

    // ------------------------------------------------------------ input
    @Override protected boolean onClick(double mx, double my, int b) {
        if (closing) return true;
        if (in(mx, my, rHelp)) { help = !help; return true; }
        if (!previewMode && selected != null && selected.isEnabled() && in(mx, my, tbBox)) {
            if (in(mx, my, tbMinus)) selected.scale.set(selected.scale.value - 0.05);
            else if (in(mx, my, tbPlus)) selected.scale.set(selected.scale.value + 0.05);
            else if (in(mx, my, tbHide)) selected.visible.toggle();
            else if (in(mx, my, tbSet)) client.setScreen(new ModuleSettingsScreen(this, selected));
            return true;
        }
        if (in(mx, my, rMain)) { client.setScreen(new ShadowClickGui()); return true; }
        if (in(mx, my, rSnap)) { ClientSettings.snap.toggle(); return true; }
        if (in(mx, my, rPrev)) { previewMode = !previewMode; drag = null; return true; }
        if (in(mx, my, rReset)) { HudManager.resetAll(); return true; }
        if (in(mx, my, rMenu)) { client.setScreen(new ShadowClickGui()); return true; }
        if (previewMode || b != 0) return false;
        HudModule[] huds = ModuleManager.huds();
        for (int i = huds.length - 1; i >= 0; i--) {
            HudModule m = huds[i];
            if (!m.isEnabled()) continue;
            bounds(m);
            int x = rb[0], y = rb[1], w = rb[2], h = rb[3];
            if (!in(mx, my, x - 2, y - 2, w + 8, h + 8)) continue;
            selected = m;
            if (in(mx, my, x + 2, y + h - 11, 9, 9)) { client.setScreen(new ModuleSettingsScreen(this, m)); return true; }
            if (in(mx, my, x + w - 11, y + h - 11, 9, 9)) { m.visible.toggle(); return true; }
            drag = m; tx = x; ty = y;
            if (in(mx, my, x + w - 4, y + h - 4, 9, 9)) { resizing = true; }
            else { resizing = false; offX = (float) (mx - x); offY = (float) (my - y); }
            return true;
        }
        selected = null;
        return false;
    }

    @Override protected boolean onDrag(double mx, double my, int b) {
        if (drag == null) return false;
        bounds(drag);
        if (resizing) {
            float base = Math.max(8f, drag.w);
            drag.scale.set(Math.round(((mx - rb[0]) / base) / 0.05) * 0.05);
            return true;
        }
        float nx = (float) (mx - offX), ny = (float) (my - offY);
        int bw = drag.boxW(), bh = drag.boxH();
        guideX = guideY = -1;
        if (ClientSettings.snap.value) {
            int g = ClientSettings.gridSize.i();
            nx = Math.round(nx / g) * g; ny = Math.round(ny / g) * g;
            float midX = (width - bw) / 2f, midY = (height - bh) / 2f;
            if (Math.abs(nx - midX) < 4) { nx = midX; guideX = width / 2f; }
            if (Math.abs(ny - midY) < 4) { ny = midY; guideY = height / 2f; }
            if (Math.abs(nx) < 4) nx = 0;
            if (Math.abs(nx + bw - width) < 4) nx = width - bw;
            if (Math.abs(ny) < 4) ny = 0;
            if (Math.abs(ny + bh - height) < 4) ny = height - bh;
        }
        tx = Math.max(0, Math.min(width - bw, nx)); ty = Math.max(0, Math.min(height - bh, ny));
        return true;
    }

    @Override protected boolean onRelease(double mx, double my, int b) {
        if (drag != null) {
            // finish on the exact target so nothing is left mid-interpolation
            if (!resizing) drag.setPixel(Math.round(tx), Math.round(ty), width, height);
            drag = null; resizing = false; guideX = guideY = -1;
            ConfigManager.saveNow();
        }
        return false;
    }

    @Override protected boolean onKey(int key, int mods) {
        if (selected == null || previewMode) return false;
        int step = (mods & 1) != 0 ? 5 : 1;
        int dx = key == 263 ? -step : key == 262 ? step : 0, dy = key == 265 ? -step : key == 264 ? step : 0;
        if (dx != 0 || dy != 0) { selected.setPixel(selected.pixelX(width) + dx, selected.pixelY(height) + dy, width, height); return true; }
        if (key == 261) { selected.visible.set(false); return true; }
        return false;
    }

    @Override protected boolean onScroll(double mx, double my, double v) {
        if (previewMode) return false;
        HudModule[] huds = ModuleManager.huds();
        for (int i = huds.length - 1; i >= 0; i--) {
            HudModule m = huds[i];
            if (!m.isEnabled()) continue;
            bounds(m);
            if (m == selected || in(mx, my, rb)) { m.scale.set(m.scale.value + (v > 0 ? 0.05 : -0.05)); selected = m; return true; }
        }
        return false;
    }

    @Override public void close() { if (ClientSettings.anim()) closing = true; else client.setScreen(null); }
    @Override public void removed() { ConfigManager.saveNow(); }
}
