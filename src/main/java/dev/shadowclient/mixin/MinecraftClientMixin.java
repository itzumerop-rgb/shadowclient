package dev.shadowclient.mixin;

import dev.shadowclient.module.hud.CpsCounter;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Counts real attack / use actions for the CPS counter (read-only; changes no behaviour). */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "doAttack", at = @At("HEAD"))
    private void shadowclient$attack(CallbackInfoReturnable<Boolean> cir) { CpsCounter.left(); }

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void shadowclient$use(CallbackInfo ci) { CpsCounter.right(); }
}
