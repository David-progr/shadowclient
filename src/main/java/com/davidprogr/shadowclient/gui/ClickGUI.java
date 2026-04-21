package com.davidprogr.shadowclient.gui;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.FeatureManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ClickGUI extends Screen {

    private static final int BG_COLOR        = 0xCC0A0A0A;
    private static final int HEADER_COLOR    = 0xFF1A1A2E;
    private static final int BORDER_COLOR    = 0xFF7B2FBE;
    private static final int ON_COLOR        = 0xFF00E676;
    private static final int OFF_COLOR       = 0xFF757575;
    private static final int TITLE_COLOR     = 0xFFE040FB;

    private final Screen parent;

    public ClickGUI(Screen parent) {
        super(Text.literal("Shadow Client"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Feature.Category[] cats = Feature.Category.values();
        int colW   = 160;
        int colGap = 10;
        int totalW = cats.length * colW + (cats.length - 1) * colGap;
        int startX = (this.width - totalW) / 2;
        int startY = 50;

        for (int c = 0; c < cats.length; c++) {
            List<Feature> features = FeatureManager.getByCategory(cats[c]);
            int colX = startX + c * (colW + colGap);

            for (int i = 0; i < features.size(); i++) {
                final Feature f = features.get(i);
                int btnY = startY + 22 + i * 24;

                ButtonWidget btn = ButtonWidget.builder(
                        makeLabel(f),
                        button -> {
                            f.toggle();
                            button.setMessage(makeLabel(f));
                        }
                ).dimensions(colX, btnY, colW, 20).build();

                this.addDrawableChild(btn);
            }
        }
    }

    private Text makeLabel(Feature f) {
        String status = f.isEnabled() ? "§a✔ " : "§7✘ ";
        return Text.literal(status + f.getName());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // Dark transparent background
        ctx.fill(0, 0, this.width, this.height, BG_COLOR);

        Feature.Category[] cats = Feature.Category.values();
        int colW   = 160;
        int colGap = 10;
        int totalW = cats.length * colW + (cats.length - 1) * colGap;
        int startX = (this.width - totalW) / 2;
        int startY = 50;

        // Title bar
        ctx.fill(0, 0, this.width, 30, HEADER_COLOR);
        ctx.fill(0, 30, this.width, 32, BORDER_COLOR);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§dShadow Client §8| §71.21.4"),
                this.width / 2, 10, TITLE_COLOR);

        // Category columns
        for (int c = 0; c < cats.length; c++) {
            List<Feature> features = FeatureManager.getByCategory(cats[c]);
            int colX = startX + c * (colW + colGap);
            int colH = 22 + features.size() * 24 + 6;

            // Column background
            ctx.fill(colX - 4, startY, colX + colW + 4, startY + colH, 0xBB111122);
            // Column border
            drawBorder(ctx, colX - 4, startY, colX + colW + 4, startY + colH, BORDER_COLOR);
            // Category header
            ctx.fill(colX - 4, startY, colX + colW + 4, startY + 18, HEADER_COLOR);
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("§b" + cats[c].displayName),
                    colX + colW / 2, startY + 5, 0xFFFFFF);
        }

        // Draw tooltip on hover
        for (Feature f : FeatureManager.FEATURES) {
            // We can't easily get button rects here, so use simple draw
        }

        super.render(ctx, mx, my, delta);
    }

    private void drawBorder(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1,     x2, y1 + 1, color); // top
        ctx.fill(x1, y2 - 1, x2, y2,     color); // bottom
        ctx.fill(x1, y1,     x1 + 1, y2, color); // left
        ctx.fill(x2 - 1, y1, x2, y2,     color); // right
    }

    @Override
    public boolean shouldPause() {
        return false; // game keeps running while GUI is open
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
