package dev.shadowclient.gui;

import dev.shadowclient.config.ConfigManager;
import dev.shadowclient.module.Module;
import dev.shadowclient.settings.ClientSettings;
import dev.shadowclient.util.Anim;
import dev.shadowclient.util.Draw;
import dev.shadowclient.util.Icons;
import dev.shadowclient.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class ModuleSettingsScreen extends ShadowScreen {
    private final Screen parent;
    private final Module m;
    private final SettingsView view;
    private final Anim open = new Anim(0f);
    private int px, py, pw, ph;
    private final int[] rToggle = new int[4], rReset = new int[4], rDone = new int[4];

    public ModuleSettingsScreen(Screen parent, Module m) {
        super(m.name + " Settings");
        this.parent = parent; this.m = m; this.view = new SettingsView(m.uiSettings());
    }

    @Override protected void init() {
        pw = Math.min(width - 16, 330); ph = Math.min(height - 16, 280);
        px = (width - pw) / 2; py = (height - ph) / 2;
        set(rToggle, px + pw - 90, py + 10, 78, 20);
        set(rReset, px + 12, py + ph - 30, 120, 20);
        set(rDone, px + pw - 92, py + ph - 30, 80, 20);
        view.bounds(px + 10, py + 66, pw - 20, ph - 66 - 38);
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        Anim.frame();
        super.render(c, mx, my, delta);
        float a = open.to(1f, 16f);
        Draw.alpha = a;
        try {
            int acc = Theme.accent();
            if (ClientSettings.glow()) Draw.glow(c, px, py, pw, ph, 5, acc, 4);
            Draw.rrect(c, px, py, pw, ph, 5, Theme.PANEL);
            Draw.border(c, px, py, pw, ph, 5, Theme.BORDER);
            Icons.drawCentered(c, m.icon, px + 24, py + 22, 2, acc);
            Draw.text(c, "\u00a7l" + m.name, px + 42, py + 11, 0xFFFFFFFF, false);
            Draw.text(c, Draw.fit(m.description, pw - 150), px + 42, py + 23, Theme.DIM, false);
            boolean en = m.isEnabled();
            boolean hov = in(mx, my, rToggle);
            Draw.rrect(c, rToggle[0], rToggle[1], rToggle[2], rToggle[3], 3, en ? Draw.lerp(Theme.CARD, acc, hov ? 0.4f : 0.28f) : hov ? Theme.BTN_H : Theme.OFF);
            Draw.ctext(c, en ? "Enabled" : "Disabled", rToggle[0] + rToggle[2] / 2, rToggle[1] + 6, en ? Theme.accentSoft() : Theme.DIM, false);
            Draw.rect(c, px + 10, py + 46, pw - 20, 1, Theme.BORDER);
            Draw.text(c, "SETTINGS", px + 12, py + 53, Theme.DIM, false);
            view.render(c, mx, my);
            button(c, rReset, "Reset to Defaults", in(mx, my, rReset), false);
            button(c, rDone, "Done", in(mx, my, rDone), true);
        } finally { Draw.alpha = 1f; }
    }

    private void button(DrawContext c, int[] r, String t, boolean hov, boolean primary) {
        int acc = Theme.accent();
        Draw.rrect(c, r[0], r[1], r[2], r[3], 3, primary ? (hov ? Draw.lerp(acc, 0xFFFFFFFF, 0.15f) : acc) : hov ? Theme.BTN_H : Theme.BTN);
        Draw.ctext(c, t, r[0] + r[2] / 2, r[1] + 6, 0xFFFFFFFF, false);
    }

    @Override protected boolean onClick(double mx, double my, int b) {
        if (view.click(mx, my, b)) return true;
        if (in(mx, my, rToggle)) { m.toggle(); return true; }
        if (in(mx, my, rReset)) { m.resetSettings(); return true; }
        if (in(mx, my, rDone)) { close(); return true; }
        return false;
    }
    @Override protected boolean onDrag(double mx, double my, int b) { return view.drag(mx, my); }
    @Override protected boolean onRelease(double mx, double my, int b) { view.release(); return false; }
    @Override protected boolean onKey(int key, int mods) { return view.key(key); }
    @Override protected boolean onScroll(double mx, double my, double v) { return view.scroll(v); }

    @Override public void close() { ConfigManager.saveNow(); client.setScreen(parent); }
}
