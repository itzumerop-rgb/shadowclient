package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import net.minecraft.client.gui.DrawContext;

public class CpsCounter extends HudModule {
    private static final long[] L = new long[64], R = new long[64];
    private static int lh, rh;
    private static boolean hooked;

    /** Called from MinecraftClientMixin on every real attack / use action. */
    public static void left() { hooked = true; push(L, true); }
    public static void right() { hooked = true; push(R, false); }
    private static void push(long[] a, boolean l) {
        long now = System.currentTimeMillis();
        if (l) { a[lh] = now; lh = (lh + 1) & 63; } else { a[rh] = now; rh = (rh + 1) & 63; }
    }
    private static int count(long[] a, long now) { int n = 0; for (long t : a) if (t != 0 && now - t <= 1000) n++; return n; }

    private final Setting.Bool showRight = add(new Setting.Bool("right", "Show Right Click", true));
    private boolean pa, pu;
    private int lc = -1, rc = -1, lastShow = -1;
    private String str = "CPS: 0";

    public CpsCounter() { super("cps", "CPS Counter", "Clicks per second for left and right mouse.", "cps", ModuleCategory.PVP, true, 0.008f, 0.075f); }

    @Override public void onTick() {
        if (mc.player == null) return;
        if (!hooked) { // fallback if the click mixin did not apply: sample key edges once per tick
            boolean a = mc.options.attackKey.isPressed(), u = mc.options.useKey.isPressed();
            if (a && !pa) push(L, true);
            if (u && !pu) push(R, false);
            pa = a; pu = u;
        }
        long now = System.currentTimeMillis();
        int l = count(L, now), r = count(R, now);
        int show = showRight.value ? 1 : 0;
        if (l != lc || r != rc || show != lastShow) {
            lc = l; rc = r; lastShow = show;
            str = show == 1 ? "CPS: " + l + " | " + r : "CPS: " + l;
        }
    }

    @Override public void draw(DrawContext c, boolean preview) {
        label(c, preview ? (showRight.value ? "CPS: 8 | 5" : "CPS: 8") : str);
    }
}
