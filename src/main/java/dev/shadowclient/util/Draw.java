package dev.shadowclient.util;

import java.util.HashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Cheap 2D drawing helpers: rounded rects, borders, glow, cached text measurement. */
public final class Draw {
    private Draw() { }
    /** Global alpha multiplier applied to everything drawn through this class. */
    public static float alpha = 1f;

    private static final HashMap<String, Integer> WIDTHS = new HashMap<>();
    private static final HashMap<String, String> FIT = new HashMap<>();
    private static final int[][] INSET = new int[10][];
    static {
        for (int r = 0; r < INSET.length; r++) {
            INSET[r] = new int[r];
            for (int i = 0; i < r; i++) {
                double dy = r - i - 0.2;
                INSET[r][i] = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            }
        }
    }

    public static TextRenderer tr() { return MinecraftClient.getInstance().textRenderer; }

    public static int mul(int col) {
        if (alpha >= 0.999f) return col;
        return ((int) ((col >>> 24) * alpha) << 24) | (col & 0xFFFFFF);
    }
    public static int withAlpha(int col, float f) {
        return ((int) ((col >>> 24) * Math.max(0f, Math.min(1f, f))) << 24) | (col & 0xFFFFFF);
    }
    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (int) ((a >>> 24) + ((b >>> 24) - (a >>> 24)) * t);
        int rr = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int gg = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bb = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (aa << 24) | (rr << 16) | (gg << 8) | bb;
    }

    public static void rect(DrawContext c, int x, int y, int w, int h, int col) {
        col = mul(col);
        if ((col >>> 24) != 0) c.fill(x, y, x + w, y + h, col);
    }

    public static void rrect(DrawContext c, int x, int y, int w, int h, int r, int col) {
        col = mul(col);
        if ((col >>> 24) == 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0 || r >= INSET.length) { c.fill(x, y, x + w, y + h, col); return; }
        c.fill(x, y + r, x + w, y + h - r, col);
        int[] in = INSET[r];
        for (int i = 0; i < r; i++) {
            c.fill(x + in[i], y + i, x + w - in[i], y + i + 1, col);
            c.fill(x + in[i], y + h - 1 - i, x + w - in[i], y + h - i, col);
        }
    }

    public static void border(DrawContext c, int x, int y, int w, int h, int r, int col) {
        col = mul(col);
        if ((col >>> 24) == 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0 || r >= INSET.length) {
            c.fill(x, y, x + w, y + 1, col); c.fill(x, y + h - 1, x + w, y + h, col);
            c.fill(x, y + 1, x + 1, y + h - 1, col); c.fill(x + w - 1, y + 1, x + w, y + h - 1, col);
            return;
        }
        int[] in = INSET[r];
        c.fill(x + r, y, x + w - r, y + 1, col); c.fill(x + r, y + h - 1, x + w - r, y + h, col);
        c.fill(x, y + r, x + 1, y + h - r, col); c.fill(x + w - 1, y + r, x + w, y + h - r, col);
        for (int i = 0; i < r; i++) {
            int a = in[i], b = i == 0 ? r : in[i - 1], span = Math.max(1, b - a);
            c.fill(x + a, y + i, x + a + span, y + i + 1, col);
            c.fill(x + w - a - span, y + i, x + w - a, y + i + 1, col);
            c.fill(x + a, y + h - 1 - i, x + a + span, y + h - i, col);
            c.fill(x + w - a - span, y + h - 1 - i, x + w - a, y + h - i, col);
        }
    }

    /** Soft glow made of a few fading 1px outlines. Cheap (n fills x 8). */
    public static void glow(DrawContext c, int x, int y, int w, int h, int r, int col, int n) {
        for (int k = 1; k <= n; k++) {
            float f = (n - k + 1f) / (n + 1f) * 0.38f;
            border(c, x - k, y - k, w + 2 * k, h + 2 * k, Math.min(r + k, 9), withAlpha(col, f));
        }
    }

    public static int w(String s) {
        Integer w = WIDTHS.get(s);
        if (w == null) {
            w = tr().getWidth(s);
            if (WIDTHS.size() > 1024) WIDTHS.clear();
            WIDTHS.put(s, w);
        }
        return w;
    }

    public static void text(DrawContext c, String s, int x, int y, int col, boolean shadow) {
        col = mul(col);
        if ((col >>> 24) < 4) return;
        c.drawText(tr(), s, x, y, col, shadow);
    }
    public static void ctext(DrawContext c, String s, int cx, int y, int col, boolean shadow) {
        text(c, s, cx - w(s) / 2, y, col, shadow);
    }
    public static void textScaled(DrawContext c, String s, int x, int y, int col, float sc, boolean shadow) {
        c.getMatrices().pushMatrix();
        c.getMatrices().translate((float) x, (float) y);
        c.getMatrices().scale(sc, sc);
        text(c, s, 0, 0, col, shadow);
        c.getMatrices().popMatrix();
    }

    /** Truncates with an ellipsis so the text fits maxW (unscaled font pixels). Cached. */
    public static String fit(String s, int maxW) {
        if (w(s) <= maxW) return s;
        String k = maxW + "|" + s;
        String r = FIT.get(k);
        if (r == null) {
            int end = s.length();
            while (end > 1 && w(s.substring(0, end) + "...") > maxW) end--;
            r = s.substring(0, end) + "...";
            if (FIT.size() > 512) FIT.clear();
            FIT.put(k, r);
        }
        return r;
    }

    public static void bitmap(DrawContext c, String[] bm, int x, int y, int sc, int col) {
        col = mul(col);
        if ((col >>> 24) == 0) return;
        for (int r = 0; r < bm.length; r++) {
            String row = bm[r]; int start = -1;
            for (int i = 0; i <= row.length(); i++) {
                boolean on = i < row.length() && row.charAt(i) == '#';
                if (on && start < 0) start = i;
                else if (!on && start >= 0) { c.fill(x + start * sc, y + r * sc, x + i * sc, y + (r + 1) * sc, col); start = -1; }
            }
        }
    }
}
