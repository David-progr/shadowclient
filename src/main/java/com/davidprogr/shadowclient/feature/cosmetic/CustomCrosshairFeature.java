package com.davidprogr.shadowclient.feature.cosmetic;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Custom Crosshair — Legit-allowed.
 * Replaces the vanilla crosshair with a custom drawn one.
 * Applied by InGameHudMixin cancelling the default render and calling this.
 */
public class CustomCrosshairFeature extends Feature {

    public final ModeSetting    style    = addSetting(new ModeSetting("Style","Crosshair shape",
            new String[]{"Cross","Dot","Circle","Dynamic","Gap"},"Cross"));
    public final SliderSetting  size     = addSetting(new SliderSetting("Size","Crosshair size px",5,2,20,1));
    public final SliderSetting  gap      = addSetting(new SliderSetting("Gap","Center gap px",2,0,10,1));
    public final SliderSetting  thickness= addSetting(new SliderSetting("Thickness","Line thickness px",1,1,4,1));
    public final ColorSetting   color    = addSetting(new ColorSetting("Color","ARGB crosshair color",0xFFFFFFFF));
    public final BooleanSetting outline  = addSetting(new BooleanSetting("Outline","Black outline",true));
    public final BooleanSetting dynamic  = addSetting(new BooleanSetting("Dynamic Expand","Expand while moving/shooting",false));

    public CustomCrosshairFeature() {
        super("CustomCrosshair","Custom crosshair styles", Category.COSMETIC, true);
    }

    public void render(DrawContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        int sw  = mc.getWindow().getScaledWidth();
        int sh  = mc.getWindow().getScaledHeight();
        int cx  = sw / 2;
        int cy  = sh / 2;
        int sz  = (int)(double) size.getValue();
        int gp  = (int)(double) gap.getValue();
        int th  = (int)(double) thickness.getValue();
        int col = color.getValue();

        // Dynamic expansion: spread increases with speed
        if (dynamic.getValue() && mc.player != null) {
            var vel = mc.player.getVelocity();
            double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            gp += (int)(speed * 6);
        }

        try {
            switch (style.getValue()) {
                case "Cross" -> {
                    // Horizontal line
                    if (outline.getValue()) {
                        fillRect(ctx, cx - sz - 1, cy - th,     cx - gp + 1, cy + th + 1, 0xFF000000);
                        fillRect(ctx, cx + gp - 1, cy - th,     cx + sz + 2, cy + th + 1, 0xFF000000);
                        fillRect(ctx, cx - th,     cy - sz - 1, cx + th + 1, cy - gp + 1, 0xFF000000);
                        fillRect(ctx, cx - th,     cy + gp - 1, cx + th + 1, cy + sz + 2, 0xFF000000);
                    }
                    fillRect(ctx, cx - sz, cy - th + 1, cx - gp, cy + th, col);
                    fillRect(ctx, cx + gp, cy - th + 1, cx + sz, cy + th, col);
                    fillRect(ctx, cx - th + 1, cy - sz, cx + th, cy - gp, col);
                    fillRect(ctx, cx - th + 1, cy + gp, cx + th, cy + sz, col);
                }
                case "Dot" -> {
                    if (outline.getValue()) fillRect(ctx, cx - th - 1, cy - th - 1, cx + th + 2, cy + th + 2, 0xFF000000);
                    fillRect(ctx, cx - th, cy - th, cx + th + 1, cy + th + 1, col);
                }
                case "Circle" -> {
                    drawCircle(ctx, cx, cy, sz, th, col);
                }
                case "Gap" -> {
                    // Cross with large gap — popular competitive style
                    int g2 = gp + 3;
                    fillRect(ctx, cx - sz, cy, cx - g2, cy + th, col);
                    fillRect(ctx, cx + g2, cy, cx + sz, cy + th, col);
                    fillRect(ctx, cx, cy - sz, cx + th, cy - g2, col);
                    fillRect(ctx, cx, cy + g2, cx + th, cy + sz, col);
                }
            }
        } catch (Exception ignored) {}
    }

    private void fillRect(DrawContext ctx, int x1, int y1, int x2, int y2, int col) {
        if (x2 <= x1 || y2 <= y1) return;
        ctx.fill(x1, y1, x2, y2, col);
    }

    private void drawCircle(DrawContext ctx, int cx, int cy, int r, int th, int col) {
        // Approximate circle with filled pixels
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                double dist = Math.sqrt((double)(x * x + y * y));
                if (dist >= r - th && dist <= r) {
                    ctx.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, col);
                }
            }
        }
    }
}
