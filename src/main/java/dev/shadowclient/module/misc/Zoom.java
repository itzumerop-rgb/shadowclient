package dev.shadowclient.module.misc;

import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.module.ModuleManager;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Anim;
import dev.shadowclient.util.Compat;
import org.lwjgl.glfw.GLFW;

/** Hold the zoom key (default C) to narrow the FOV. Restores your FOV and sensitivity on release. */
public class Zoom extends Module {
    private final Setting.Num factor = add(new Setting.Num("factor", "Zoom Factor", 4.0, 1.5, 20.0, 0.5, 1));
    private final Setting.Bool lowSens = add(new Setting.Bool("sens", "Lower Sensitivity While Zoomed", true));
    private final Setting.Bool smooth = add(new Setting.Bool("smooth", "Smooth Zoom", true));
    private final Anim a = new Anim(0f);
    private boolean target;
    private int origFov = -1; private double origSens;

    public Zoom() {
        super("zoom", "Zoom", "Hold a key to zoom in (default: C).", "zoom", ModuleCategory.VISUAL, true);
        holdKey = true; defaultKey(GLFW.GLFW_KEY_C);
    }

    private static Zoom self() { return (Zoom) ModuleManager.get("zoom"); }
    /** Called every rendered frame from HudManager (and each tick) for smooth, always-restored zoom. */
    public static void frame() { Zoom z = self(); if (z != null) z.update(); }

    @Override public void onTick() { target = Compat.isKeyDown(keybind.code) && mc.currentScreen == null && mc.player != null; update(); }
    @Override public void onDisable() { target = false; a.v = 0; restore(); }
    @Override public void onWorldLeave() { target = false; a.v = 0; restore(); }

    private void update() {
        if (!isEnabled()) { if (origFov >= 0) restore(); return; }
        if (mc.options == null) return;
        float v = smooth.value ? a.to(target ? 1f : 0f, 18f) : (a.v = target ? 1f : 0f);
        if (v > 0.001f) {
            if (origFov < 0) { origFov = mc.options.getFov().getValue(); origSens = mc.options.getMouseSensitivity().getValue(); }
            float zoomed = Math.max(5f, origFov / factor.f());
            mc.options.getFov().setValue(Math.round(origFov + (zoomed - origFov) * v));
            if (lowSens.value) mc.options.getMouseSensitivity().setValue(origSens / (1.0 + (factor.value - 1.0) * v));
        } else if (origFov >= 0) restore();
    }

    private void restore() {
        if (origFov < 0 || mc.options == null) return;
        mc.options.getFov().setValue(origFov);
        mc.options.getMouseSensitivity().setValue(origSens);
        origFov = -1;
    }
}
