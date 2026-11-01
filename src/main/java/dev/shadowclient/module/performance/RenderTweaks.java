package dev.shadowclient.module.performance;

import dev.shadowclient.module.Module;
import dev.shadowclient.module.ModuleCategory;
import dev.shadowclient.settings.Setting;
import net.minecraft.client.option.CloudRenderMode;

/** Safe vanilla-option tweaks: entity render distance, entity shadows, clouds. Originals are restored on disable. */
public class RenderTweaks extends Module {
    private final Setting.Num entityDist = add(new Setting.Num("entityDist", "Entity Render Distance", 0.75, 0.5, 5.0, 0.25, 2));
    private final Setting.Bool noShadows = add(new Setting.Bool("noShadows", "Disable Entity Shadows", true));
    private final Setting.Bool noClouds = add(new Setting.Bool("noClouds", "Disable Clouds", false));
    private Double oDist; private Boolean oShadows; private CloudRenderMode oClouds;
    private double lastDist = -1; private boolean lastShadows, lastClouds;

    public RenderTweaks() { super("render_tweaks", "Render Tweaks", "Entity distance, shadows and clouds for extra FPS.", "render", ModuleCategory.PERFORMANCE, false); }

    @Override public void onEnable() {
        if (mc.options == null) return;
        if (oDist == null) { oDist = mc.options.getEntityDistanceScaling().getValue(); oShadows = mc.options.getEntityShadows().getValue(); oClouds = mc.options.getCloudRenderMode().getValue(); }
        apply();
    }
    private void apply() {
        mc.options.getEntityDistanceScaling().setValue(entityDist.value);
        mc.options.getEntityShadows().setValue(!noShadows.value && oShadows);
        mc.options.getCloudRenderMode().setValue(noClouds.value ? CloudRenderMode.OFF : oClouds);
        lastDist = entityDist.value; lastShadows = noShadows.value; lastClouds = noClouds.value;
    }
    @Override public void onTick() {
        if (oDist != null && (lastDist != entityDist.value || lastShadows != noShadows.value || lastClouds != noClouds.value)) apply();
    }
    @Override public void onDisable() {
        if (mc.options != null && oDist != null) {
            mc.options.getEntityDistanceScaling().setValue(oDist); mc.options.getEntityShadows().setValue(oShadows); mc.options.getCloudRenderMode().setValue(oClouds);
        }
        oDist = null; oShadows = null; oClouds = null;
    }
}
