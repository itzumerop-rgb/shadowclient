package dev.shadowclient;

import dev.shadowclient.config.ConfigManager;
import dev.shadowclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shadow Client by ItzZShad0w_ - entrypoint. */
public class ShadowClient implements ClientModInitializer {
    public static final String MOD_ID = "shadowclient";
    public static final String NAME = "Shadow Client";
    public static final String AUTHOR = "ItzZShad0w_";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    private static boolean firstTick = true;

    @Override
    public void onInitializeClient() {
        ConfigManager.init();
        ModuleManager.init();
        ConfigManager.load();
        KeybindManager.init();
        HudManager.init();
        ClientTickEvents.END_CLIENT_TICK.register(ShadowClient::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ModuleManager.onWorldLeave());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.saveNow());
        LOGGER.info("{} by {} loaded ({} modules)", NAME, AUTHOR, ModuleManager.all().length);
    }

    private static void tick(MinecraftClient mc) {
        if (firstTick) { firstTick = false; ModuleManager.activateLoaded(); }
        KeybindManager.tick(mc);
        ModuleManager.tick();
        ConfigManager.tick();
    }
}
