package com.davidprogr.shadowclient.render;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.FeatureManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class HUDRenderer {

    public static void render(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.currentScreen != null) return;

        TextRenderer font  = mc.textRenderer;
        PlayerEntity player = mc.player;

        // --- Watermark top-left ---
        ctx.drawTextWithShadow(font, "§dShadow §7Client §81.21.4", 3, 3, 0xFFFFFF);

        // --- Active modules list top-right ---
        List<Feature> active = FeatureManager.FEATURES.stream()
                .filter(Feature::isEnabled)
                .toList();

        int sw = mc.getWindow().getScaledWidth();
        int y  = 3;
        for (Feature f : active) {
            int tw = font.getWidth(f.getName());
            ctx.fill(sw - tw - 6, y - 1, sw - 1, y + font.fontHeight + 1, 0x99000000);
            ctx.drawTextWithShadow(font, "§d" + f.getName(), sw - tw - 4, y, 0xFFFFFF);
            y += font.fontHeight + 3;
        }

        // --- Coordinates bottom-left ---
        if (FeatureManager.COORDS.isEnabled()) {
            int sh = mc.getWindow().getScaledHeight();
            String line = String.format("§7X:§f%.1f §7Y:§f%.1f §7Z:§f%.1f",
                    player.getX(), player.getY(), player.getZ());
            int tw = font.getWidth(line);
            int bx = 3, by = sh - font.fontHeight - 4;
            ctx.fill(bx - 2, by - 2, bx + tw + 2, by + font.fontHeight + 2, 0x99000000);
            ctx.drawTextWithShadow(font, line, bx, by, 0xFFFFFF);
        }
    }
}
