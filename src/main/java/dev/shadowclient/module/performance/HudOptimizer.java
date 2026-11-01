package dev.shadowclient.module.performance;

import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.module.ModuleManager;
import dev.shadowclient.settings.Setting;

/** Cuts HUD work: widgets refresh cached text every N ticks and the HUD is skipped behind full menus. */
public class HudOptimizer extends Module {
    private final Setting.Bool hideBehind = add(new Setting.Bool("hideBehind", "Skip HUD Behind Menus", true));
    private final Setting.Num interval = add(new Setting.Num("interval", "Data Refresh Interval (ticks)", 2, 1, 10, 1, 0));

    public HudOptimizer() { super("hud_optimizer", "HUD Optimizer", "Skips hidden HUD work and refreshes cached HUD data less often.", "hud", ModuleCategory.PERFORMANCE, true); }

    public static int interval() { HudOptimizer h = ModuleManager.hudOptimizer; return h != null && h.isEnabled() ? h.interval.i() : 1; }
    public static boolean hideBehindScreens() { HudOptimizer h = ModuleManager.hudOptimizer; return h != null && h.isEnabled() && h.hideBehind.value; }
}
