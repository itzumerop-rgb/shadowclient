package dev.shadowclient.mixin;

import dev.shadowclient.module.misc.Freecam;
import dev.shadowclient.module.misc.Freelook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Redirects mouse look to the freecam / freelook camera instead of the real player. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void shadowclient$look(double dx, double dy, CallbackInfo ci) {
        if ((Object) this != MinecraftClient.getInstance().player) return;
        if (Freecam.isActive()) { Freecam.rotate(dx, dy); ci.cancel(); }
        else if (Freelook.isActive()) { Freelook.rotate(dx, dy); ci.cancel(); }
    }
}
