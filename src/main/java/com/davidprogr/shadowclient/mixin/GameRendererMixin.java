package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.ShadowClientMod;
import com.davidprogr.shadowclient.feature.FeatureManager;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.option.GameOptions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // ---- Fullbright: override gamma ----
    @Inject(method = "getSkyDarkness", at = @At("RETURN"), cancellable = true)
    private void onGetSkyDarkness(float delta, CallbackInfoReturnable<Float> cir) {
        if (FeatureManager.FULLBRIGHT.isEnabled()) {
            cir.setReturnValue(0.0f);
        }
    }

    // ---- Zoom: shrink FOV when V is held ----
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(net.minecraft.client.render.Camera camera, float tickDelta, boolean changingFov,
                           CallbackInfoReturnable<Double> cir) {
        if (FeatureManager.ZOOM.isEnabled()) {
            cir.setReturnValue(10.0);
        }
    }
}
