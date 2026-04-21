package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.visual.HUDFeature;
import com.davidprogr.shadowclient.render.HUDRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InGameHudMixin — hooks into InGameHud.render() to inject our custom HUD.
 * In 1.21.4 the signature is render(DrawContext, RenderTickCounter).
 * renderCrosshair no longer exists as a separate method in 1.21.4,
 * so custom crosshair is drawn as part of our HUDRenderer instead.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    /**
     * Inject at TAIL so we draw after vanilla HUD elements.
     * 1.21.4 signature: render(DrawContext context, RenderTickCounter tickCounter)
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            FeatureRegistry reg = FeatureRegistry.get();
            HUDFeature hud = reg.hud;
            float tickDelta = tickCounter.getTickDelta(false);
            if (hud != null && hud.isEnabled()) {
                HUDRenderer.render(context, tickDelta);
            }
        } catch (Exception ignored) {}
    }
}
