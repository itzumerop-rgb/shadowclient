package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import net.minecraft.client.gui.DrawContext;

public class SpeedDisplay extends HudModule {
    private final Setting.Bool vertical = add(new Setting.Bool("vertical", "Include Vertical Movement", false));
    private final Setting.Choice unit = add(new Setting.Choice("unit", "Unit", new String[]{"blocks/s", "km/h"}, 0));
    private double px, py, pz, speed;
    private boolean init;
    private int lastTenths = -1, lastUnit = -1;
    private String str = "Speed: 0.0 b/s";

    public SpeedDisplay() { super("speed", "Speed Display", "Shows your movement speed.", "speed", ModuleCategory.MOVEMENT, false, 0.008f, 0.135f); }

    @Override public void onTick() {
        if (mc.player == null) { init = false; return; }
        double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
        if (init) {
            double dx = x - px, dz = z - pz, dy = vertical.value ? y - py : 0;
            double v = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20.0;
            speed += (v - speed) * 0.35;
        }
        px = x; py = y; pz = z; init = true;
        double shown = unit.index == 0 ? speed : speed * 3.6;
        int tenths = (int) Math.round(shown * 10);
        if (tenths != lastTenths || unit.index != lastUnit) {
            lastTenths = tenths; lastUnit = unit.index;
            str = "Speed: " + (tenths / 10) + "." + (tenths % 10) + (unit.index == 0 ? " b/s" : " km/h");
        }
    }

    @Override public void draw(DrawContext c, boolean preview) { label(c, preview ? "Speed: 4.3 b/s" : str); }
}
