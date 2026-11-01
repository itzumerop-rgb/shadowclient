package dev.shadowclient.module.performance;

import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.ClientSettings;

/** Owns the global animation quality / menu background animation settings. Disable the module to turn all decorative animation off. */
public class AnimationControl extends Module {
    public AnimationControl() {
        super("animations", "Animations", "Menu animations, glow and background effects. Disable for max performance.", "anim", ModuleCategory.PERFORMANCE, true);
        add(ClientSettings.animQuality);
        add(ClientSettings.bgAnim);
    }
}
