package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import dev.shadowclient.util.Samples;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ArmorDurability extends HudModule {
    private final Setting.Choice mode = add(new Setting.Choice("mode", "Display", new String[]{"Remaining", "Percent"}, 0));
    private final Setting.Bool showHeld = add(new Setting.Bool("held", "Show Held Item", true));
    private final int[] last = new int[5];
    private final String[] str = new String[5];
    private int lastMode = -1;

    public ArmorDurability() {
        super("armor_durability", "Armor Durability", "Shows numeric durability of armor and your held item.", "durability", ModuleCategory.PVP, true, 0.915f, 0.62f);
        java.util.Arrays.fill(last, -2); java.util.Arrays.fill(str, "");
    }

    @Override public void draw(DrawContext c, boolean preview) {
        boolean real = !preview && mc.player != null;
        if (lastMode != mode.index) { lastMode = mode.index; java.util.Arrays.fill(last, -2); }
        int n = showHeld.value ? 5 : 4, widest = 20;
        for (int i = 0; i < n; i++) {
            ItemStack s = stack(i, real);
            int v = s.isEmpty() ? -1 : (!s.isDamageable() ? -3 : (mode.index == 0 ? s.getMaxDamage() - s.getDamage() : Math.round(100f * (s.getMaxDamage() - s.getDamage()) / s.getMaxDamage())));
            if (v != last[i]) { last[i] = v; str[i] = v == -1 ? "" : v == -3 ? "-" : (mode.index == 0 ? Integer.toString(v) : v + "%"); }
            widest = Math.max(widest, 24 + Draw.w(str[i]));
        }
        box(c, widest + 6, n * 18 + 4);
        int y = 3;
        for (int i = 0; i < n; i++) {
            ItemStack s = stack(i, real);
            if (!s.isEmpty()) {
                c.drawItem(s, 3, y);
                int col = color();
                if (s.isDamageable()) col = 0xFF000000 | MathHelper.hsvToRgb(Math.max(0f, 1f - (float) s.getDamage() / s.getMaxDamage()) / 3f, 0.85f, 1f);
                Draw.text(c, str[i], 23, y + 4, col, shadow.value);
            }
            y += 18;
        }
    }

    private ItemStack stack(int i, boolean real) {
        if (i == 4) return real ? mc.player.getMainHandStack() : Samples.held();
        return real ? mc.player.getEquippedStack(ArmorHud.SLOTS[i]) : Samples.armor()[i];
    }
}
