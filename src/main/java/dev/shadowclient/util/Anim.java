package dev.shadowclient.util;

import dev.shadowclient.settings.ClientSettings;

/** Frame-rate independent exponential smoothing. Call Anim.frame() once per rendered frame. */
public final class Anim {
    private static long last = System.nanoTime();
    public static float dt = 0.016f;
    public float v;

    public Anim(float v) { this.v = v; }

    public static void frame() {
        long n = System.nanoTime();
        if (n - last < 500_000L) return;
        dt = Math.min(0.1f, (n - last) / 1_000_000_000f);
        last = n;
    }

    public float to(float target, float speed) {
        if (!ClientSettings.anim()) { v = target; return v; }
        v += (target - v) * (1f - (float) Math.exp(-speed * dt));
        if (Math.abs(target - v) < 0.002f) v = target;
        return v;
    }
}
