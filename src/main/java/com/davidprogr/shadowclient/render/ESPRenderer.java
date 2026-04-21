package com.davidprogr.shadowclient.render;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.visual.ESPFeature;
import com.davidprogr.shadowclient.feature.visual.TracersFeature;
import com.davidprogr.shadowclient.feature.visual.NametagsFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class ESPRenderer {

    public static void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                              Camera camera, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        try {
            FeatureRegistry reg = FeatureRegistry.get();
            ESPFeature esp = reg.esp;
            TracersFeature tracers = reg.tracers;
            NametagsFeature nametags = reg.nametags;

            if ((esp == null || !esp.isEnabled()) &&
                (tracers == null || !tracers.isEnabled()) &&
                (nametags == null || !nametags.isEnabled())) return;

            Vec3d camPos = camera.getPos();
            matrices.push();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);

            // Safe iteration — copy to avoid CME
            java.util.List<Entity> entityList = new java.util.ArrayList<>();
            mc.world.getEntities().forEach(entityList::add);
            for (Entity entity : entityList) {
                if (entity == mc.player) continue;
                if (!(entity instanceof LivingEntity living)) continue;
                if (living.isDead() || living.getHealth() <= 0) continue;

                boolean isPlayer = entity instanceof PlayerEntity;

                double x = entity.getLerpedPos(tickDelta).x;
                double y = entity.getLerpedPos(tickDelta).y;
                double z = entity.getLerpedPos(tickDelta).z;

                // ── ESP boxes ──
                if (esp != null && esp.isEnabled()) {
                    boolean players = esp.players.getValue();
                    boolean mobs    = esp.mobs.getValue();
                    if ((isPlayer && players) || (!isPlayer && mobs)) {
                        int color = isPlayer ? esp.colorP.getValue() : esp.colorM.getValue();
                        String mode = esp.mode.getValue();
                        Box bb = entity.getBoundingBox();
                        Box local = bb.offset(-camPos.x, -camPos.y, -camPos.z);
                        // offset already applied via matrix translate above
                        Box entityBox = new Box(
                            bb.minX, bb.minY, bb.minZ,
                            bb.maxX, bb.maxY, bb.maxZ
                        );
                        drawBox(matrices, immediate, entityBox, color, mode);
                    }
                }

                // ── Tracers ──
                if (tracers != null && tracers.isEnabled()) {
                    boolean tPlayers = tracers.players.getValue();
                    boolean tMobs    = tracers.mobs.getValue();
                    if ((isPlayer && tPlayers) || (!isPlayer && tMobs)) {
                        int color = isPlayer ? 0xFF4FC3F7 : 0xFFF44336;
                        Vec3d screenCenter = new Vec3d(
                            camPos.x, camPos.y + camera.getFocusedEntity().getEyeHeight(camera.getFocusedEntity().getPose()), camPos.z
                        );
                        drawLine(matrices, immediate,
                            screenCenter.x, screenCenter.y, screenCenter.z,
                            x, y + entity.getHeight() / 2f, z, color);
                    }
                }
            }

            matrices.pop();
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────────────────────
    //  Drawing helpers
    // ─────────────────────────────────────────────────────────────

    private static void drawBox(MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                                Box box, int argb, String mode) {
        VertexConsumer vc = immediate.getBuffer(RenderLayer.LINES);
        org.joml.Matrix4f mat = matrices.peek().getPositionMatrix();
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8)  & 0xFF) / 255f;
        float b = (argb         & 0xFF) / 255f;
        float a = ((argb >> 24) & 0xFF) / 255f;
        if (a == 0f) a = 1f;

        float x1 = (float)box.minX, y1 = (float)box.minY, z1 = (float)box.minZ;
        float x2 = (float)box.maxX, y2 = (float)box.maxY, z2 = (float)box.maxZ;

        if ("Corners".equals(mode)) {
            float cx = (x2 - x1) * 0.25f;
            float cy = (y2 - y1) * 0.25f;
            float cz = (z2 - z1) * 0.25f;
            // Bottom corners
            drawCorner(vc, mat, x1, y1, z1,  cx, cy, cz,  r, g, b, a);
            drawCorner(vc, mat, x2, y1, z1, -cx, cy, cz,  r, g, b, a);
            drawCorner(vc, mat, x1, y1, z2,  cx, cy,-cz,  r, g, b, a);
            drawCorner(vc, mat, x2, y1, z2, -cx, cy,-cz,  r, g, b, a);
            // Top corners
            drawCorner(vc, mat, x1, y2, z1,  cx,-cy, cz,  r, g, b, a);
            drawCorner(vc, mat, x2, y2, z1, -cx,-cy, cz,  r, g, b, a);
            drawCorner(vc, mat, x1, y2, z2,  cx,-cy,-cz,  r, g, b, a);
            drawCorner(vc, mat, x2, y2, z2, -cx,-cy,-cz,  r, g, b, a);
        } else {
            // Full box outline — 12 edges
            // Bottom
            line(vc, mat, x1,y1,z1, x2,y1,z1, r,g,b,a); line(vc,mat,x2,y1,z1,x2,y1,z2,r,g,b,a);
            line(vc, mat, x2,y1,z2, x1,y1,z2, r,g,b,a); line(vc,mat,x1,y1,z2,x1,y1,z1,r,g,b,a);
            // Top
            line(vc, mat, x1,y2,z1, x2,y2,z1, r,g,b,a); line(vc,mat,x2,y2,z1,x2,y2,z2,r,g,b,a);
            line(vc, mat, x2,y2,z2, x1,y2,z2, r,g,b,a); line(vc,mat,x1,y2,z2,x1,y2,z1,r,g,b,a);
            // Verticals
            line(vc, mat, x1,y1,z1, x1,y2,z1, r,g,b,a); line(vc,mat,x2,y1,z1,x2,y2,z1,r,g,b,a);
            line(vc, mat, x2,y1,z2, x2,y2,z2, r,g,b,a); line(vc,mat,x1,y1,z2,x1,y2,z2,r,g,b,a);
        }
    }

    private static void drawCorner(VertexConsumer vc, org.joml.Matrix4f mat,
                                   float cx, float cy, float cz,
                                   float dx, float dy, float dz,
                                   float r, float g, float b, float a) {
        line(vc,mat, cx,cy,cz, cx+dx,cy,cz,    r,g,b,a);
        line(vc,mat, cx,cy,cz, cx,cy+dy,cz,    r,g,b,a);
        line(vc,mat, cx,cy,cz, cx,cy,cz+dz,    r,g,b,a);
    }

    private static void drawLine(MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2, int argb) {
        VertexConsumer vc = immediate.getBuffer(RenderLayer.LINES);
        org.joml.Matrix4f mat = matrices.peek().getPositionMatrix();
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8)  & 0xFF) / 255f;
        float b = (argb         & 0xFF) / 255f;
        float a = Math.max(0.01f, ((argb >> 24) & 0xFF) / 255f);
        line(vc, mat, (float)x1,(float)y1,(float)z1, (float)x2,(float)y2,(float)z2, r,g,b,a);
    }

    private static void line(VertexConsumer vc, org.joml.Matrix4f mat,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        org.joml.Vector3f normal = new org.joml.Vector3f(x2-x1, y2-y1, z2-z1).normalize();
        vc.vertex(mat, x1,y1,z1).color(r,g,b,a).normal(normal.x,normal.y,normal.z);
        vc.vertex(mat, x2,y2,z2).color(r,g,b,a).normal(normal.x,normal.y,normal.z);
    }
}
