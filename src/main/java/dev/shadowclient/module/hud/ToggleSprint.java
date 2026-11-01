package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import net.minecraft.client.gui.DrawContext;

/** Press your sprint key once to keep sprinting while moving forward. Purely uses vanilla sprint rules. */
public class ToggleSprint extends HudModule {
    private final Setting.Bool showText = add(new Setting.Bool("text", "Show Status Text", true));
    private boolean toggled, prev;

    public ToggleSprint() { super("toggle_sprint", "Toggle Sprint", "Tap sprint once to keep sprinting.", "sprint", ModuleCategory.MOVEMENT, true, 0.008f, 0.19f); }

    @Override public void onDisable() { toggled = false; }
    @Override public void onWorldLeave() { toggled = false; prev = false; }

    @Override public void onTick() {
        if (mc.player == null) return;
        boolean down = mc.options.sprintKey.isPressed();
        if (down && !prev && mc.currentScreen == null) toggled = !toggled;
        prev = down;
        if (!toggled || mc.currentScreen != null) return;
        var p = mc.player;
        if (mc.options.forwardKey.isPressed() && !p.isSneaking() && !p.isUsingItem() && !p.isSprinting()
                && (p.getHungerManager().getFoodLevel() > 6 || p.getAbilities().allowFlying)) {
            p.setSprinting(true);
        }
    }

    @Override public void draw(DrawContext c, boolean preview) {
        if (!preview && (!toggled || !showText.value)) { w = 0; h = 0; return; }
        label(c, "Sprinting (Toggled)");
    }
}
