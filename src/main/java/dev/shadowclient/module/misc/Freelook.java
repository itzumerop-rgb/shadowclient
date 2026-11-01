package dev.shadowclient.module.misc;

import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Compat;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;

/** Hold the key (default Left Alt) to look around without turning your player. Camera only; nothing is sent to the server. */
public class Freelook extends Module {
    private static boolean active;
    public static float yaw, pitch;
    private final Setting.Bool third = add(new Setting.Bool("third", "Switch To Third Person", true));
    private final Setting.Num dist = add(new Setting.Num("dist", "Camera Distance", 4.0, 2.0, 8.0, 0.5, 1));
    private Perspective saved;

    public Freelook() {
        super("freelook", "Freelook", "Hold a key to look around freely (default: Left Alt).", "freelook", ModuleCategory.VISUAL, true);
        holdKey = true; isNew = true; defaultKey(GLFW.GLFW_KEY_LEFT_ALT);
    }

    public static boolean isActive() { return active; }
    public static void rotate(double dx, double dy) {
        yaw += (float) (dx * 0.15); pitch = Math.max(-90f, Math.min(90f, pitch + (float) (dy * 0.15)));
    }
    public static double distance() { Freelook f = (Freelook) dev.shadowclient.module.ModuleManager.get("freelook"); return f == null ? 4.0 : f.dist.value; }

    @Override public void onTick() {
        boolean down = Compat.isKeyDown(keybind.code) && mc.currentScreen == null && mc.player != null && !Freecam.isActive();
        if (down && !active) start(); else if (!down && active) stop();
    }
    private void start() {
        yaw = mc.player.getYaw(); pitch = mc.player.getPitch(); active = true;
        if (third.value) { saved = mc.options.getPerspective(); mc.options.setPerspective(Perspective.THIRD_PERSON_BACK); }
    }
    private void stop() {
        active = false;
        if (saved != null) { mc.options.setPerspective(saved); saved = null; }
    }
    @Override public void onDisable() { if (active) stop(); }
    @Override public void onWorldLeave() { if (active) stop(); }
}
