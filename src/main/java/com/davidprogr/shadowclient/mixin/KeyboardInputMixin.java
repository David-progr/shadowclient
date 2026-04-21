package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureManager;

import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    // In 1.21.4, movement is stored in Input.playerInput (a PlayerInput record)
    // We override it after tick() to force sprinting when moving forward
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (!FeatureManager.TOGGLE_SPRINT.isEnabled()) return;

        KeyboardInput self = (KeyboardInput)(Object)this;
        PlayerInput pi = self.playerInput;
        if (pi == null) return;

        // Only apply sprint if actually moving forward
        if (pi.forward()) {
            self.playerInput = new PlayerInput(
                    pi.forward(),
                    pi.backward(),
                    pi.left(),
                    pi.right(),
                    pi.jump(),
                    true,   // sprint = true
                    pi.sneak()
            );
        }
    }
}
