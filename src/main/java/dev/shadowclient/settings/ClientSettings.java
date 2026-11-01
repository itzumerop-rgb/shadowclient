package dev.shadowclient.settings;

import dev.shadowclient.module.ModuleManager;
import java.util.ArrayList;
import java.util.List;

/** Global (non-module) preferences: theme, animation, HUD-editor grid, layout. */
public final class ClientSettings {
    private ClientSettings() { }

    public static final Setting.Color accent = new Setting.Color("accent", "Accent Color", 0xE53935);
    public static final Setting.Choice animQuality = new Setting.Choice("quality", "Animation Quality", new String[]{"Off", "Low", "High"}, 2);
    public static final Setting.Bool bgAnim = new Setting.Bool("bgAnim", "Menu Background Animation", true);
    public static final Setting.Bool snap = new Setting.Bool("snap", "HUD Snap To Grid", true);
    public static final Setting.Num gridSize = new Setting.Num("gridSize", "HUD Grid Size", 5, 2, 20, 1, 0);
    public static final Setting.Bool gridLayout = new Setting.Bool("gridLayout", "Grid Layout (off = list)", true);

    /** Saved under "theme" in the config. animQuality/bgAnim are saved by the Animations module. */
    public static final List<Setting> THEME = List.of(accent, snap, gridSize, gridLayout);

    public static List<Setting> uiList() {
        List<Setting> l = new ArrayList<>();
        l.add(accent); l.add(animQuality); l.add(bgAnim); l.add(gridLayout); l.add(snap); l.add(gridSize);
        return l;
    }

    public static boolean anim() {
        return animQuality.index > 0 && (ModuleManager.animations == null || ModuleManager.animations.isEnabled());
    }
    public static boolean glow() { return anim() && animQuality.index >= 2; }
    public static int accent() { return 0xFF000000 | accent.rgb; }

    public static void resetAll() {
        accent.reset(); animQuality.reset(); bgAnim.reset(); snap.reset(); gridSize.reset(); gridLayout.reset();
    }
}
