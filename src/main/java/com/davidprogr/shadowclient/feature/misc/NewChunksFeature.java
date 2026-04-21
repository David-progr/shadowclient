package com.davidprogr.shadowclient.feature.misc;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.ColorSetting;
import com.davidprogr.shadowclient.feature.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Highlights newly generated chunks (chunks with no bedrock at y=0 — 1.18+ logic: air at y=-64).
 * These are chunks the server just generated, a strong indicator for players near the world edge.
 */
public class NewChunksFeature extends Feature {
    public final ColorSetting color = new ColorSetting("Color", "Highlight color", 0x4400AAFF);
    public final BooleanSetting notifyChat = new BooleanSetting("Chat Notify", "Print new chunk coords in chat", false);

    // Public so ESPRenderer / WorldRendererMixin can read it
    public final Set<ChunkPos> newChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public final Set<ChunkPos> oldChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public NewChunksFeature() {
        super("NewChunks", "Highlights newly generated chunks", Category.MISC, false);
        addSetting(color);
        addSetting(notifyChat);
    }

    @Override
    public void onDisable() {
        newChunks.clear();
        oldChunks.clear();
    }

    /**
     * Called from WorldRendererMixin when a chunk is loaded.
     * Checks if the chunk's lowest section is empty (never-generated) vs populated.
     */
    public void onChunkLoad(int chunkX, int chunkZ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        if (oldChunks.contains(pos)) return;

        var chunk = mc.world.getChunk(chunkX, chunkZ);
        if (chunk == null) return;

        // Check if the very bottom section is empty (new chunk heuristic)
        boolean isEmpty = true;
        try {
            var section = chunk.getSection(0); // section at world bottom
            isEmpty = (section == null || section.isEmpty());
        } catch (Exception ignored) {}

        if (isEmpty) {
            newChunks.add(pos);
            if (notifyChat.getValue() && mc.player != null) {
                mc.player.sendMessage(
                    net.minecraft.text.Text.literal("§b[NewChunks] §fNew chunk at " + chunkX + ", " + chunkZ),
                    false
                );
            }
        } else {
            oldChunks.add(pos);
            newChunks.remove(pos);
        }
    }
}
