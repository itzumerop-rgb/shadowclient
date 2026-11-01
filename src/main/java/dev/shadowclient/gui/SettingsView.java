package dev.shadowclient.gui;

import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Compat;
import dev.shadowclient.util.Draw;
import dev.shadowclient.util.Theme;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

/** Scrollable list of controls: toggle, slider, dropdown, colour picker, key capture. Used by every settings page. */
public final class SettingsView {
    private static final int ROW = 22, CTRL = 116, PICK_H = 52, SV_W = 20, SV_H = 8, HUE_N = 40;
    private final List<Setting> list;
    private int x, y, w, h;
    private float scroll;
    private Setting openChoice, openColor, dragging;
    private Setting.Key capturing;
    private int dragKind; // 1 slider, 2 sv, 3 hue

    public SettingsView(List<Setting> list) { this.list = list; }
    public void bounds(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
    public boolean capturing() { return capturing != null; }

    private int rowH(Setting s) { return ROW + (s == openColor ? PICK_H : 0); }
    private int contentH() { int t = 0; for (int i = 0; i < list.size(); i++) t += rowH(list.get(i)); return t; }
    private void clamp() { scroll = Math.max(0, Math.min(Math.max(0, contentH() - h), scroll)); }
    private int ctrlX() { return x + w - CTRL - 12; }
    private int rowY(Setting target) {
        int yy = y - (int) scroll;
        for (int i = 0; i < list.size(); i++) { Setting s = list.get(i); if (s == target) return yy; yy += rowH(s); }
        return yy;
    }

    public void render(DrawContext c, int mx, int my) {
        clamp();
        c.enableScissor(x, y, x + w, y + h);
        int yy = y - (int) scroll, cx = ctrlX(), acc = Theme.accent();
        for (int i = 0; i < list.size(); i++) {
            Setting s = list.get(i);
            int rh = rowH(s);
            if (yy + rh > y && yy < y + h) drawRow(c, s, yy, cx, mx, my, acc);
            yy += rh;
        }
        int ch = contentH();
        if (ch > h) {
            int bh = Math.max(16, h * h / ch), by = y + (int) ((h - bh) * (scroll / (ch - h)));
            Draw.rrect(c, x + w - 4, by, 3, bh, 1, 0xFF3A3B42);
        }
        c.disableScissor();
        if (openChoice instanceof Setting.Choice ch2) {
            int oy = rowY(ch2) + ROW - 3;
            Draw.rrect(c, cx, oy, CTRL, ch2.options.length * 15 + 4, 3, 0xFF202127);
            Draw.border(c, cx, oy, CTRL, ch2.options.length * 15 + 4, 3, acc);
            for (int i = 0; i < ch2.options.length; i++) {
                boolean hov = in(mx, my, cx, oy + 2 + i * 15, CTRL, 15);
                if (hov) Draw.rrect(c, cx + 2, oy + 2 + i * 15, CTRL - 4, 15, 2, Draw.withAlpha(acc, 0.35f));
                Draw.text(c, ch2.options[i], cx + 8, oy + 6 + i * 15, i == ch2.index ? acc : 0xFFE6E6E6, false);
            }
        }
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    private void drawRow(DrawContext c, Setting s, int yy, int cx, int mx, int my, int acc) {
        Draw.rrect(c, x, yy + 1, w - 8, ROW - 2, 3, Theme.ROW);
        Draw.text(c, Draw.fit(s.label, cx - x - 14), x + 8, yy + 7, 0xFFE6E6E6, false);
        int cy = yy + 3, ch = ROW - 6;
        boolean hov = in(mx, my, cx, cy, CTRL, ch) && my >= y && my < y + h;
        if (s instanceof Setting.Bool b) {
            int tx = cx + CTRL - 26, ty = cy + 2;
            Draw.rrect(c, tx, ty, 26, 12, 6, b.value ? acc : Theme.OFF);
            Draw.rrect(c, tx + (b.value ? 15 : 2), ty + 2, 8, 8, 4, 0xFFFFFFFF);
        } else if (s instanceof Setting.Num n) {
            int tw = CTRL - 38, ty = cy + ch / 2 - 1;
            Draw.rrect(c, cx, ty, tw, 4, 2, Theme.OFF);
            int fw = (int) (tw * n.fraction());
            Draw.rrect(c, cx, ty, Math.max(2, fw), 4, 2, acc);
            Draw.rrect(c, cx + fw - 3, ty - 2, 6, 8, 3, 0xFFFFFFFF);
            String v = n.display();
            Draw.text(c, v, cx + CTRL - Draw.w(v), cy + 4, 0xFFB5B6BE, false);
        } else if (s instanceof Setting.Choice ch2) {
            Draw.rrect(c, cx, cy, CTRL, ch, 3, hov || openChoice == s ? Theme.BTN_H : Theme.BTN);
            Draw.ctext(c, ch2.get(), cx + CTRL / 2, cy + 4, 0xFFFFFFFF, false);
            Draw.text(c, "v", cx + CTRL - 10, cy + 4, acc, false);
        } else if (s instanceof Setting.Color col) {
            Draw.rrect(c, cx, cy, CTRL, ch, 3, 0xFF000000 | col.rgb);
            Draw.border(c, cx, cy, CTRL, ch, 3, openColor == s ? 0xFFFFFFFF : Theme.BORDER);
            String hex = String.format("#%06X", col.rgb);
            int lum = (((col.rgb >> 16) & 255) * 3 + ((col.rgb >> 8) & 255) * 6 + (col.rgb & 255)) / 10;
            Draw.ctext(c, hex, cx + CTRL / 2, cy + 4, lum > 140 ? 0xFF000000 : 0xFFFFFFFF, false);
            if (openColor == s) drawPicker(c, col, cx, yy + ROW);
        } else if (s instanceof Setting.Key k) {
            boolean cap = capturing == k;
            Draw.rrect(c, cx, cy, CTRL, ch, 3, cap ? Draw.withAlpha(acc, 0.35f) : hov ? Theme.BTN_H : Theme.BTN);
            if (cap) Draw.border(c, cx, cy, CTRL, ch, 3, acc);
            Draw.ctext(c, cap ? "Press a key..." : Compat.keyName(k.code), cx + CTRL / 2, cy + 4, cap ? acc : 0xFFFFFFFF, false);
        }
    }

    private void drawPicker(DrawContext c, Setting.Color col, int px, int py) {
        int cw = CTRL / SV_W, chh = 4;
        for (int i = 0; i < SV_W; i++) for (int j = 0; j < SV_H; j++) {
            int rgb = Setting.Color.hsvToRgb(col.h, i / (SV_W - 1f), 1f - j / (SV_H - 1f));
            Draw.rect(c, px + i * cw, py + j * chh, cw, chh, 0xFF000000 | rgb);
        }
        int sx = px + Math.round(col.s * (SV_W - 1)) * cw, sy = py + Math.round((1 - col.v) * (SV_H - 1)) * chh;
        Draw.border(c, sx - 1, sy - 1, cw + 2, chh + 2, 0, 0xFFFFFFFF);
        int hw = CTRL / HUE_N;
        for (int i = 0; i < HUE_N; i++) Draw.rect(c, px + i * hw, py + 36, hw, 8, 0xFF000000 | Setting.Color.hsvToRgb(i / (float) HUE_N, 1, 1));
        int hx = px + Math.round(col.h * (HUE_N - 1)) * hw;
        Draw.border(c, hx - 1, py + 35, hw + 2, 10, 0, 0xFFFFFFFF);
    }

    public boolean click(double mx, double my, int btn) {
        int cx = ctrlX();
        if (openChoice instanceof Setting.Choice ch) {
            int oy = rowY(ch) + ROW - 3;
            if (in(mx, my, cx, oy + 2, CTRL, ch.options.length * 15)) { ch.set((int) ((my - oy - 2) / 15)); openChoice = null; return true; }
            openChoice = null;
        }
        if (!in(mx, my, x, y, w, h)) return false;
        int yy = y - (int) scroll;
        for (int i = 0; i < list.size(); i++) {
            Setting s = list.get(i);
            int cy = yy + 3, chh = ROW - 6;
            if (s instanceof Setting.Color col && openColor == s && in(mx, my, cx, yy + ROW, CTRL, PICK_H)) {
                dragging = col; dragKind = my < yy + ROW + 34 ? 2 : 3; dragTo(mx, my, yy + ROW); return true;
            }
            if (in(mx, my, cx - 2, cy, CTRL + 4, chh)) {
                capturing = null;
                if (s instanceof Setting.Bool b) b.toggle();
                else if (s instanceof Setting.Num n) { dragging = n; dragKind = 1; dragTo(mx, my, yy); }
                else if (s instanceof Setting.Choice) { openChoice = openChoice == s ? null : s; }
                else if (s instanceof Setting.Color) { openColor = openColor == s ? null : s; }
                else if (s instanceof Setting.Key k) { if (btn == 1) k.set(0); else capturing = k; }
                return true;
            }
            yy += rowH(s);
        }
        capturing = null;
        return true;
    }

    private void dragTo(double mx, double my, int rowTop) {
        int cx = ctrlX();
        if (dragKind == 1 && dragging instanceof Setting.Num n) {
            double f = (mx - cx) / (double) (CTRL - 38);
            n.set(n.min + Math.max(0, Math.min(1, f)) * (n.max - n.min));
        } else if (dragKind == 2 && dragging instanceof Setting.Color col) {
            double sx = Math.max(0, Math.min(1, (mx - cx) / (double) (CTRL - 1))), sy = Math.max(0, Math.min(1, (my - rowTop) / 31.0));
            col.setHsv(col.h, (float) sx, (float) (1 - sy));
        } else if (dragKind == 3 && dragging instanceof Setting.Color col) {
            col.setHsv((float) Math.max(0, Math.min(0.999, (mx - cx) / (double) CTRL)), col.s, col.v);
        }
    }

    public boolean drag(double mx, double my) {
        if (dragging == null) return false;
        int top = rowY(dragging);
        dragTo(mx, my, dragKind == 1 ? top : top + ROW);
        return true;
    }
    public void release() { dragging = null; dragKind = 0; }

    /** @return true if the key was consumed (capturing). */
    public boolean key(int key) {
        if (capturing == null) return false;
        if (key == 256) { capturing = null; return true; }          // Esc: cancel
        if (key == 259 || key == 261) capturing.set(0);               // Backspace/Delete: unbind
        else capturing.set(key);
        capturing = null;
        return true;
    }

    public boolean scroll(double amount) { scroll -= amount * 16; clamp(); return true; }
}
