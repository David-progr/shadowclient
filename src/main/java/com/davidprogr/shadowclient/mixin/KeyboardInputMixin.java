package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureManager;

import net.minecraft.client.input.KeyboardInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    // ---- Toggle Sprint: force sprint on every input tick ----
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (FeatureManager.TOGGLE_SPRINT.isEnabled()) {
            KeyboardInput self = (KeyboardInput)(Object)this;
            // Only sprint when actually moving forward
            if (self.pressingForward) {
                self.sprinting = true;
            }
        }
    }
}
