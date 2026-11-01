package dev.shadowclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.shadowclient.config.ConfigManager;

/** Base class for every configurable value. Subclasses know how to save/load themselves. */
public abstract class Setting {
    public final String key, label;
    /** If false the owner (e.g. HUD section / keybind section) serialises this value instead. */
    public boolean persist = true;

    protected Setting(String key, String label) { this.key = key; this.label = label; }

    public abstract JsonElement save();
    public abstract void load(JsonElement e);
    public abstract void reset();
    protected static void dirty() { ConfigManager.markDirty(); }

    public static final class Bool extends Setting {
        public boolean value;
        private final boolean def;
        public Bool(String k, String l, boolean d) { super(k, l); value = def = d; }
        public void set(boolean v) { if (v != value) { value = v; dirty(); } }
        public void toggle() { set(!value); }
        @Override public JsonElement save() { return new JsonPrimitive(value); }
        @Override public void load(JsonElement e) { try { value = e.getAsBoolean(); } catch (Exception ignored) { } }
        @Override public void reset() { set(def); }
    }

    public static final class Num extends Setting {
        public double value;
        public final double min, max, step, def;
        public final int decimals;
        public Num(String k, String l, double d, double min, double max, double step, int decimals) {
            super(k, l); this.def = d; this.min = min; this.max = max; this.step = step; this.decimals = decimals; this.value = d;
        }
        public void set(double v) {
            v = Math.max(min, Math.min(max, v));
            if (step > 0) v = Math.round((v - min) / step) * step + min;
            v = Math.round(v * 10000.0) / 10000.0;
            v = Math.max(min, Math.min(max, v));
            if (v != value) { value = v; dirty(); }
        }
        public int i() { return (int) Math.round(value); }
        public float f() { return (float) value; }
        public double fraction() { return max == min ? 0 : (value - min) / (max - min); }
        public String display() { return decimals <= 0 ? Integer.toString(i()) : String.format(java.util.Locale.ROOT, "%." + decimals + "f", value); }
        @Override public JsonElement save() { return new JsonPrimitive(value); }
        @Override public void load(JsonElement e) { try { set(e.getAsDouble()); } catch (Exception ignored) { } }
        @Override public void reset() { set(def); }
    }

    public static final class Choice extends Setting {
        public final String[] options;
        public int index;
        private final int def;
        public Choice(String k, String l, String[] options, int def) { super(k, l); this.options = options; this.index = this.def = def; }
        public String get() { return options[index]; }
        public void set(int i) { if (i >= 0 && i < options.length && i != index) { index = i; dirty(); } }
        public void next() { set((index + 1) % options.length); }
        @Override public JsonElement save() { return new JsonPrimitive(index); }
        @Override public void load(JsonElement e) { try { int i = e.getAsInt(); if (i >= 0 && i < options.length) index = i; } catch (Exception ignored) { } }
        @Override public void reset() { set(def); }
    }

    public static final class Color extends Setting {
        public int rgb;
        public float h, s, v;
        private final int def;
        public Color(String k, String l, int d) { super(k, l); rgb = def = d & 0xFFFFFF; sync(); }
        private void sync() {
            float r = ((rgb >> 16) & 255) / 255f, g = ((rgb >> 8) & 255) / 255f, b = (rgb & 255) / 255f;
            float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b)), d = max - min;
            v = max; s = max == 0 ? 0 : d / max;
            if (d == 0) h = 0; else {
                float hh;
                if (max == r) hh = ((g - b) / d) % 6f; else if (max == g) hh = (b - r) / d + 2f; else hh = (r - g) / d + 4f;
                hh /= 6f; if (hh < 0) hh += 1f; h = hh;
            }
        }
        public void setRgb(int c) { c &= 0xFFFFFF; if (c != rgb) { rgb = c; sync(); dirty(); } }
        public void setHsv(float h, float s, float v) {
            this.h = Math.max(0, Math.min(1, h)); this.s = Math.max(0, Math.min(1, s)); this.v = Math.max(0, Math.min(1, v));
            rgb = hsvToRgb(this.h, this.s, this.v); dirty();
        }
        public static int hsvToRgb(float h, float s, float v) {
            float hh = (h - (float) Math.floor(h)) * 6f; int i = (int) hh; float f = hh - i;
            float p = v * (1 - s), q = v * (1 - s * f), t = v * (1 - s * (1 - f)); float r, g, b;
            switch (i % 6) {
                case 0: r = v; g = t; b = p; break; case 1: r = q; g = v; b = p; break;
                case 2: r = p; g = v; b = t; break; case 3: r = p; g = q; b = v; break;
                case 4: r = t; g = p; b = v; break; default: r = v; g = p; b = q;
            }
            return ((int) (r * 255 + .5f) << 16) | ((int) (g * 255 + .5f) << 8) | (int) (b * 255 + .5f);
        }
        @Override public JsonElement save() { return new JsonPrimitive(rgb); }
        @Override public void load(JsonElement e) { try { rgb = e.getAsInt() & 0xFFFFFF; sync(); } catch (Exception ignored) { } }
        @Override public void reset() { setRgb(def); }
    }

    public static final class Key extends Setting {
        public int code;
        private final int def;
        public Key(String k, String l, int d) { super(k, l); code = def = d; }
        public void set(int c) { if (c != code) { code = c; dirty(); } }
        @Override public JsonElement save() { return new JsonPrimitive(code); }
        @Override public void load(JsonElement e) { try { code = e.getAsInt(); } catch (Exception ignored) { } }
        @Override public void reset() { set(def); }
    }
}
