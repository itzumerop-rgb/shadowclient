package dev.shadowclient.module;

import dev.shadowclient.config.ConfigManager;
import dev.shadowclient.settings.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;

public abstract class Module {
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    public final String id, name, description, icon;
    public final ModuleCategory category;
    public final boolean enabledByDefault;
    public boolean isNew;
    /** Hold-style keybind handled by the module itself (zoom, freelook...) instead of toggling the module. */
    public boolean holdKey;
    /** False for modules whose "enabled" state must not survive a restart (Freecam). */
    public boolean persistEnabled = true;
    public Setting.Key keybind = new Setting.Key("key", "Keybind", 0);

    private boolean enabled, favorite;
    protected final List<Setting> settings = new ArrayList<>();
    private List<Setting> uiList;

    protected Module(String id, String name, String desc, String icon, ModuleCategory cat, boolean enabledByDefault) {
        this.id = id; this.name = name; this.description = desc; this.icon = icon; this.category = cat;
        this.enabledByDefault = enabledByDefault; this.enabled = enabledByDefault;
        keybind.persist = false;
    }

    /** Declares the default key (also what "reset to defaults" restores). */
    protected void defaultKey(int code) { keybind = new Setting.Key("key", "Keybind", code); keybind.persist = false; uiList = null; }

    protected <T extends Setting> T add(T s) { settings.add(s); return s; }
    public List<Setting> settings() { return settings; }
    /** Settings plus the keybind row, for UI. */
    public List<Setting> uiSettings() {
        if (uiList == null) { uiList = new ArrayList<>(settings); uiList.add(keybind); }
        return uiList;
    }

    public boolean isEnabled() { return enabled; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean f) { if (f != favorite) { favorite = f; ConfigManager.markDirty(); } }
    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean v) {
        if (v == enabled) return;
        if (v && !canEnable()) return;
        enabled = v;
        if (v) onEnable(); else onDisable();
        ConfigManager.markDirty();
    }
    /** Used by the config loader; lifecycle callbacks run later from ModuleManager.activateLoaded(). */
    public void setEnabledRaw(boolean v) { enabled = v; }

    public void resetSettings() { for (Setting s : settings) s.reset(); keybind.reset(); }

    protected boolean canEnable() { return true; }
    public void onEnable() { }
    public void onDisable() { }
    public void onTick() { }
    public void onWorldLeave() { }
}
