package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

public class PingDisplay extends HudModule {
    private final Setting.Bool dot = add(new Setting.Bool("dot", "Colored Indicator", true));
    private int ms = -1, lastMs = -2; private String str = "Ping: --";
    private int tick;

    public PingDisplay() { super("ping", "Ping Display", "Shows your latency to the server.", "ping", ModuleCategory.HUD, false, 0.92f, 0.07f); }

    @Override public void onTick() {
        if (mc.player == null || ++tick % 20 != 0) return;
        ClientPlayNetworkHandler h = mc.getNetworkHandler();
        PlayerListEntry e = h == null ? null : h.getPlayerListEntry(mc.player.getUuid());
        ms = e == null ? -1 : e.getLatency();
        if (ms != lastMs) { lastMs = ms; str = ms < 0 ? "Ping: --" : "Ping: " + ms + "ms"; }
    }

    @Override public void draw(DrawContext c, boolean preview) {
        int v = preview ? 42 : ms;
        String s = preview ? "Ping: 42ms" : str;
        box(c, Draw.w(s) + (dot.value ? 18 : 10), 14);
        int x = 5;
        if (dot.value) {
            int col = v < 0 ? 0xFF6B6C74 : v < 80 ? 0xFF4CD964 : v < 160 ? 0xFFFFC043 : 0xFFFF4D4D;
            Draw.rrect(c, 5, 4, 6, 6, 3, col); x = 14;
        }
        text(c, s, x, 3);
    }
}
