package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.visual.NoFogFeature;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.4 BackgroundRenderer.applyFog signature:
 *   applyFog(Camera, FogType, Vector4f, float, boolean, float) : Fog
 */
@Mixin(BackgroundRenderer.class)
public class FogRendererMixin {

    @Inject(
        method = "applyFog(Lnet/minecraft/client/render/Camera;" +
                 "Lnet/minecraft/client/render/BackgroundRenderer$FogType;" +
                 "Lorg/joml/Vector4f;FZF)" +
                 "Lnet/minecraft/client/render/Fog;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onApplyFog(Camera camera,
                                   BackgroundRenderer.FogType fogType,
                                   Vector4f color,
                                   float viewDistance,
                                   boolean thickenFog,
                                   float tickDelta,
                                   CallbackInfoReturnable<Fog> cir) {
        try {
            NoFogFeature nf = FeatureRegistry.get().noFog;
            if (nf != null && nf.isEnabled()) {
                // Return a far-away fog that is effectively invisible
                cir.setReturnValue(new Fog(viewDistance * 0.9f, viewDistance, net.minecraft.client.render.FogShape.SPHERE, color.x, color.y, color.z, color.w));
            }
        } catch (Exception ignored) {}
    }
}
