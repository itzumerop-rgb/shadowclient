package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.ClientSettings;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

/** Shows the entity under your crosshair (name + health) and your attack-cooldown charge. */
public class TargetIndicator extends HudModule {
    private final Setting.Bool attackBar = add(new Setting.Bool("attackBar", "Always Show Attack Bar", true));
    private Entity lastEntity; private String name = ""; private int lastHp2 = -1; private String hpStr = "";

    public TargetIndicator() { super("target", "Target Indicator", "Crosshair target info and attack cooldown bar.", "target", ModuleCategory.PVP, false, 0.40f, 0.60f); isNew = true; }

    @Override public void draw(DrawContext c, boolean preview) {
        String n; String hp; float frac, charge;
        LivingEntity le = null;
        if (preview) { n = "Zombie"; hp = "14.0 / 20.0"; frac = 0.7f; charge = 0.75f; }
        else {
            if (mc.player == null) return;
            Entity e = mc.crosshairTarget instanceof EntityHitResult r ? r.getEntity() : null;
            le = e instanceof LivingEntity l ? l : null;
            charge = mc.player.getAttackCooldownProgress(0f);
            if (le == null && !attackBar.value) { w = 0; h = 0; return; }
            if (le != null) {
                if (le != lastEntity) { lastEntity = le; name = le.getName().getString(); lastHp2 = -1; }
                int hp2 = Math.round(le.getHealth() * 2f);
                if (hp2 != lastHp2) { lastHp2 = hp2; hpStr = String.format(java.util.Locale.ROOT, "%.1f / %.1f", le.getHealth(), le.getMaxHealth()); }
                n = name; hp = hpStr; frac = Math.max(0f, Math.min(1f, le.getHealth() / Math.max(1f, le.getMaxHealth())));
            } else { n = null; hp = null; frac = 0; }
        }
        int bw = 80;
        if (n != null) bw = Math.max(bw, Math.max(Draw.w(n), Draw.w(hp)) + 10);
        int bh = (n != null ? 30 : 0) + (attackBar.value || n != null ? 9 : 0) + 4;
        box(c, bw, bh);
        int y = 3;
        if (n != null) {
            text(c, n, 5, y); y += 11;
            Draw.rrect(c, 5, y, bw - 10, 5, 2, 0xFF2A2B30);
            int hc = frac > 0.5f ? 0xFF4CD964 : frac > 0.25f ? 0xFFFFC043 : 0xFFFF4D4D;
            Draw.rrect(c, 5, y, Math.max(2, Math.round((bw - 10) * frac)), 5, 2, hc);
            y += 7;
            Draw.text(c, hp, 5, y, 0xFFB5B6BE, false); y += 11;
        }
        Draw.rrect(c, 5, y, bw - 10, 4, 2, 0xFF2A2B30);
        Draw.rrect(c, 5, y, Math.max(2, Math.round((bw - 10) * charge)), 4, 2, charge >= 1f ? ClientSettings.accent() : 0xFF8A8B93);
    }
}
