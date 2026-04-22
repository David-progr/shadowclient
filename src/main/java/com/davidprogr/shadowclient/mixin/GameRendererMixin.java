package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.visual.NoHurtCamFeature;
import com.davidprogr.shadowclient.feature.visual.ZoomFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
                if (zoomLevel < baseFov) {
                    cir.setReturnValue(zoom.smoothZoom.getValue()
                        ? zoom.getRenderedFov(baseFov)
                        : zoomLevel);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * NoHurtCam — zero out the player's hurtTime every render tick so
     * the camera-tilt calculation always sees 0 and skips the shake.
     * Hooks into the render() entry point which fires every frame.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(
            net.minecraft.client.render.RenderTickCounter tickCounter,
            boolean tick,
            CallbackInfo ci) {
        try {
            NoHurtCamFeature nhc = FeatureRegistry.get().noHurtCam;
            if (nhc == null || !nhc.isEnabled()) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.player.hurtTime = 0;
                mc.player.maxHurtTime = 0;
            }
        } catch (Exception ignored) {}
    }
}
