package dev.shadowclient.module;

import dev.shadowclient.module.hud.*;
import dev.shadowclient.module.misc.*;
import dev.shadowclient.module.performance.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private ModuleManager() { }
    private static Module[] all = new Module[0];
    private static HudModule[] huds = new HudModule[0];
    private static final Map<String, Module> BY_ID = new HashMap<>();
    public static HudOptimizer hudOptimizer;
    public static AnimationControl animations;

    public static void init() {
        hudOptimizer = new HudOptimizer();
        animations = new AnimationControl();
        List<Module> l = new ArrayList<>();
        // HUD
        l.add(new FpsDisplay()); l.add(new ClockDisplay()); l.add(new CoordinatesDisplay());
        l.add(new DirectionIndicator()); l.add(new PingDisplay()); l.add(new PotionEffectsDisplay());
        // PvP
        l.add(new CpsCounter()); l.add(new Keystrokes()); l.add(new ArmorHud()); l.add(new ArmorDurability());
        l.add(new TotemCounter()); l.add(new ShieldStatus()); l.add(new TargetIndicator());
        // Movement
        l.add(new ToggleSprint()); l.add(new Freecam()); l.add(new SpeedDisplay());
        // Visual
        l.add(new Zoom()); l.add(new Freelook()); l.add(new Watermark());
        // Performance
        l.add(new ParticleReduction()); l.add(new RenderTweaks()); l.add(hudOptimizer); l.add(animations);
        // Other
        l.add(new ToggleHud());
        all = l.toArray(new Module[0]);
        List<HudModule> h = new ArrayList<>();
        for (Module m : all) { BY_ID.put(m.id, m); if (m instanceof HudModule hm) h.add(hm); }
        huds = h.toArray(new HudModule[0]);
    }

    public static Module[] all() { return all; }
    public static HudModule[] huds() { return huds; }
    public static Module get(String id) { return BY_ID.get(id); }

    /** Runs onEnable for modules the config marked enabled (options etc. are ready by the first tick). */
    public static void activateLoaded() {
        for (Module m : all) if (m.isEnabled()) { try { m.onEnable(); } catch (Throwable t) { m.setEnabledRaw(false); } }
    }

    public static void tick() {
        for (int i = 0; i < all.length; i++) {
            Module m = all[i];
            if (!m.isEnabled()) continue;
            try { m.onTick(); } catch (Throwable t) { dev.shadowclient.ShadowClient.LOGGER.error("Module {} crashed in tick, disabling", m.id, t); m.setEnabledRaw(false); }
        }
    }

    public static void onWorldLeave() { for (Module m : all) m.onWorldLeave(); }
}
