package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureManager;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // ---- Fullbright: override sky darkness to remove dark tint ----
    @Inject(method = "getSkyDarkness", at = @At("RETURN"), cancellable = true)
    private void onGetSkyDarkness(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (FeatureManager.FULLBRIGHT.isEnabled()) {
            cir.setReturnValue(0.0f);
        }
    }

    // ---- Zoom: narrow the FOV when zoom key is held ----
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov,
                           CallbackInfoReturnable<Double> cir) {
        if (FeatureManager.ZOOM.isEnabled()) {
            cir.setReturnValue(10.0);
        }
    }
}
