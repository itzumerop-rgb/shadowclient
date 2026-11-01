package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.ClientSettings;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import dev.shadowclient.util.Logo;
import net.minecraft.client.gui.DrawContext;

public class Watermark extends HudModule {
    private final Setting.Bool logo = add(new Setting.Bool("logo", "Show Logo", true));
    private final Setting.Bool text = add(new Setting.Bool("text", "Show Text", true));
    private final Setting.Bool fps = add(new Setting.Bool("fps", "FPS Next To Logo", true));
    private final Setting.Choice col = add(new Setting.Choice("color", "Color", new String[]{"Red", "White"}, 0));
    private int last = -1;
    private String fpsStr = "";

    public Watermark() { super("watermark", "Client Watermark", "SHADOW CLIENT logo, name and FPS.", "watermark", ModuleCategory.VISUAL, true, 0.45f, 0.006f); }

    @Override public void draw(DrawContext c, boolean preview) {
        int fp = preview ? 144 : mc.getCurrentFps();
        if (fp != last) { last = fp; fpsStr = fp + " FPS"; }
        int acc = col.index == 0 ? ClientSettings.accent() : 0xFFFFFFFF;
        int x = 4, hgt = logo.value ? 26 : 14;
        int tw = text.value ? Draw.w("SHADOW CLIENT") : 0, fw = fps.value ? Draw.w(fpsStr) : 0;
        int bw = 8 + (logo.value ? 26 : 0) + Math.max(tw, fw) + (text.value || fps.value ? 0 : 0);
        if (!text.value && !fps.value) bw = logo.value ? 30 : 8;
        box(c, bw, hgt);
        if (logo.value) { Logo.draw(c, x, 1, 1, acc); x += 28; }
        if (text.value) {
            Draw.text(c, "SHADOW CLIENT", x, logo.value ? 4 : 3, acc, shadow.value);
            if (fps.value) Draw.text(c, fpsStr, x, 14, color(), shadow.value);
        } else if (fps.value) Draw.text(c, fpsStr, x, logo.value ? 9 : 3, color(), shadow.value);
    }
}
