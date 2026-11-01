package dev.shadowclient.config;

import com.google.gson.*;
import dev.shadowclient.ShadowClient;
import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleManager;
import dev.shadowclient.settings.ClientSettings;
import dev.shadowclient.settings.Setting;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import net.fabricmc.loader.api.FabricLoader;

/** JSON config in .minecraft/config/shadowclient/config.json. Debounced, atomic writes; no polling. */
public final class ConfigManager {
    private ConfigManager() { }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path dir, file;
    private static boolean dirty;
    private static int idle;

    public static void init() {
        dir = FabricLoader.getInstance().getConfigDir().resolve("shadowclient");
        file = dir.resolve("config.json");
        try { Files.createDirectories(dir); } catch (IOException e) { ShadowClient.LOGGER.error("Cannot create config dir", e); }
    }

    public static void markDirty() { dirty = true; idle = 0; }
    /** Called every client tick; writes ~2 seconds after the last change. */
    public static void tick() { if (dirty && ++idle >= 40) saveNow(); }

    public static void load() {
        JsonObject root = null;
        try {
            if (Files.exists(file)) {
                try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { root = JsonParser.parseReader(r).getAsJsonObject(); }
            } else {
                try (InputStream in = ConfigManager.class.getResourceAsStream("/assets/shadowclient/default_config.json")) {
                    if (in != null) root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            ShadowClient.LOGGER.error("Config unreadable, using defaults (old file kept as config.json.broken)", e);
            try { Files.move(file, dir.resolve("config.json.broken"), StandardCopyOption.REPLACE_EXISTING); } catch (Exception ignored) { }
        }
        if (root != null) apply(root);
        boolean exists = Files.exists(file);
        dirty = false;
        if (!exists) saveNow();
    }

    private static JsonObject obj(JsonObject o, String k) {
        return o != null && o.has(k) && o.get(k).isJsonObject() ? o.getAsJsonObject(k) : null;
    }

    private static void apply(JsonObject root) {
        JsonObject theme = obj(root, "theme");
        if (theme != null) for (Setting s : ClientSettings.THEME) if (theme.has(s.key)) s.load(theme.get(s.key));
        JsonObject mods = obj(root, "modules"), hud = obj(root, "hud"), keys = obj(root, "keybinds");
        for (Module m : ModuleManager.all()) {
            JsonObject jm = obj(mods, m.id);
            if (jm != null) {
                if (jm.has("enabled") && m.persistEnabled) m.setEnabledRaw(jm.get("enabled").getAsBoolean());
                if (jm.has("favorite")) m.setFavorite(jm.get("favorite").getAsBoolean());
                JsonObject st = obj(jm, "settings");
                if (st != null) for (Setting s : m.settings()) if (s.persist && st.has(s.key)) s.load(st.get(s.key));
            }
            if (keys != null && keys.has(m.id)) m.keybind.load(keys.get(m.id));
            if (m instanceof HudModule hm) {
                JsonObject jh = obj(hud, m.id);
                if (jh != null) {
                    if (jh.has("x")) hm.nx = clamp01(jh.get("x").getAsFloat());
                    if (jh.has("y")) hm.ny = clamp01(jh.get("y").getAsFloat());
                    if (jh.has("scale")) hm.scale.load(jh.get("scale"));
                    if (jh.has("visible")) hm.visible.load(jh.get("visible"));
                }
            }
        }
    }
    private static float clamp01(float f) { return Math.max(0f, Math.min(1f, f)); }

    public static void saveNow() {
        if (file == null) return;
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        JsonObject theme = new JsonObject();
        for (Setting s : ClientSettings.THEME) theme.add(s.key, s.save());
        root.add("theme", theme);
        JsonObject mods = new JsonObject(), hud = new JsonObject(), keys = new JsonObject();
        for (Module m : ModuleManager.all()) {
            JsonObject jm = new JsonObject();
            jm.addProperty("enabled", m.isEnabled());
            jm.addProperty("favorite", m.isFavorite());
            JsonObject st = new JsonObject();
            for (Setting s : m.settings()) if (s.persist) st.add(s.key, s.save());
            jm.add("settings", st);
            mods.add(m.id, jm);
            keys.add(m.id, m.keybind.save());
            if (m instanceof HudModule hm) {
                JsonObject jh = new JsonObject();
                jh.addProperty("x", hm.nx); jh.addProperty("y", hm.ny);
                jh.add("scale", hm.scale.save()); jh.add("visible", hm.visible.save());
                hud.add(m.id, jh);
            }
        }
        root.add("modules", mods); root.add("hud", hud); root.add("keybinds", keys);
        try {
            Files.createDirectories(dir);
            Path tmp = dir.resolve("config.json.tmp");
            Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8);
            try { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING); }
            dirty = false; idle = 0;
        } catch (IOException e) {
            ShadowClient.LOGGER.error("Failed to save config", e);
        }
    }
}
