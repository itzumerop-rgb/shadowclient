package dev.shadowclient;

import dev.shadowclient.config.ConfigManager;
import dev.shadowclient.gui.HudEditorScreen;
import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleManager;
import dev.shadowclient.module.misc.Zoom;
import dev.shadowclient.module.performance.HudOptimizer;
import dev.shadowclient.util.Anim;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;

public final class HudManager {
    private HudManager() { }
    /** Set by the Toggle HUD module. */
    public static boolean hidden;

    @SuppressWarnings("deprecation")
    public static void init() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> render(ctx));
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Anim.frame();
        Zoom.frame();
        if (mc.player == null || hidden || mc.options.hudHidden) return;
        if (mc.currentScreen instanceof HudEditorScreen) return;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && HudOptimizer.hideBehindScreens()) return;
        HudModule[] huds = ModuleManager.huds();
        for (int i = 0; i < huds.length; i++) {
            HudModule m = huds[i];
            if (m.isEnabled() && m.visible.value) drawAt(ctx, m, false);
        }
    }

    /** Draws one widget at its saved position/scale. Faults disable the widget instead of crashing the HUD. */
    public static void drawAt(DrawContext ctx, HudModule m, boolean preview) {
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        float s = m.scale.f();
        ctx.getMatrices().pushMatrix();
        try {
            ctx.getMatrices().translate((float) m.pixelX(sw), (float) m.pixelY(sh));
            ctx.getMatrices().scale(s, s);
            m.draw(ctx, preview);
        } catch (Throwable t) {
            ShadowClient.LOGGER.error("HUD module {} failed to draw, disabling", m.id, t);
            m.setEnabledRaw(false);
        } finally {
            ctx.getMatrices().popMatrix();
        }
    }

    public static void resetAll() {
        for (HudModule m : ModuleManager.huds()) m.resetLayout();
        ConfigManager.markDirty();
    }
}
