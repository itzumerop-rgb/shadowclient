package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.ClientSettings;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class DirectionIndicator extends HudModule {
    private static final String[] NAMES = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final String[] AXIS = {"-Z", "", "+X", "", "+Z", "", "-X", ""};
    private final Setting.Choice style = add(new Setting.Choice("style", "Style", new String[]{"Compass Bar", "Text"}, 0));
    private final Setting.Bool axis = add(new Setting.Bool("axis", "Show Axis (Text)", true));
    private int lastKey = -1; private String str = "";

    public DirectionIndicator() { super("direction", "Direction Indicator", "Compass bar or text showing where you face.", "direction", ModuleCategory.HUD, false, 0.42f, 0.04f); isNew = true; }

    @Override public void draw(DrawContext c, boolean preview) {
        float yaw = preview || mc.player == null ? 180f : mc.player.getYaw();
        float comp = MathHelper.wrapDegrees(yaw + 180f); if (comp < 0) comp += 360f;
        int sector = ((int) ((comp + 22.5f) / 45f)) & 7;
        if (style.index == 1) {
            int key = sector * 1000 + (int) comp * 2 + (axis.value ? 1 : 0);
            if (key != lastKey) { lastKey = key; str = "Facing: " + NAMES[sector] + (axis.value && !AXIS[sector].isEmpty() ? " (" + AXIS[sector] + ")" : "") + "  " + (int) comp + "\u00b0"; }
            label(c, str); return;
        }
        box(c, 112, 17);
        for (int k = 0; k < 8; k++) {
            float d = MathHelper.wrapDegrees(k * 45f - comp);
            if (Math.abs(d) > 56f) continue;
            int x = 56 + Math.round(d / 56f * 50f);
            boolean main = (k & 1) == 0;
            int col = k == 0 ? ClientSettings.accent() : main ? color() : 0xFF8A8B93;
            Draw.ctext(c, NAMES[k], x, main ? 3 : 4, col, shadow.value);
        }
        Draw.rect(c, 55, 13, 2, 3, ClientSettings.accent());
    }
}
