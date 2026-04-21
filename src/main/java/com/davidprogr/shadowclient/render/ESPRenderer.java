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
        if (matrices == null) return;

        Camera camera = context.camera();
        Vec3d camPos  = camera.getPos();

        VertexConsumerProvider.Immediate provider =
                mc.getBufferBuilders().getEntityVertexConsumers();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;

            boolean isPlayer = entity instanceof PlayerEntity;
            float r = isPlayer ? 0.2f : 1.0f;
            float g = isPlayer ? 0.6f : 0.4f;
            float b = isPlayer ? 1.0f : 0.2f;

            if (esp)     drawESPBox(matrices, provider, entity, camPos, r, g, b, 0.8f);
            if (tracers) drawTracer(matrices, provider, entity, camPos, mc, r, g, b, 0.9f);
        }

        provider.draw();
    }

    // ---- Bounding box (wireframe) ----
    private static void drawESPBox(MatrixStack matrices, VertexConsumerProvider provider,
                                    Entity entity, Vec3d camPos,
                                    float r, float g, float b, float a) {
        Box box = entity.getBoundingBox();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer lines = provider.getBuffer(RenderLayer.getLines());
        Matrix4f mat = matrices.peek().getPositionMatrix();

        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;
        drawBox(lines, mat, x1, y1, z1, x2, y2, z2, r, g, b, a);

        matrices.pop();
    }

    // ---- Tracer line ----
    private static void drawTracer(MatrixStack matrices, VertexConsumerProvider provider,
                                    Entity entity, Vec3d camPos, MinecraftClient mc,
                                    float r, float g, float b, float a) {
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        Vec3d center = entity.getBoundingBox().getCenter();

        VertexConsumer lines = provider.getBuffer(RenderLayer.getLines());
        Matrix4f mat = matrices.peek().getPositionMatrix();

        // line from camera pos to entity center
        addLine(lines, mat,
                (float) camPos.x, (float) camPos.y, (float) camPos.z,
                (float) center.x, (float) center.y, (float) center.z,
                r, g, b, a);

        matrices.pop();
    }

    // ---- Box edges ----
    private static void drawBox(VertexConsumer lines, Matrix4f mat,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2,
                                  float r, float g, float b, float a) {
        // Bottom
        addLine(lines, mat, x1,y1,z1, x2,y1,z1, r,g,b,a);
        addLine(lines, mat, x2,y1,z1, x2,y1,z2, r,g,b,a);
        addLine(lines, mat, x2,y1,z2, x1,y1,z2, r,g,b,a);
        addLine(lines, mat, x1,y1,z2, x1,y1,z1, r,g,b,a);
        // Top
        addLine(lines, mat, x1,y2,z1, x2,y2,z1, r,g,b,a);
        addLine(lines, mat, x2,y2,z1, x2,y2,z2, r,g,b,a);
        addLine(lines, mat, x2,y2,z2, x1,y2,z2, r,g,b,a);
        addLine(lines, mat, x1,y2,z2, x1,y2,z1, r,g,b,a);
        // Verticals
        addLine(lines, mat, x1,y1,z1, x1,y2,z1, r,g,b,a);
        addLine(lines, mat, x2,y1,z1, x2,y2,z1, r,g,b,a);
        addLine(lines, mat, x2,y1,z2, x2,y2,z2, r,g,b,a);
        addLine(lines, mat, x1,y1,z2, x1,y2,z2, r,g,b,a);
    }

    private static void addLine(VertexConsumer lines, Matrix4f mat,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2,
                                  float r, float g, float b, float a) {
        float dx = x2-x1, dy = y2-y1, dz = z2-z1;
        float len = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (len == 0f) len = 1f;
        lines.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(dx/len, dy/len, dz/len);
        lines.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(dx/len, dy/len, dz/len);
    }
}
