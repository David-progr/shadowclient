package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.visual.ESPFeature;
import com.davidprogr.shadowclient.feature.visual.TracersFeature;
import com.davidprogr.shadowclient.feature.visual.OreSimulatorFeature;
import com.davidprogr.shadowclient.render.ESPRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WorldRendererMixin — hooks into the world render pass to draw:
 *   • ESP bounding boxes around entities
 *   • Tracer lines to entities
 *
 * Hooks into WorldRenderer.render() at RETURN so we draw after all world geometry.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    /**
     * Called after the main world render each frame.
     * We use this to invoke ESPRenderer which draws ESP boxes and tracers.
     * The render() method in 1.21.4 takes these parameters (confirmed via javap).
     */
    @Inject(
        method = "render",
        at = @At("RETURN")
    )
    private void onRender(
            net.minecraft.client.util.ObjectAllocator objectAllocator,
            net.minecraft.client.render.RenderTickCounter renderTickCounter,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            Matrix4f viewMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci) {
        try {
            FeatureRegistry reg = FeatureRegistry.get();
            ESPFeature esp = reg.esp;
            TracersFeature tracers = reg.tracers;
            OreSimulatorFeature oreCheck = reg.oreSimulator;
            boolean anyActive = (esp != null && esp.isEnabled())
                             || (tracers != null && tracers.isEnabled())
                             || (oreCheck != null && oreCheck.isEnabled());
            if (!anyActive) return;

            // Build a fresh MatrixStack from the view matrix for our overlay
            MatrixStack matrices = new MatrixStack();
            // We render in world space via ESPRenderer — it handles its own matrix setup
            // Use the immediate VertexConsumerProvider from the render context
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc == null || mc.world == null) return;

            VertexConsumerProvider.Immediate immediate =
                    mc.getBufferBuilders().getEntityVertexConsumers();
            float tickDelta = renderTickCounter.getTickDelta(false);
            ESPRenderer.render(matrices, immediate, camera, tickDelta);

            // OreSimulator overlay
            OreSimulatorFeature oreSim = reg.oreSimulator;
            if (oreSim != null && oreSim.isEnabled()) {
                oreSim.render(matrices, immediate, camera);
            }

            immediate.draw(RenderLayer.LINES);
        } catch (Exception ignored) {}
    }

    /**
     * XRay: hook into scheduleBlockRerenderIfNeeded so we can trigger
     * re-render when XRay is toggled. In 1.21.4 this takes (BlockPos, BlockState, BlockState).
     */
    @Inject(method = "scheduleBlockRerenderIfNeeded", at = @At("HEAD"))
    private void onScheduleRerender(BlockPos pos, BlockState old, BlockState updated, CallbackInfo ci) {
        // Placeholder — XRay toggle calls scheduleTerrainUpdate() directly from XRayFeature
    }
}
