package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.cosmetic.CustomCrosshairFeature;
import com.davidprogr.shadowclient.feature.misc.HitmarkerFeature;
import com.davidprogr.shadowclient.feature.visual.HUDFeature;
import com.davidprogr.shadowclient.render.HUDRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InGameHudMixin — hooks render(DrawContext, RenderTickCounter) in 1.21.4.
 *
 * Draws at TAIL (after all vanilla HUD elements):
 *  1. Custom HUD overlay (coords, FPS, armour, potions, target health…)
 *  2. Custom crosshair (drawn over vanilla's — vanilla crosshair stays unless
 *     CustomCrosshair hides it; toggling hudHidden isn't needed)
 *  3. Hitmarker overlay
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            FeatureRegistry reg = FeatureRegistry.get();
            float tickDelta = tickCounter.getTickDelta(false);
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null) return;

            // ── Custom HUD ───────────────────────────────────────────────
            HUDFeature hud = reg.hud;
            if (hud != null && hud.isEnabled()) {
                HUDRenderer.render(ctx, tickDelta);
            }

            // ── Custom Crosshair ─────────────────────────────────────────
            CustomCrosshairFeature crosshair = reg.customCrosshair;
            if (crosshair != null && crosshair.isEnabled()) {
                crosshair.render(ctx);
            }

            // ── Hitmarker ────────────────────────────────────────────────
            HitmarkerFeature hm = reg.hitmarker;
            if (hm != null && hm.isEnabled() && hm.shouldRender()) {
                renderHitmarker(ctx, hm, mc);
            }

        } catch (Exception ignored) {}
    }

    private void renderHitmarker(DrawContext ctx, HitmarkerFeature hm, MinecraftClient mc) {
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2, cy = sh / 2;
        int sz = hm.size.getValue().intValue();

        int col;
        switch (hm.color.getValue()) {
            case "Red"   -> col = 0xFFFF3333;
            case "Green" -> col = 0xFF33FF33;
            case "Cyan"  -> col = 0xFF00FFFF;
            default      -> col = 0xFFFFFFFF;
        }

        // Four diagonal lines — classic FPS hitmarker
        // Top-left
        ctx.fill(cx - sz,     cy - sz,     cx - sz / 2, cy - sz / 2, col);
        // Top-right
        ctx.fill(cx + sz / 2, cy - sz,     cx + sz,     cy - sz / 2, col);
        // Bottom-left
        ctx.fill(cx - sz,     cy + sz / 2, cx - sz / 2, cy + sz,     col);
        // Bottom-right
        ctx.fill(cx + sz / 2, cy + sz / 2, cx + sz,     cy + sz,     col);
    }
}
