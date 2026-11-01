package dev.shadowclient.gui;

import dev.shadowclient.HudManager;
import dev.shadowclient.config.ConfigManager;
import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.module.ModuleManager;
import dev.shadowclient.settings.ClientSettings;
import dev.shadowclient.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import net.minecraft.client.gui.DrawContext;

/** Shadow Client Mod Menu: header, tab row, filter pills, search, favourites, grid/list switch and module cards. */
public class ShadowClickGui extends ShadowScreen {
    private enum Tab { MODS, SETTINGS }
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd  hh:mm a", Locale.ENGLISH);
    private static final int GAP = 8;

    private Tab tab = Tab.MODS;
    private ModuleCategory cat = ModuleCategory.ALL;
    private String search = "";
    private boolean searchFocus, favOnly, closing, listDirty = true;
    private float scroll, scrollT;
    private final Anim open = new Anim(0f);
    private final Map<String, Anim> hover = new HashMap<>();
    private final List<Module> shown = new ArrayList<>();
    private final ModuleCategory[] cats = ModuleCategory.values();
    private final Anim[] catAnim = new Anim[cats.length];
    private SettingsView global;
    private String clock = ""; private long clockSec = -1;

    private int pw, px, py, tabsY, mainY, mainH, gx, gy, gw, gh, cols, cw, ch, pillY;
    private final int[] pillX = new int[8], pillW = new int[8];
    private final int[] rClose = new int[4], rTabMods = new int[4], rTabSet = new int[4], rTabEdit = new int[4],
            rHdrEdit = new int[4], rHdrGear = new int[4], rSearch = new int[4], rFav = new int[4], rLayout = new int[4],
            rResetAll = new int[4], rResetHud = new int[4], rH = new int[4], rG = new int[4], rT = new int[4];

    public ShadowClickGui() {
        super("Shadow Client");
        for (int i = 0; i < catAnim.length; i++) catAnim[i] = new Anim(i == 0 ? 1f : 0f);
    }

    // ---------------------------------------------------------------- layout
    @Override protected void init() {
        pw = Math.min(width - 12, 640); px = (width - pw) / 2;
        int top = 42 + 32;
        int total = Math.min(height - 12, top + 300);
        mainH = Math.max(120, total - top);
        py = Math.max(4, (height - (top + mainH)) / 2);
        tabsY = py + 42; mainY = tabsY + 32;
        set(rHdrEdit, px + pw - 28, py + 4, 28, 28);
        set(rHdrGear, px + pw - 62, py + 4, 28, 28);
        set(rTabMods, px, tabsY, 96, 26); set(rTabSet, px + 102, tabsY, 26, 26); set(rTabEdit, px + 132, tabsY, 26, 26);
        set(rClose, px + pw - 26, tabsY, 26, 26);

        pillY = mainY + 8;
        int sw = Math.min(120, Math.max(60, pw / 5));
        int avail = pw - 16 - (sw + 16 + 16 + 3 * 5);
        int pad = 8, g = 5, sum = 0;
        for (int i = 0; i < cats.length; i++) sum += Draw.w(cats[i].label) + 2 * pad + g;
        if (sum > avail) { pad = 4; g = 3; }
        int xx = px + 8;
        for (int i = 0; i < cats.length; i++) { pillW[i] = Draw.w(cats[i].label) + 2 * pad; pillX[i] = xx; xx += pillW[i] + g; }
        set(rLayout, px + pw - 8 - 16, pillY, 16, 16);
        set(rFav, rLayout[0] - 5 - 16, pillY, 16, 16);
        set(rSearch, rFav[0] - 5 - sw, pillY, sw, 16);

        gx = px + 8; gy = pillY + 24; gw = pw - 16 - 6; gh = mainH - (gy - mainY) - 6;
        boolean grid = ClientSettings.gridLayout.value;
        cols = grid ? (gw >= 520 ? 4 : gw >= 380 ? 3 : 2) : 1;
        cw = (gw - (cols - 1) * GAP) / cols;
        ch = grid ? 78 : 28;

        if (global == null) global = new SettingsView(ClientSettings.uiList());
        global.bounds(px + 10, mainY + 10, pw - 20, mainH - 50);
        set(rResetAll, px + 10, mainY + mainH - 30, 120, 20);
        set(rResetHud, px + 136, mainY + mainH - 30, 130, 20);
        listDirty = true;
    }

    private void rebuild() {
        shown.clear();
        String q = search.toLowerCase(Locale.ROOT);
        for (Module m : ModuleManager.all()) {
            if (cat == ModuleCategory.NEW) { if (!m.isNew) continue; }
            else if (cat != ModuleCategory.ALL && m.category != cat) continue;
            if (favOnly && !m.isFavorite()) continue;
            if (!q.isEmpty() && !m.name.toLowerCase(Locale.ROOT).contains(q) && !m.description.toLowerCase(Locale.ROOT).contains(q)) continue;
            shown.add(m);
        }
        listDirty = false;
    }

    private int rows() { return (shown.size() + cols - 1) / cols; }
    private int contentH() { return rows() * (ch + GAP) - GAP; }
    private float maxScroll() { return Math.max(0, contentH() - gh); }
    private int cardX(int i) { return gx + (i % cols) * (cw + GAP); }
    private int cardY(int i) { return gy + (i / cols) * (ch + GAP) - Math.round(scroll); }

    private void cardRects(int x, int y) {
        if (ClientSettings.gridLayout.value) {
            set(rH, x + cw - 22, y + 5, 16, 16);
            set(rG, x + 8, y + ch - 26, 20, 20);
            set(rT, x + 32, y + ch - 26, cw - 40, 20);
        } else {
            set(rT, x + cw - 80, y + 4, 74, 20);
            set(rG, x + cw - 106, y + 4, 20, 20);
            set(rH, x + cw - 126, y + 6, 16, 16);
        }
    }

    // ---------------------------------------------------------------- render
    @Override public void render(DrawContext c, int mx, int my, float delta) {
        Anim.frame();
        super.render(c, mx, my, delta);
        float a = open.to(closing ? 0f : 1f, 15f);
        if (closing && a <= 0.02f) { client.setScreen(null); return; }
        if (listDirty) rebuild();
        scrollT = Math.max(0, Math.min(maxScroll(), scrollT));
        scroll = ClientSettings.anim() ? scroll + (scrollT - scroll) * (1f - (float) Math.exp(-18 * Anim.dt)) : scrollT;
        if (Math.abs(scroll - scrollT) < 0.3f) scroll = scrollT;

        Draw.alpha = a;
        c.getMatrices().pushMatrix();
        try {
            if (ClientSettings.anim()) {
                float s = 0.94f + 0.06f * a, cx = width / 2f, cy = height / 2f;
                c.getMatrices().translate(cx, cy); c.getMatrices().scale(s, s); c.getMatrices().translate(-cx, -cy);
            }
            int acc = Theme.accent();
            if (ClientSettings.glow() && ClientSettings.bgAnim.value) {
                float pulse = 0.55f + 0.45f * (float) Math.sin(System.currentTimeMillis() / 700.0);
                Draw.glow(c, px, mainY, pw, mainH, 4, Draw.withAlpha(acc, 0.5f + 0.5f * pulse), 5);
            }
            header(c, mx, my, acc);
            tabs(c, mx, my, acc);
            Draw.rrect(c, px, mainY, pw, mainH, 4, Theme.PANEL);
            if (tab == Tab.MODS) { filters(c, mx, my, acc); grid(c, mx, my, acc); }
            else settings(c, mx, my, acc);
        } finally { c.getMatrices().popMatrix(); Draw.alpha = 1f; }
    }

    private void btn(DrawContext c, int[] r, boolean hov, boolean active, int acc) {
        Draw.rrect(c, r[0], r[1], r[2], r[3], 3, active ? Draw.lerp(Theme.BTN, acc, 0.9f) : hov ? Theme.BTN_H : Theme.BTN);
    }

    private void header(DrawContext c, int mx, int my, int acc) {
        Logo.draw(c, px + 2, py + 5, 1, acc);
        Draw.textScaled(c, "\u00a7lMOD MENU", px + 32, py + 8, 0xFFFFFFFF, 1.4f, false);
        Draw.textScaled(c, "SHADOW CLIENT  \u2022  by " + dev.shadowclient.ShadowClient.AUTHOR, px + 32, py + 24, Theme.DIM, 0.7f, false);
        long sec = System.currentTimeMillis() / 1000;
        if (sec != clockSec) { clockSec = sec; clock = LocalDateTime.now().format(FMT); }
        String name = client.getSession().getUsername();
        int upW = Draw.w(name) + 32, upX = rHdrGear[0] - 6 - upW;
        int clW = Draw.w(clock) + 26, clX = px + 32 + (int) (Draw.w("MOD MENU") * 1.4f) + 24;
        if (clX + clW < upX - 6) {
            Draw.rrect(c, clX, py + 8, clW, 20, 3, Theme.BTN);
            Icons.draw(c, "watermark", clX + 6, py + 14, 1, Theme.DIM);
            Draw.text(c, clock, clX + 20, py + 14, 0xFFFFFFFF, false);
        }
        Draw.rrect(c, upX, py + 4, upW, 28, 3, Theme.BTN);
        Icons.draw(c, "user", upX + 7, py + 14, 2, acc);
        Draw.text(c, name, upX + 26, py + 14, 0xFFFFFFFF, false);
        btn(c, rHdrGear, in(mx, my, rHdrGear), false, acc); Icons.drawCentered(c, "gear", rHdrGear[0] + 14, rHdrGear[1] + 14, 1, 0xFFFFFFFF);
        btn(c, rHdrEdit, in(mx, my, rHdrEdit), false, acc); Icons.drawCentered(c, "move", rHdrEdit[0] + 14, rHdrEdit[1] + 14, 1, 0xFFFFFFFF);
    }

    private void tabs(DrawContext c, int mx, int my, int acc) {
        boolean hm = in(mx, my, rTabMods);
        Draw.rrect(c, rTabMods[0], rTabMods[1], rTabMods[2], rTabMods[3], 3, tab == Tab.MODS ? (hm ? Draw.lerp(acc, 0xFFFFFFFF, 0.12f) : acc) : hm ? Theme.BTN_H : Theme.BTN);
        Icons.draw(c, "grid", rTabMods[0] + 9, rTabMods[1] + 9, 1, 0xFFFFFFFF);
        Draw.text(c, "\u00a7lMOD MENU", rTabMods[0] + 22, rTabMods[1] + 9, 0xFFFFFFFF, false);
        btn(c, rTabSet, in(mx, my, rTabSet), tab == Tab.SETTINGS, acc); Icons.drawCentered(c, "gear", rTabSet[0] + 13, rTabSet[1] + 13, 1, 0xFFFFFFFF);
        btn(c, rTabEdit, in(mx, my, rTabEdit), false, acc); Icons.drawCentered(c, "move", rTabEdit[0] + 13, rTabEdit[1] + 13, 1, 0xFFFFFFFF);
        boolean hc = in(mx, my, rClose);
        Draw.rrect(c, rClose[0], rClose[1], rClose[2], rClose[3], 3, hc ? Draw.withAlpha(acc, 0.35f) : Draw.withAlpha(acc, 0.12f));
        Draw.border(c, rClose[0], rClose[1], rClose[2], rClose[3], 3, acc);
        Icons.drawCentered(c, "close", rClose[0] + 13, rClose[1] + 13, 1, acc);
    }

    private void filters(DrawContext c, int mx, int my, int acc) {
        for (int i = 0; i < cats.length; i++) {
            float t = catAnim[i].to(cats[i] == cat ? 1f : 0f, 16f);
            boolean hov = mx >= pillX[i] && mx < pillX[i] + pillW[i] && my >= pillY && my < pillY + 16;
            int fill = Draw.lerp(hov ? Theme.BTN_H : Theme.BTN, acc, t);
            Draw.rrect(c, pillX[i], pillY, pillW[i], 16, 3, fill);
            Draw.ctext(c, cats[i].label, pillX[i] + pillW[i] / 2, pillY + 4, Draw.lerp(Theme.DIM, 0xFFFFFFFF, Math.max(t, hov ? 0.7f : 0f)), false);
        }
        Draw.rrect(c, rSearch[0], rSearch[1], rSearch[2], rSearch[3], 3, Theme.BTN);
        if (searchFocus) Draw.border(c, rSearch[0], rSearch[1], rSearch[2], rSearch[3], 3, acc);
        String s = search; int maxW = rSearch[2] - 14;
        while (s.length() > 1 && Draw.w(s) > maxW) s = s.substring(1);
        if (search.isEmpty() && !searchFocus) Draw.text(c, "Search", rSearch[0] + 7, rSearch[1] + 4, Theme.DIM, false);
        else Draw.text(c, s + (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : ""), rSearch[0] + 7, rSearch[1] + 4, 0xFFFFFFFF, false);
        btn(c, rFav, in(mx, my, rFav), false, acc);
        Icons.drawCentered(c, "heart", rFav[0] + 8, rFav[1] + 8, 1, favOnly ? acc : 0xFFB5B6BE);
        btn(c, rLayout, in(mx, my, rLayout), false, acc);
        Icons.drawCentered(c, ClientSettings.gridLayout.value ? "grid" : "list", rLayout[0] + 8, rLayout[1] + 8, 1, 0xFFB5B6BE);
    }

    private void grid(DrawContext c, int mx, int my, int acc) {
        c.enableScissor(gx - 2, gy, gx + gw + 6, gy + gh);
        boolean g = ClientSettings.gridLayout.value;
        for (int i = 0; i < shown.size(); i++) {
            int x = cardX(i), y = cardY(i);
            if (y + ch < gy || y > gy + gh) continue;
            drawCard(c, shown.get(i), x, y, mx, my, g, acc);
        }
        if (shown.isEmpty()) Draw.ctext(c, favOnly ? "No favourites yet - click a heart." : "No modules found.", gx + gw / 2, gy + 20, Theme.DIM, false);
        float ms = maxScroll();
        if (ms > 0) {
            int bh = Math.max(18, (int) (gh * (gh / (float) contentH())));
            int by = gy + (int) ((gh - bh) * (scroll / ms));
            Draw.rrect(c, gx + gw + 2, by, 3, bh, 1, 0xFF3A3B42);
        }
        c.disableScissor();
    }

    private void drawCard(DrawContext c, Module m, int x, int y, int mx, int my, boolean grid, int acc) {
        boolean hov = my >= gy && my < gy + gh && mx >= x && mx < x + cw && my >= y && my < y + ch;
        float h = hover.computeIfAbsent(m.id, k -> new Anim(0f)).to(hov ? 1f : 0f, 16f);
        y -= Math.round(h);
        boolean en = m.isEnabled();
        cardRects(x, y);
        Draw.rrect(c, x, y, cw, ch, 4, Draw.lerp(Theme.CARD, Theme.CARD_H, h));
        Draw.border(c, x, y, cw, ch, 4, Draw.lerp(Theme.BORDER, acc, h * 0.9f));
        if (h > 0.3f && ClientSettings.glow()) Draw.glow(c, x, y, cw, ch, 4, Draw.withAlpha(acc, h), 2);
        int nameCol = en ? 0xFFFFFFFF : Theme.DIM;
        boolean favHov = in(mx, my, rH), gearHov = in(mx, my, rG), togHov = in(mx, my, rT);
        if (grid) {
            String nm = Draw.fit(m.name, cw - 52);
            Draw.text(c, nm, x + 8, y + 7, nameCol, false);
            if (m.isNew) {
                int bx = x + 8 + Draw.w(nm) + 4;
                if (bx + 22 < x + cw - 24) { Draw.rrect(c, bx, y + 6, 22, 10, 3, acc); Draw.textScaled(c, "NEW", bx + 4, y + 8, 0xFFFFFFFF, 0.7f, false); }
            }
            Draw.textScaled(c, Draw.fit(m.description, (int) ((cw - 16) / 0.75f)), x + 8, y + 19, Theme.DIM, 0.75f, false);
            Icons.drawCentered(c, m.icon, x + cw / 2, y + 40, 2 + (h > 0.5f ? 0 : 0), en ? 0xFFFFFFFF : Theme.ICON_OFF);
        } else {
            Icons.drawCentered(c, m.icon, x + 18, y + ch / 2, 2, en ? 0xFFFFFFFF : Theme.ICON_OFF);
            Draw.text(c, m.name, x + 38, y + 5, nameCol, false);
            Draw.textScaled(c, Draw.fit(m.description, (int) ((cw - 38 - 134) / 0.75f)), x + 38, y + 16, Theme.DIM, 0.75f, false);
        }
        Draw.rrect(c, rH[0], rH[1], 16, 16, 3, favHov ? Theme.BTN_H : Theme.BTN);
        Icons.drawCentered(c, "heart", rH[0] + 8, rH[1] + 8, 1, m.isFavorite() ? acc : 0xFF6B6C74);
        Draw.rrect(c, rG[0], rG[1], 20, 20, 3, gearHov ? Theme.BTN_H : Theme.BTN);
        Icons.drawCentered(c, "gear", rG[0] + 10, rG[1] + 10, 1, gearHov ? 0xFFFFFFFF : 0xFFB5B6BE);
        int tf = en ? Draw.lerp(Theme.CARD, acc, togHov ? 0.42f : 0.28f) : togHov ? Theme.BTN_H : Theme.BTN;
        Draw.rrect(c, rT[0], rT[1], rT[2], rT[3], 3, tf);
        Draw.rrect(c, rT[0] + 7, rT[1] + 7, 6, 6, 3, en ? acc : 0xFF4A4B52);
        Draw.ctext(c, en ? "Enabled" : "Disabled", rT[0] + rT[2] / 2 + 5, rT[1] + 6, en ? Theme.accentSoft() : Theme.DIM, false);
    }

    private void settings(DrawContext c, int mx, int my, int acc) {
        global.render(c, mx, my);
        Draw.rrect(c, rResetAll[0], rResetAll[1], rResetAll[2], rResetAll[3], 3, in(mx, my, rResetAll) ? Theme.BTN_H : Theme.BTN);
        Draw.ctext(c, "Reset All Settings", rResetAll[0] + rResetAll[2] / 2, rResetAll[1] + 6, 0xFFFFFFFF, false);
        Draw.rrect(c, rResetHud[0], rResetHud[1], rResetHud[2], rResetHud[3], 3, in(mx, my, rResetHud) ? Theme.BTN_H : Theme.BTN);
        Draw.ctext(c, "Reset HUD Positions", rResetHud[0] + rResetHud[2] / 2, rResetHud[1] + 6, 0xFFFFFFFF, false);
    }

    // ---------------------------------------------------------------- input
    @Override protected boolean onClick(double mx, double my, int b) {
        if (closing) return true;
        if (in(mx, my, rClose)) { close(); return true; }
        if (in(mx, my, rHdrEdit) || in(mx, my, rTabEdit)) { client.setScreen(new HudEditorScreen()); return true; }
        if (in(mx, my, rHdrGear) || in(mx, my, rTabSet)) { tab = tab == Tab.SETTINGS ? Tab.MODS : Tab.SETTINGS; return true; }
        if (in(mx, my, rTabMods)) { tab = Tab.MODS; return true; }
        if (tab == Tab.SETTINGS) {
            if (global.click(mx, my, b)) return true;
            if (in(mx, my, rResetAll)) { for (Module m : ModuleManager.all()) m.resetSettings(); ClientSettings.resetAll(); return true; }
            if (in(mx, my, rResetHud)) { HudManager.resetAll(); return true; }
            return false;
        }
        searchFocus = in(mx, my, rSearch);
        if (searchFocus) return true;
        for (int i = 0; i < cats.length; i++) {
            if (mx >= pillX[i] && mx < pillX[i] + pillW[i] && my >= pillY && my < pillY + 16) { cat = cats[i]; listDirty = true; scrollT = 0; return true; }
        }
        if (in(mx, my, rFav)) { favOnly = !favOnly; listDirty = true; return true; }
        if (in(mx, my, rLayout)) { ClientSettings.gridLayout.toggle(); init(); scrollT = 0; return true; }
        if (my >= gy && my < gy + gh) {
            for (int i = 0; i < shown.size(); i++) {
                int x = cardX(i), y = cardY(i);
                if (mx < x || mx >= x + cw || my < y || my >= y + ch) continue;
                Module m = shown.get(i);
                cardRects(x, y);
                if (b == 1) { client.setScreen(new ModuleSettingsScreen(this, m)); return true; }
                if (in(mx, my, rH)) { m.setFavorite(!m.isFavorite()); if (favOnly) listDirty = true; }
                else if (in(mx, my, rG)) client.setScreen(new ModuleSettingsScreen(this, m));
                else if (in(mx, my, rT)) m.toggle();
                return true;
            }
        }
        return false;
    }

    @Override protected boolean onDrag(double mx, double my, int b) { return tab == Tab.SETTINGS && global.drag(mx, my); }
    @Override protected boolean onRelease(double mx, double my, int b) { global.release(); return false; }

    @Override protected boolean onKey(int key, int mods) {
        if (tab == Tab.SETTINGS && global.key(key)) return true;
        if (tab == Tab.MODS && searchFocus) {
            if (key == 259) { if (!search.isEmpty()) { search = search.substring(0, search.length() - 1); listDirty = true; } return true; }
            if (key == 256 || key == 257) { searchFocus = false; return true; }
        }
        return false;
    }

    @Override protected boolean onChar(int cp) {
        if (tab == Tab.MODS && searchFocus && cp >= 32 && cp != 127 && search.length() < 24) {
            search += new String(Character.toChars(cp)); listDirty = true; scrollT = 0; return true;
        }
        return false;
    }

    @Override protected boolean onScroll(double mx, double my, double v) {
        if (tab == Tab.SETTINGS) return global.scroll(v);
        scrollT = Math.max(0, Math.min(maxScroll(), scrollT - (float) v * 26f));
        return true;
    }

    @Override public void close() {
        if (ClientSettings.anim()) closing = true; else client.setScreen(null);
    }
    @Override public void removed() { ConfigManager.saveNow(); }
}
