package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import dev.shadowclient.util.Samples;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ArmorHud extends HudModule {
    static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Setting.Bool showHeld = add(new Setting.Bool("held", "Show Held Item", false));
    private final Setting.Bool bar = add(new Setting.Bool("bar", "Durability Bar", true));

    public ArmorHud() { super("armor_hud", "Armor HUD", "Shows equipped armor with durability bars.", "armor", ModuleCategory.PVP, false, 0.008f, 0.30f); }

    static void bar(DrawContext c, ItemStack s, int x, int y) {
        if (!s.isDamageable() || s.getDamage() <= 0) return;
        float f = 1f - (float) s.getDamage() / s.getMaxDamage();
        int col = 0xFF000000 | MathHelper.hsvToRgb(Math.max(0f, f) / 3f, 1f, 1f);
        Draw.rect(c, x, y, 16, 2, 0xFF000000);
        Draw.rect(c, x, y, Math.round(16 * f), 1, col);
    }

    @Override public void draw(DrawContext c, boolean preview) {
        boolean real = !preview && mc.player != null;
        int n = showHeld.value ? 5 : 4, y = 2;
        box(c, 22, n * 20 + 2);
        for (int i = 0; i < 4; i++) {
            ItemStack s = real ? mc.player.getEquippedStack(SLOTS[i]) : Samples.armor()[i];
            if (!s.isEmpty()) { c.drawItem(s, 3, y); if (bar.value) bar(c, s, 3, y + 16); }
            y += 20;
        }
        if (showHeld.value) {
            ItemStack s = real ? mc.player.getMainHandStack() : Samples.held();
            if (!s.isEmpty()) { c.drawItem(s, 3, y); if (bar.value) bar(c, s, 3, y + 16); }
        }
    }
}
