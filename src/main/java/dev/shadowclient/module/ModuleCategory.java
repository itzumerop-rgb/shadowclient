package dev.shadowclient.module;

public enum ModuleCategory {
    ALL("All"), NEW("New"), HUD("HUD"), PERFORMANCE("Performance"), PVP("PvP"), MOVEMENT("Movement"), VISUAL("Visual"), OTHER("Other");
    public final String label;
    ModuleCategory(String label) { this.label = label; }
}
