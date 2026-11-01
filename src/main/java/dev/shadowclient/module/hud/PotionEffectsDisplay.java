package dev.shadowclient.module.hud;

import dev.shadowclient.module.HudModule;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import dev.shadowclient.util.Draw;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionEffectsDisplay extends HudModule {
    private static final String[] ROMAN = {"", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};
    private final Setting.Bool timer = add(new Setting.Bool("timer", "Show Duration", true));
    private final List<String> lines = new ArrayList<>();
    private final List<Integer> colors = new ArrayList<>();
    private final List<String> times = new ArrayList<>();

    public PotionEffectsDisplay() { super("potions", "Potion Effects", "Lists your active status effects with timers.", "potion", ModuleCategory.HUD, true, 0.008f, 0.34f); }

    private static String time(StatusEffectInstance e) {
        if (e.isInfinite()) return "**:**";
        int s = Math.max(0, e.getDuration() / 20);
        int m = s / 60; s %= 60;
        return (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    }

    @Override public void onTick() {
        if (mc.player == null || !tickGate()) return;
        lines.clear(); colors.clear(); times.clear();
        for (StatusEffectInstance e : mc.player.getStatusEffects()) {
            int amp = e.getAmplifier();
            lines.add(e.getEffectType().value().getName().getString() + (amp < ROMAN.length ? ROMAN[amp] : " " + (amp + 1)));
            colors.add(0xFF000000 | e.getEffectType().value().getColor());
            times.add(time(e));
        }
    }

    @Override public void draw(DrawContext c, boolean preview) {
        List<String> l = lines, t = times; List<Integer> col = colors;
        if (preview) {
            l = List.of("Speed II", "Strength I", "Regeneration II"); t = List.of("01:30", "03:00", "00:12");
            col = List.of(0xFF7CAFC6, 0xFF932423, 0xFFCD5CAB);
        }
        if (l.isEmpty()) { w = 0; h = 0; return; }
        int widest = 0;
        for (int i = 0; i < l.size(); i++) widest = Math.max(widest, Draw.w(l.get(i)) + (timer.value ? 8 + Draw.w(t.get(i)) : 0));
        box(c, widest + 22, l.size() * 12 + 4);
        for (int i = 0; i < l.size(); i++) {
            int y = 3 + i * 12;
            Draw.rrect(c, 4, y + 1, 7, 7, 2, col.get(i));
            text(c, l.get(i), 15, y);
            if (timer.value) Draw.text(c, t.get(i), 15 + widest - Draw.w(t.get(i)), y, 0xFFB5B6BE, shadow.value);
        }
    }
}
