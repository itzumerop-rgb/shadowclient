package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import dev.shadowclient.util.Samples;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class TotemCounter extends HudModule {
    private final Setting.Bool hideZero = add(new Setting.Bool("hideZero", "Hide When Zero", false));
    private int count = -1;
    private String str = "x0";

    public TotemCounter() { super("totems", "Totem Counter", "Counts Totems of Undying in your inventory and offhand.", "totem", ModuleCategory.PVP, false, 0.45f, 0.80f); }

    @Override public void onTick() {
        if (mc.player == null || !tickGate()) return;
        int n = 0;
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.size(); i++) { ItemStack s = inv.getStack(i); if (s.isOf(Items.TOTEM_OF_UNDYING)) n += s.getCount(); }
        if (n != count) { count = n; str = "x" + n; }
    }

    @Override public void draw(DrawContext c, boolean preview) {
        if (!preview && hideZero.value && count <= 0) { w = 0; h = 0; return; }
        box(c, 24 + Draw.w(preview ? "x3" : str) + 4, 20);
        c.drawItem(Samples.totem(), 2, 2);
        text(c, preview ? "x3" : str, 22, 6);
    }
}
