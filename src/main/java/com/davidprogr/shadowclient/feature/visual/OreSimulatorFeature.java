package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * OreSimulator — Shadow only.
 *
 * Scans loaded chunks within a configurable radius and renders coloured
 * bounding boxes around every visible ore block using the lines render layer.
 *
 * Works purely client-side: reads blocks from the already-loaded
 * ClientChunkManager — no extra packet traffic.  Safe to use alongside XRay.
 *
 * Ore colours (ARGB):
 *   Diamond        0xFF00CFFF
 *   Emerald        0xFF00FF44
 *   Ancient Debris 0xFFAA3300
 *   Gold           0xFFFFD700
 *   Iron           0xFFD4A76A
 *   Copper         0xFFE07840
 *   Lapis          0xFF2255FF
 *   Redstone       0xFFFF2222
 *   Coal           0xFF888888
 *   Quartz         0xFFEEDDCC
 *   Nether Gold    0xFFFFAA00
 */
public class OreSimulatorFeature extends Feature {

    public final SliderSetting  radius      = addSetting(new SliderSetting("Radius","Chunk scan radius (chunks)",3,1,8,1));
    public final SliderSetting  lineWidth   = addSetting(new SliderSetting("Line Width","Box line width",1.5,0.5,4.0,0.5));
    public final BooleanSetting diamond     = addSetting(new BooleanSetting("Diamond","Show diamond ore",true));
    public final BooleanSetting emerald     = addSetting(new BooleanSetting("Emerald","Show emerald ore",true));
    public final BooleanSetting ancient     = addSetting(new BooleanSetting("Ancient Debris","Show ancient debris",true));
    public final BooleanSetting gold        = addSetting(new BooleanSetting("Gold","Show gold ore",true));
    public final BooleanSetting iron        = addSetting(new BooleanSetting("Iron","Show iron ore",false));
    public final BooleanSetting copper      = addSetting(new BooleanSetting("Copper","Show copper ore",false));
    public final BooleanSetting lapis       = addSetting(new BooleanSetting("Lapis","Show lapis ore",true));
    public final BooleanSetting redstone    = addSetting(new BooleanSetting("Redstone","Show redstone ore",false));
    public final BooleanSetting coal        = addSetting(new BooleanSetting("Coal","Show coal ore",false));
    public final BooleanSetting quartz      = addSetting(new BooleanSetting("Quartz","Show quartz ore",true));
    public final BooleanSetting netherGold  = addSetting(new BooleanSetting("Nether Gold","Show nether gold",true));

    // Cached scan results:  pos → ARGB colour
    private final Map<BlockPos, Integer> found = new LinkedHashMap<>();
    private int scanTimer = 0;
    private static final int SCAN_INTERVAL = 40; // ticks between full re-scans

    public OreSimulatorFeature() {
        super("OreSimulator","Highlights ore blocks in loaded chunks", Category.VISUAL, false);
    }

    @Override public void onEnable()  { found.clear(); scanTimer = 0; }
    @Override public void onDisable() { found.clear(); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (++scanTimer < SCAN_INTERVAL) return;
        scanTimer = 0;
        scanOres(mc);
    }

    /** Called by WorldRendererMixin / our ESP pass each frame. */
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera) {
        if (found.isEmpty()) return;
        Vec3d cam = camera.getPos();
        VertexConsumer vc = immediate.getBuffer(RenderLayer.LINES);
        org.joml.Matrix4f mat = matrices.peek().getPositionMatrix();

        for (Map.Entry<BlockPos, Integer> entry : new ArrayList<>(found.entrySet())) {
            BlockPos pos = entry.getKey();
            int argb    = entry.getValue();
            float r = ((argb >> 16) & 0xFF) / 255f;
            float g = ((argb >>  8) & 0xFF) / 255f;
            float b = ( argb        & 0xFF) / 255f;
            float a = Math.max(0.3f, ((argb >> 24) & 0xFF) / 255f);

            float x1 = (float)(pos.getX() - cam.x);
            float y1 = (float)(pos.getY() - cam.y);
            float z1 = (float)(pos.getZ() - cam.z);
            float x2 = x1 + 1f, y2 = y1 + 1f, z2 = z1 + 1f;
            drawBox(vc, mat, x1, y1, z1, x2, y2, z2, r, g, b, a);
        }
    }

    // ─────────────────────────────────────────────────────────────
    private void scanOres(MinecraftClient mc) {
        found.clear();
        int cx = mc.player.getBlockX() >> 4;
        int cz = mc.player.getBlockZ() >> 4;
        int rad = radius.getValue().intValue();

        for (int dx = -rad; dx <= rad; dx++) {
            for (int dz = -rad; dz <= rad; dz++) {
                var chunk = mc.world.getChunk(cx + dx, cz + dz,
                        net.minecraft.world.chunk.ChunkStatus.FULL, false);
                if (chunk == null) continue;
                // Scan every block in chunk (Y from world bottom to top)
                int worldMinY = mc.world.getBottomY();
                int worldMaxY = mc.world.getTopYInclusive() + 1;
                int baseX = (cx + dx) << 4;
                int baseZ = (cz + dz) << 4;
                for (int y = worldMinY; y < worldMaxY; y++) {
                    for (int x = baseX; x < baseX + 16; x++) {
                        for (int z = baseZ; z < baseZ + 16; z++) {
                            BlockPos bp = new BlockPos(x, y, z);
                            Block block = mc.world.getBlockState(bp).getBlock();
                            int color = getColor(block);
                            if (color != 0) found.put(bp.toImmutable(), color);
                        }
                    }
                }
            }
        }
    }

    /** Returns ARGB colour for an ore block, or 0 if not tracked. */
    private int getColor(Block b) {
        if (diamond.getValue() && (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE))
            return 0xFF00CFFF;
        if (emerald.getValue() && (b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE))
            return 0xFF00FF44;
        if (ancient.getValue() && b == Blocks.ANCIENT_DEBRIS)
            return 0xFFAA3300;
        if (gold.getValue() && (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE))
            return 0xFFFFD700;
        if (iron.getValue() && (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE))
            return 0xFFD4A76A;
        if (copper.getValue() && (b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE))
            return 0xFFE07840;
        if (lapis.getValue() && (b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE))
            return 0xFF2255FF;
        if (redstone.getValue() && (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE))
            return 0xFFFF2222;
        if (coal.getValue() && (b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE))
            return 0xFF888888;
        if (quartz.getValue() && b == Blocks.NETHER_QUARTZ_ORE)
            return 0xFFEEDDCC;
        if (netherGold.getValue() && b == Blocks.NETHER_GOLD_ORE)
            return 0xFFFFAA00;
        return 0;
    }

    // ── Box drawing helpers ───────────────────────────────────────
    private static void drawBox(VertexConsumer vc, org.joml.Matrix4f mat,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float r, float g, float b, float a) {
        // Bottom face
        line(vc,mat, x1,y1,z1, x2,y1,z1, r,g,b,a); line(vc,mat, x2,y1,z1, x2,y1,z2, r,g,b,a);
        line(vc,mat, x2,y1,z2, x1,y1,z2, r,g,b,a); line(vc,mat, x1,y1,z2, x1,y1,z1, r,g,b,a);
        // Top face
        line(vc,mat, x1,y2,z1, x2,y2,z1, r,g,b,a); line(vc,mat, x2,y2,z1, x2,y2,z2, r,g,b,a);
        line(vc,mat, x2,y2,z2, x1,y2,z2, r,g,b,a); line(vc,mat, x1,y2,z2, x1,y2,z1, r,g,b,a);
        // Verticals
        line(vc,mat, x1,y1,z1, x1,y2,z1, r,g,b,a); line(vc,mat, x2,y1,z1, x2,y2,z1, r,g,b,a);
        line(vc,mat, x2,y1,z2, x2,y2,z2, r,g,b,a); line(vc,mat, x1,y1,z2, x1,y2,z2, r,g,b,a);
    }

    private static void line(VertexConsumer vc, org.joml.Matrix4f mat,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        org.joml.Vector3f n = new org.joml.Vector3f(x2-x1, y2-y1, z2-z1).normalize();
        vc.vertex(mat,x1,y1,z1).color(r,g,b,a).normal(n.x,n.y,n.z);
        vc.vertex(mat,x2,y2,z2).color(r,g,b,a).normal(n.x,n.y,n.z);
    }
}
