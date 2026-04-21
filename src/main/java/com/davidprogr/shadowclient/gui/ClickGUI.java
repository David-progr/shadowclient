package com.davidprogr.shadowclient.gui;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.FeatureManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.util.List;

public class ClickGUI extends Screen {

    private static final int BG_COLOR     = 0xCC0A0A0A;
    private static final int HEADER_COLOR = 0xFF1A1A2E;
    private static final int BORDER_COLOR = 0xFF7B2FBE;

    private final Screen parent;

    public ClickGUI(Screen parent) {
        super(Text.literal("Shadow Client"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Feature.Category[] cats = Feature.Category.values();
        int colW    = 155;
        int colGap  = 8;
        int totalW  = cats.length * colW + (cats.length - 1) * colGap;
        int startX  = (this.width - totalW) / 2;
        int startY  = 50;

        for (int c = 0; c < cats.length; c++) {
            List<Feature> features = FeatureManager.getByCategory(cats[c]);
            int colX = startX + c * (colW + colGap);

            for (int i = 0; i < features.size(); i++) {
                final Feature f = features.get(i);
                int btnY = startY + 20 + i * 22;
                ButtonWidget btn = ButtonWidget.builder(
                        label(f),
                        b -> { f.toggle(); b.setMessage(label(f)); }
                ).dimensions(colX, btnY, colW, 18).build();
                this.addDrawableChild(btn);
            }
        }
    }

    private Text label(Feature f) {
        return Text.literal((f.isEnabled() ? "§a● " : "§8○ ") + f.getName());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // Full background
        ctx.fill(0, 0, this.width, this.height, BG_COLOR);

        Feature.Category[] cats = Feature.Category.values();
        int colW   = 155;
        int colGap = 8;
        int totalW = cats.length * colW + (cats.length - 1) * colGap;
        int startX = (this.width - totalW) / 2;
        int startY = 50;

        // Title bar
        ctx.fill(0, 0, this.width, 28, HEADER_COLOR);
        ctx.fill(0, 28, this.width, 29, BORDER_COLOR);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§d✦ Shadow Client §81.21.4"),
                this.width / 2, 9, 0xFFFFFF);

        // Close hint
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("§8[ESC to close]"),
                5, 18, 0xFFFFFF);

        // Category columns
        for (int c = 0; c < cats.length; c++) {
            List<Feature> features = FeatureManager.getByCategory(cats[c]);
            int colX  = startX + c * (colW + colGap);
            int colH  = 20 + features.size() * 22 + 6;

            // Column bg + border
            ctx.fill(colX - 2, startY, colX + colW + 2, startY + colH, 0xBB111122);
            drawBorder(ctx, colX - 2, startY, colX + colW + 2, startY + colH, BORDER_COLOR);

            // Category header
            ctx.fill(colX - 2, startY, colX + colW + 2, startY + 16, HEADER_COLOR);
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("§b" + cats[c].displayName),
                    colX + colW / 2, startY + 4, 0xFFFFFF);
        }

        super.render(ctx, mx, my, delta);
    }

    private void drawBorder(DrawContext ctx, int x1, int y1, int x2, int y2, int col) {
        ctx.fill(x1, y1,     x2, y1 + 1, col);
        ctx.fill(x1, y2 - 1, x2, y2,     col);
        ctx.fill(x1, y1,     x1 + 1, y2, col);
        ctx.fill(x2 - 1, y1, x2, y2,     col);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
