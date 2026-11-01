package dev.shadowclient.module;

import dev.shadowclient.config.ConfigManager;
import dev.shadowclient.module.performance.HudOptimizer;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import net.minecraft.client.gui.DrawContext;

/** A module that draws a movable widget. draw() always renders at (0,0); HudManager positions/scales it. */
public abstract class HudModule extends Module {
    public final Setting.Num scale;
    public final Setting.Bool visible, background, shadow;
    public final Setting.Num bgOpacity;
    public final Setting.Color textColor;
    /** Normalised (0..1) screen position of the top-left corner. */
    public float nx, ny;
    private final float defX, defY;
    /** Size of the last drawn frame in unscaled pixels. */
    public int w = 60, h = 14;
    private int tickCounter;

    protected HudModule(String id, String name, String desc, String icon, ModuleCategory cat, boolean en, float x, float y) {
        super(id, name, desc, icon, cat, en);
        this.defX = this.nx = x; this.defY = this.ny = y;
        scale = add(new Setting.Num("scale", "Scale", 1.0, 0.5, 3.0, 0.05, 2)); scale.persist = false;
        visible = add(new Setting.Bool("visible", "Visible", true)); visible.persist = false;
        background = add(new Setting.Bool("bg", "Background", true));
        bgOpacity = add(new Setting.Num("bgOpacity", "Background Opacity", 0.55, 0, 1, 0.05, 2));
        shadow = add(new Setting.Bool("shadow", "Text Shadow", true));
        textColor = add(new Setting.Color("textColor", "Text Color", 0xFFFFFF));
    }

    /** @param preview true inside the HUD editor: draw sample data when real data is unavailable */
    public abstract void draw(DrawContext c, boolean preview);

    public int boxW() { return Math.max(8, Math.round(w * scale.f())); }
    public int boxH() { return Math.max(8, Math.round(h * scale.f())); }
    public int pixelX(int sw) { return Math.max(0, Math.min(Math.max(0, sw - boxW()), Math.round(nx * sw))); }
    public int pixelY(int sh) { return Math.max(0, Math.min(Math.max(0, sh - boxH()), Math.round(ny * sh))); }
    public void setPixel(int px, int py, int sw, int sh) { nx = px / (float) sw; ny = py / (float) sh; ConfigManager.markDirty(); }
    public void resetLayout() { nx = defX; ny = defY; scale.reset(); visible.reset(); ConfigManager.markDirty(); }

    protected int color() { return 0xFF000000 | textColor.rgb; }
    protected int bg() { return ((int) (bgOpacity.value * 255) << 24) | 0x0A0A0C; }
    protected void box(DrawContext c, int bw, int bh) { w = bw; h = bh; if (background.value) Draw.rrect(c, 0, 0, bw, bh, 3, bg()); }
    protected void text(DrawContext c, String s, int x, int y) { Draw.text(c, s, x, y, color(), shadow.value); }
    protected void label(DrawContext c, String s) { box(c, Draw.w(s) + 10, 14); text(c, s, 5, 3); }
    /** Gate for cached data refresh; honours the HUD Optimizer interval. */
    protected boolean tickGate() { return (++tickCounter % HudOptimizer.interval()) == 0; }
}
