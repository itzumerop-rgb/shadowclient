package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.util.Draw;
import dev.shadowclient.util.Samples;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ShieldStatus extends HudModule {
    private String state = "No Shield";
    private int col = 0xFF9A9BA3;

    public ShieldStatus() { super("shield", "Shield Status", "Shows whether your shield is ready, blocking or on cooldown.", "shield", ModuleCategory.PVP, false, 0.45f, 0.72f); }

    @Override public void onTick() {
        if (mc.player == null) return;
        ItemStack s = mc.player.getOffHandStack();
        if (!s.isOf(Items.SHIELD)) s = mc.player.getMainHandStack();
        if (!s.isOf(Items.SHIELD)) { state = "No Shield"; col = 0xFF9A9BA3; }
        else if (mc.player.getItemCooldownManager().isCoolingDown(s)) { state = "Cooldown"; col = 0xFFFFB347; }
        else if (mc.player.isBlocking()) { state = "Blocking"; col = 0xFF6EE06E; }
        else { state = "Ready"; col = 0xFFFFFFFF; }
    }

    @Override public void draw(DrawContext c, boolean preview) {
        String s = preview ? "Ready" : state;
        box(c, 24 + Draw.w(s) + 6, 20);
        c.drawItem(Samples.shield(), 2, 2);
        Draw.text(c, s, 22, 6, preview ? 0xFFFFFFFF : col, shadow.value);
    }
}
