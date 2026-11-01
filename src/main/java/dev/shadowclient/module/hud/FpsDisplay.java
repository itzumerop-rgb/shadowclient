package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import net.minecraft.client.gui.DrawContext;

public class FpsDisplay extends HudModule {
    private final Setting.Choice style = add(new Setting.Choice("style", "Style", new String[]{"FPS: 120", "120 FPS"}, 0));
    private int last = -1, lastStyle = -1;
    private String str = "";

    public FpsDisplay() { super("fps", "FPS Display", "Shows your current frames per second.", "fps", ModuleCategory.HUD, true, 0.008f, 0.012f); }

    @Override public void draw(DrawContext c, boolean preview) {
        int fps = preview ? 144 : mc.getCurrentFps();
        if (fps != last || style.index != lastStyle) {
            last = fps; lastStyle = style.index;
            str = style.index == 0 ? "FPS: " + fps : fps + " FPS";
        }
        label(c, str);
    }
}
