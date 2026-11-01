package dev.shadowclient.mixin;

import dev.shadowclient.module.misc.Freecam;
import dev.shadowclient.module.misc.Freelook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPos(double x, double y, double z);
    @Shadow public abstract boolean isThirdPerson();

    @Inject(method = "update", at = @At("TAIL"))
    private void shadowclient$update(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (Freecam.isActive()) {
            float t = mc.getRenderTickCounter().getTickProgress(true);
            setRotation(Freecam.yaw, Freecam.pitch);
            setPos(Freecam.x(t), Freecam.y(t), Freecam.z(t));
        } else if (Freelook.isActive() && mc.player != null) {
            setRotation(Freelook.yaw, Freelook.pitch);
            if (isThirdPerson()) {
                float t = mc.getRenderTickCounter().getTickProgress(true);
                Entity e = mc.player;
                Vec3d eye = e.getLerpedPos(t).add(0, e.getStandingEyeHeight(), 0);
                Vec3d back = Vec3d.fromPolar(Freelook.pitch, Freelook.yaw).multiply(Freelook.distance());
                setPos(eye.x - back.x, eye.y - back.y, eye.z - back.z);
            }
        }
    }
}
