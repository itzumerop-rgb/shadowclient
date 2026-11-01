package dev.shadowclient.mixin;

import dev.shadowclient.module.misc.Freecam;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** While Freecam is active the real player must stand still. */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("TAIL"))
    private void shadowclient$tick(CallbackInfo ci) {
        if (Freecam.isActive()) { this.playerInput = PlayerInput.DEFAULT; this.movementVector = Vec2f.ZERO; }
    }
}
