package dev.shadowclient.module.performance;

import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import net.minecraft.client.option.ParticlesMode;

/** Uses the game's own particle setting, so it stays compatible with every other mod. Restores your choice when disabled. */
public class ParticleReduction extends Module {
    private final Setting.Choice level = add(new Setting.Choice("level", "Particle Level", new String[]{"Decreased", "Minimal"}, 0));
    private ParticlesMode original;
    private int lastLevel = -1;

    public ParticleReduction() { super("particles", "Particle Reduction", "Reduces particle count to save FPS in fights.", "particles", ModuleCategory.PERFORMANCE, false); }

    private ParticlesMode target() { return level.index == 0 ? ParticlesMode.DECREASED : ParticlesMode.MINIMAL; }

    @Override public void onEnable() {
        if (mc.options == null) return;
        if (original == null) original = mc.options.getParticles().getValue();
        mc.options.getParticles().setValue(target()); lastLevel = level.index;
    }
    @Override public void onTick() { if (level.index != lastLevel) onEnable(); }
    @Override public void onDisable() {
        if (mc.options != null && original != null) mc.options.getParticles().setValue(original);
        original = null; lastLevel = -1;
    }
}
