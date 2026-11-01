package dev.shadowclient.util;

import java.util.HashMap;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

/** Version-sensitive helpers live here so API drift is a one-file fix. */
public final class Compat {
    private Compat() { }
    private static final HashMap<Integer, String> NAMES = new HashMap<>();

    public static boolean isKeyDown(int code) {
        if (code <= 0) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null) return false;
        // 1.21.9+ takes a Window; on older versions use mc.getWindow().getHandle()
        return InputUtil.isKeyPressed(mc.getWindow(), code);
    }

    public static String keyName(int code) {
        if (code <= 0) return "NONE";
        String n = NAMES.get(code);
        if (n == null) {
            n = InputUtil.Type.KEYSYM.createFromCode(code).getLocalizedText().getString().toUpperCase(Locale.ROOT);
            NAMES.put(code, n);
        }
        return n;
    }
}
