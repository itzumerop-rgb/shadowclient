package dev.shadowclient;

import dev.shadowclient.gui.HudEditorScreen;
import dev.shadowclient.gui.ShadowClickGui;
import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleManager;
import dev.shadowclient.util.Compat;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeybindManager {
    private KeybindManager() { }
    public static KeyBinding openMenu, openEditor;
    private static boolean[] prev = new boolean[0];

    public static void init() {
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of(ShadowClient.MOD_ID, "main"));
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.shadowclient.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        openEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.shadowclient.open_hud_editor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_CONTROL, cat));
        prev = new boolean[ModuleManager.all().length];
    }

    public static void tick(MinecraftClient mc) {
        boolean open = false, edit = false;
        while (openMenu.wasPressed()) open = true;
        while (openEditor.wasPressed()) edit = true;
        if (mc.player != null && mc.currentScreen == null) {
            if (open) mc.setScreen(new ShadowClickGui());
            else if (edit) mc.setScreen(new HudEditorScreen());
        }
        Module[] all = ModuleManager.all();
        boolean free = mc.player != null && mc.currentScreen == null;
        for (int i = 0; i < all.length; i++) {
            Module m = all[i];
            int code = m.keybind.code;
            if (m.holdKey || code <= 0) { prev[i] = false; continue; }
            boolean down = Compat.isKeyDown(code);
            if (down && !prev[i] && free) m.toggle();
            prev[i] = down;
        }
    }
}
