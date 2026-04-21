package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.visual.ZoomFeature;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * getFov returns float in 1.21.4.
     * ZoomFeature overrides the FoV by dividing by the zoom level.
     */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta,
                          boolean changingFov, CallbackInfoReturnable<Float> cir) {
        try {
            ZoomFeature zoom = FeatureRegistry.get().zoom;
            if (zoom != null && zoom.isEnabled()) {
                float baseFov   = cir.getReturnValue();
                float zoomLevel = zoom.fov.getValue().floatValue();
                // fov setting is the desired FOV in degrees, not a divisor
                // if baseFov > zoomLevel, apply zoom
                if (zoomLevel < baseFov) {
                    if (zoom.smoothZoom.getValue()) {
                        float rendered = zoom.getRenderedFov(baseFov);
                        cir.setReturnValue(rendered);
                    } else {
                        cir.setReturnValue(zoomLevel);
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
