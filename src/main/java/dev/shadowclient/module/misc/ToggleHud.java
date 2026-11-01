package dev.shadowclient.module.misc;

import dev.shadowclient.HudManager;
import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.util.Compat;
import org.lwjgl.glfw.GLFW;

/** Press the bound key (default F8) to hide/show every Shadow Client HUD widget. */
public class ToggleHud extends Module {
    private boolean prev;
    public ToggleHud() {
        super("toggle_hud", "Toggle HUD", "Hide or show all Shadow HUD widgets with one key.", "power", ModuleCategory.OTHER, true);
        holdKey = true;
        defaultKey(GLFW.GLFW_KEY_F8);
    }
    @Override public void onTick() {
        int code = keybind.code;
        boolean down = Compat.isKeyDown(code) && mc.currentScreen == null;
        if (down && !prev) HudManager.hidden = !HudManager.hidden;
        prev = down;
    }
    @Override public void onDisable() { HudManager.hidden = false; }
}
