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
        TextRenderer font = mc.textRenderer;
        PlayerEntity player = mc.player;
        if (player == null) return;

        // --- Active modules list (top-right) ---
        List<Feature> active = FeatureManager.FEATURES.stream()
                .filter(Feature::isEnabled)
                .toList();

        int x = mc.getWindow().getScaledWidth() - 2;
        int y = 2;
        for (Feature f : active) {
            String name = f.getName();
            int textW = font.getWidth(name);
            // subtle dark bg
            ctx.fill(x - textW - 3, y - 1, x + 1, y + font.fontHeight + 1, 0x88000000);
            ctx.drawTextWithShadow(font, "§d" + name, x - textW, y, 0xFFFFFF);
            y += font.fontHeight + 2;
        }

        // --- Coordinates HUD (bottom-left) ---
        if (FeatureManager.COORDS.isEnabled()) {
            int bx = 3;
            int by = mc.getWindow().getScaledHeight() - 30;
            double px = player.getX();
            double py = player.getY();
            double pz = player.getZ();

            String coords = String.format("§7X: §f%.1f  §7Y: §f%.1f  §7Z: §f%.1f", px, py, pz);
            ctx.fill(bx - 2, by - 2, bx + font.getWidth(coords) + 2, by + font.fontHeight + 2, 0x88000000);
            ctx.drawTextWithShadow(font, coords, bx, by, 0xFFFFFF);
        }

        // --- Watermark (top-left) ---
        ctx.drawTextWithShadow(font, "§dShadow §7Client", 3, 3, 0xFFFFFF);
    }
}
