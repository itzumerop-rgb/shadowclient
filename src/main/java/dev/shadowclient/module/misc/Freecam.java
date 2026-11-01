package dev.shadowclient.module.misc;

import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Detached camera. RESTRICTED to singleplayer / LAN-host worlds (an integrated server must be running).
 * It refuses to activate on remote multiplayer servers to respect server rules.
 */
public class Freecam extends Module {
    private static boolean active;
    public static float yaw, pitch;
    private static double x, y, z, px, py, pz;
    private final Setting.Num speed = add(new Setting.Num("speed", "Speed", 1.0, 0.1, 5.0, 0.1, 1));

    public Freecam() {
        super("freecam", "Freecam", "Detached camera. Singleplayer / LAN only.", "freecam", ModuleCategory.MOVEMENT, false);
        isNew = true; persistEnabled = false; defaultKey(GLFW.GLFW_KEY_F6);
    }

    public static boolean isActive() { return active; }
    public static void rotate(double dx, double dy) {
        yaw += (float) (dx * 0.15); pitch = Math.max(-90f, Math.min(90f, pitch + (float) (dy * 0.15)));
    }
    public static double x(float t) { return px + (x - px) * t; }
    public static double y(float t) { return py + (y - py) * t; }
    public static double z(float t) { return pz + (z - pz) * t; }

    private boolean allowed() { return mc.player != null && mc.getServer() != null; }

    @Override protected boolean canEnable() {
        if (!allowed()) {
            if (mc.player != null) mc.player.sendMessage(Text.literal("\u00a7c[Shadow Client] Freecam is only available in singleplayer / LAN worlds."), true);
            return false;
        }
        return true;
    }
    @Override public void onEnable() {
        if (mc.player == null) { setEnabledRaw(false); return; }
        x = px = mc.player.getX(); y = py = mc.player.getEyeY(); z = pz = mc.player.getZ();
        yaw = mc.player.getYaw(); pitch = mc.player.getPitch(); active = true;
    }
    @Override public void onDisable() { active = false; }
    @Override public void onWorldLeave() { active = false; setEnabledRaw(false); }

    @Override public void onTick() {
        if (!active) return;
        if (!allowed()) { setEnabled(false); return; }
        px = x; py = y; pz = z;
        var o = mc.options;
        float f = (o.forwardKey.isPressed() ? 1 : 0) - (o.backKey.isPressed() ? 1 : 0);
        float s = (o.rightKey.isPressed() ? 1 : 0) - (o.leftKey.isPressed() ? 1 : 0);
        float u = (o.jumpKey.isPressed() ? 1 : 0) - (o.sneakKey.isPressed() ? 1 : 0);
        Vec3d look = Vec3d.fromPolar(pitch, yaw);
        Vec3d right = look.crossProduct(new Vec3d(0, 1, 0));
        right = right.lengthSquared() < 1e-6 ? Vec3d.ZERO : right.normalize();
        double sp = speed.value * 0.6;
        x += (look.x * f + right.x * s) * sp;
        y += (look.y * f + right.y * s + u) * sp;
        z += (look.z * f + right.z * s) * sp;
    }
}
