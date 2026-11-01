package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class CoordinatesDisplay extends HudModule {
    private final Setting.Choice layout = add(new Setting.Choice("layout", "Layout", new String[]{"Multi-line", "Single line"}, 0));
    private final Setting.Num decimals = add(new Setting.Num("decimals", "Decimals", 0, 0, 2, 1, 0));
    private String sx = "0", sy = "0", sz = "0", one = "XYZ: 0 0 0";

    public CoordinatesDisplay() { super("coords", "Coordinates", "Shows your X, Y and Z position.", "coords", ModuleCategory.HUD, true, 0.008f, 0.25f); }

    private String f(double v) {
        int d = decimals.i();
        return d == 0 ? Integer.toString(MathHelper.floor(v)) : String.format(Locale.ROOT, "%." + d + "f", v);
    }

    @Override public void onTick() {
        if (mc.player == null || !tickGate()) return;
        sx = f(mc.player.getX()); sy = f(mc.player.getY()); sz = f(mc.player.getZ());
        one = "XYZ: " + sx + " " + sy + " " + sz;
    }

    @Override public void draw(DrawContext c, boolean preview) {
        String x = preview ? "128" : sx, y = preview ? "64" : sy, z = preview ? "-342" : sz;
        if (layout.index == 1) { label(c, preview ? "XYZ: 128 64 -342" : one); return; }
        String a = "X: " + x, b = "Y: " + y, d = "Z: " + z;
        box(c, Math.max(Draw.w(a), Math.max(Draw.w(b), Draw.w(d))) + 10, 38);
        text(c, a, 5, 3); text(c, b, 5, 14); text(c, d, 5, 25);
    }
}
