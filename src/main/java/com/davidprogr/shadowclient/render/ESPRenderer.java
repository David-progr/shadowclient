package com.davidprogr.shadowclient.render;

import com.davidprogr.shadowclient.feature.FeatureManager;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;

public class ESPRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ESPRenderer::onRender);
    }

    private static void onRender(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        boolean esp     = FeatureManager.ESP.isEnabled();
        boolean tracers = FeatureManager.TRACERS.isEnabled();
        if (!esp && !tracers) return;

        MatrixStack matrices = context.matrixStack();
        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();

        VertexConsumerProvider.Immediate provider = mc.getBufferBuilders().getEntityVertexConsumers();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity instanceof PlayerEntity) {
                if (esp) {
                    drawESPBox(matrices, provider, entity, camPos, 0.2f, 0.6f, 1.0f, 0.4f);
                }
                if (tracers) {
                    drawTracer(matrices, provider, entity, camPos, 0.2f, 0.6f, 1.0f, 0.8f);
                }
            } else {
                // Non-player entities (mobs, animals)
                if (esp) {
                    drawESPBox(matrices, provider, entity, camPos, 1.0f, 0.4f, 0.2f, 0.3f);
                }
                if (tracers) {
                    drawTracer(matrices, provider, entity, camPos, 1.0f, 0.4f, 0.2f, 0.6f);
                }
            }
        }

        provider.draw();
    }

    private static void drawESPBox(MatrixStack matrices, VertexConsumerProvider provider,
                                    Entity entity, Vec3d camPos,
                                    float r, float g, float b, float a) {
        Box box = entity.getBoundingBox();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer lines = provider.getBuffer(RenderLayer.LINES);
        Matrix4f mat = matrices.peek().getPositionMatrix();

        drawBox(lines, mat,
                (float)(box.minX), (float)(box.minY), (float)(box.minZ),
                (float)(box.maxX), (float)(box.maxY), (float)(box.maxZ),
                r, g, b, a);

        matrices.pop();
    }

    private static void drawTracer(MatrixStack matrices, VertexConsumerProvider provider,
                                    Entity entity, Vec3d camPos,
                                    float r, float g, float b, float a) {
        MinecraftClient mc = MinecraftClient.getInstance();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        Vec3d center = entity.getBoundingBox().getCenter();
        // Start from camera look direction
        Vec3d lookVec = camPos.add(mc.player.getRotationVec(1.0f).multiply(1.0));

        VertexConsumer lines = provider.getBuffer(RenderLayer.LINES);
        Matrix4f mat = matrices.peek().getPositionMatrix();

        // Draw line from camera pos to entity center
        lines.vertex(mat, (float)(camPos.x), (float)(camPos.y), (float)(camPos.z))
                .color(r, g, b, a)
                .normal(0, 1, 0);
        lines.vertex(mat, (float)(center.x), (float)(center.y), (float)(center.z))
                .color(r, g, b, a)
                .normal(0, 1, 0);

        matrices.pop();
    }

    private static void drawBox(VertexConsumer lines, Matrix4f mat,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2,
                                  float r, float g, float b, float a) {
        // Bottom face
        drawLine(lines, mat, x1, y1, z1, x2, y1, z1, r, g, b, a);
        drawLine(lines, mat, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(lines, mat, x2, y1, z2, x1, y1, z2, r, g, b, a);
        drawLine(lines, mat, x1, y1, z2, x1, y1, z1, r, g, b, a);
        // Top face
        drawLine(lines, mat, x1, y2, z1, x2, y2, z1, r, g, b, a);
        drawLine(lines, mat, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(lines, mat, x2, y2, z2, x1, y2, z2, r, g, b, a);
        drawLine(lines, mat, x1, y2, z2, x1, y2, z1, r, g, b, a);
        // Vertical edges
        drawLine(lines, mat, x1, y1, z1, x1, y2, z1, r, g, b, a);
        drawLine(lines, mat, x2, y1, z1, x2, y2, z1, r, g, b, a);
        drawLine(lines, mat, x2, y1, z2, x2, y2, z2, r, g, b, a);
        drawLine(lines, mat, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private static void drawLine(VertexConsumer lines, Matrix4f mat,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float r, float g, float b, float a) {
        float nx = x2 - x1, ny = y2 - y1, nz = z2 - z1;
        float len = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
        if (len == 0) len = 1;
        lines.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(nx/len, ny/len, nz/len);
        lines.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(nx/len, ny/len, nz/len);
    }
}
