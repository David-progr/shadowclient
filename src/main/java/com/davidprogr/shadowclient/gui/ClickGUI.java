package com.davidprogr.shadowclient.gui;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * ClickGUI v2 — Category tabs on the left, module list in the centre.
 * Right-click on any module → opens a settings panel on the right.
 */
public class ClickGUI extends Screen {

    // ── Colours (shadow purple theme — overridden to cyan for Light build via static field)
    public static int ACCENT = 0xFFAA00FF;
    private static final int BG = 0xE0111111;
    private static final int PANEL_BG = 0xE0181818;
    private static final int HOVER = 0xFF222233;
    private static final int TEXT = 0xFFEEEEEE;
    private static final int DISABLED = 0xFF555555;
    private static final int ENABLED = 0xFF00FF88;

    // ── Layout constants
    private static final int TAB_W = 100;
    private static final int TAB_H = 22;
    private static final int MOD_H = 22;
    private static final int PANEL_W = 200;
    private static final int SEARCH_H = 20;

    // ── State
    private Feature.Category selectedCategory = Feature.Category.COMBAT;
    private Feature settingsTarget = null;       // null = no settings panel open
    private String searchQuery = "";
    private boolean searchFocused = false;

    // Scroll state
    private int moduleScroll = 0;
    private int settingScroll = 0;

    // Dragging settings panel
    private int settingsPanelX = 0, settingsPanelY = 0;
    private boolean dragging = false;
    private int dragOffX, dragOffY;

    // Keybind capture
    private KeybindSetting capturingKeybind = null;

    // Slider dragging
    private SliderSetting draggingSlider = null;
    private int sliderBarStartX;

    public ClickGUI() {
        super(Text.literal("ClickGUI"));
    }

    // ─────────────────────────────────────────────────────────────
    //  Render
    // ─────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // DO NOT call super.render() — it applies Minecraft's blur/dirt background.
        // We draw our own semi-transparent dim instead.
        ctx.fill(0, 0, width, height, 0xAA000000);

        int guiW = TAB_W + 200 + 4;
        int guiH = Math.min(height - 40, 400);
        int guiX = (width - guiW) / 2;
        int guiY = (height - guiH) / 2;

        // Outer panel bg
        ctx.fill(guiX, guiY, guiX + guiW, guiY + guiH, BG);
        drawBorder(ctx, guiX, guiY, guiX + guiW, guiY + guiH, ACCENT, 1);

        // Title bar
        ctx.fill(guiX, guiY, guiX + guiW, guiY + 18, colorWithAlpha(ACCENT, 180));
        ctx.drawCenteredTextWithShadow(textRenderer, "§lClickGUI", guiX + guiW / 2, guiY + 5, TEXT);

        int contentY = guiY + 18;

        // Search bar
        ctx.fill(guiX, contentY, guiX + guiW, contentY + SEARCH_H, PANEL_BG);
        String hint = searchFocused ? "" : "§7Search...";
        String display = searchFocused ? searchQuery + "§7|" : (searchQuery.isEmpty() ? hint : searchQuery);
        ctx.drawTextWithShadow(textRenderer, display, guiX + 6, contentY + 6, TEXT);
        contentY += SEARCH_H;

        // Category tabs (left column)
        drawCategoryTabs(ctx, guiX, contentY, guiH - 18 - SEARCH_H, mx, my);

        // Module list (right of tabs)
        int listX = guiX + TAB_W;
        int listW = 200;
        int listH = guiH - 18 - SEARCH_H;
        drawModuleList(ctx, listX, contentY, listW, listH, mx, my);

        // Settings panel (right side, floating)
        if (settingsTarget != null) {
            settingsPanelX = Math.min(settingsPanelX, width - PANEL_W - 4);
            settingsPanelY = Math.min(settingsPanelY, height - 20);
            drawSettingsPanel(ctx, settingsPanelX, settingsPanelY, mx, my);
        }
        // NOTE: super.render() intentionally omitted — it triggers Minecraft's blur shader.
    }

    private void drawCategoryTabs(DrawContext ctx, int x, int y, int h, int mx, int my) {
        Feature.Category[] cats = Feature.Category.values();
        ctx.fill(x, y, x + TAB_W, y + h, PANEL_BG);
        for (int i = 0; i < cats.length; i++) {
            Feature.Category cat = cats[i];
            int ty = y + i * TAB_H;
            boolean hover = mx >= x && mx < x + TAB_W && my >= ty && my < ty + TAB_H;
            boolean selected = cat == selectedCategory;
            int bg = selected ? colorWithAlpha(ACCENT, 120) : (hover ? HOVER : 0);
            if (bg != 0) ctx.fill(x, ty, x + TAB_W, ty + TAB_H, bg);
            if (selected) ctx.fill(x, ty, x + 2, ty + TAB_H, ACCENT);
            ctx.drawTextWithShadow(textRenderer, cat.name(), x + 6, ty + 7, selected ? ACCENT : TEXT);
        }
    }

    private void drawModuleList(DrawContext ctx, int x, int y, int w, int h, int mx, int my) {
        ctx.fill(x, y, x + w, y + h, BG);
        List<Feature> features = getFilteredFeatures();

        // Clamp scroll
        int maxScroll = Math.max(0, features.size() * MOD_H - h);
        moduleScroll = Math.max(0, Math.min(moduleScroll, maxScroll));

        // Scissor
        ctx.enableScissor(x, y, x + w, y + h);
        int fy = y - moduleScroll;
        for (Feature feature : features) {
            if (fy + MOD_H > y && fy < y + h) {
                boolean hover = mx >= x && mx < x + w && my >= fy && my < fy + MOD_H;
                boolean enabled = feature.isEnabled();
                boolean selected = feature == settingsTarget;

                int rowBg = selected ? colorWithAlpha(ACCENT, 60) : (hover ? HOVER : 0);
                if (rowBg != 0) ctx.fill(x, fy, x + w, fy + MOD_H, rowBg);

                // Enabled indicator dot
                int dotColor = enabled ? ENABLED : DISABLED;
                ctx.fill(x + 4, fy + 8, x + 10, fy + 14, dotColor);

                ctx.drawTextWithShadow(textRenderer, feature.getName(), x + 14, fy + 7, enabled ? TEXT : DISABLED);

                // Keybind hint
                List<Setting<?>> settings = feature.getSettings();
                for (Setting<?> s : settings) {
                    if (s instanceof KeybindSetting kb) {
                        int key = kb.getValue();
                        if (key != -1) {
                            String keyName = "[" + net.minecraft.client.util.InputUtil.fromKeyCode(key, 0).getLocalizedText().getString() + "]";
                            ctx.drawTextWithShadow(textRenderer, "§8" + keyName, x + w - textRenderer.getWidth(keyName) - 4, fy + 7, DISABLED);
                        }
                        break;
                    }
                }
            }
            fy += MOD_H;
        }
        ctx.disableScissor();

        // Scrollbar
        if (features.size() * MOD_H > h) {
            float ratio = (float) h / (features.size() * MOD_H);
            int sbH = Math.max(20, (int)(h * ratio));
            int sbY = y + (int)((moduleScroll / (float)maxScroll) * (h - sbH));
            ctx.fill(x + w - 3, sbY, x + w, sbY + sbH, colorWithAlpha(ACCENT, 150));
        }
    }

    private void drawSettingsPanel(DrawContext ctx, int px, int py, int mx, int my) {
        Feature f = settingsTarget;
        List<Setting<?>> settings = f.getSettings();
        int panelH = Math.min(height - py - 4, 30 + settings.size() * 28 + 30);

        // Panel bg
        ctx.fill(px, py, px + PANEL_W, py + panelH, PANEL_BG);
        drawBorder(ctx, px, py, px + PANEL_W, py + panelH, ACCENT, 1);

        // Title bar (draggable)
        ctx.fill(px, py, px + PANEL_W, py + 20, colorWithAlpha(ACCENT, 180));
        ctx.drawCenteredTextWithShadow(textRenderer, "§l" + f.getName(), px + PANEL_W / 2, py + 6, TEXT);
        // Close button [X]
        ctx.drawTextWithShadow(textRenderer, "§c✕", px + PANEL_W - 14, py + 6, TEXT);

        int sy = py + 24;

        // Keybind row
        ctx.drawTextWithShadow(textRenderer, "§7Keybind", px + 6, sy + 3, TEXT);
        // Find the first KeybindSetting
        KeybindSetting kb = null;
        for (Setting<?> s : settings) if (s instanceof KeybindSetting k) { kb = k; break; }
        if (kb != null) {
            boolean capturing = (capturingKeybind == kb);
            String label = capturing ? "§e[ Press key... ]" :
                (kb.getValue() == -1 ? "§8NONE" :
                    "§a" + net.minecraft.client.util.InputUtil.fromKeyCode(kb.getValue(), 0).getLocalizedText().getString().toUpperCase());
            int btnW = 80;
            int btnX = px + PANEL_W - btnW - 4;
            ctx.fill(btnX, sy, btnX + btnW, sy + 14, capturing ? colorWithAlpha(ACCENT, 120) : 0xFF222222);
            drawBorder(ctx, btnX, sy, btnX + btnW, sy + 14, capturing ? ACCENT : 0xFF444444, 1);
            ctx.drawCenteredTextWithShadow(textRenderer, label, btnX + btnW / 2, sy + 3, TEXT);
        }
        sy += 20;

        // Settings
        ctx.enableScissor(px, py + 24, px + PANEL_W, py + panelH - 4);
        int drawY = py + 24 + 20 - settingScroll;
        for (Setting<?> setting : settings) {
            if (setting instanceof KeybindSetting) continue; // already drawn above
            if (drawY + 24 > py + panelH - 4 || drawY < py + 24) { drawY += 26; continue; }
            drawSetting(ctx, setting, px + 4, drawY, PANEL_W - 8, mx, my);
            drawY += 26;
        }
        ctx.disableScissor();
    }

    @SuppressWarnings("unchecked")
    private void drawSetting(DrawContext ctx, Setting<?> setting, int x, int y, int w, int mx, int my) {
        ctx.drawTextWithShadow(textRenderer, "§7" + setting.getName(), x, y + 2, TEXT);

        if (setting instanceof BooleanSetting bs) {
            boolean val = bs.getValue();
            int toggleX = x + w - 28;
            ctx.fill(toggleX, y, toggleX + 26, y + 12, val ? colorWithAlpha(ENABLED, 200) : 0xFF333333);
            drawBorder(ctx, toggleX, y, toggleX + 26, y + 12, val ? ENABLED : 0xFF555555, 1);
            int knobX = val ? (toggleX + 14) : (toggleX + 2);
            ctx.fill(knobX, y + 2, knobX + 10, y + 10, 0xFFEEEEEE);

        } else if (setting instanceof SliderSetting ss) {
            double min = ss.getMin(), max = ss.getMax(), val = ss.getValue();
            int barX = x + w / 2;
            int barW = w / 2;
            int barY = y + 5;
            float pct = (float)((val - min) / (max - min));
            ctx.fill(barX, barY, barX + barW, barY + 6, 0xFF333333);
            ctx.fill(barX, barY, barX + (int)(barW * pct), barY + 6, ACCENT);
            // Knob
            int knobX = barX + (int)(barW * pct) - 3;
            ctx.fill(knobX, barY - 2, knobX + 6, barY + 8, 0xFFEEEEEE);
            String valStr = ss.isInt() ? String.valueOf((int)val) : String.format("%.1f", val);
            ctx.drawTextWithShadow(textRenderer, "§a" + valStr, x + w - textRenderer.getWidth(valStr) - barW - 4, y + 2, TEXT);

        } else if (setting instanceof ModeSetting ms) {
            String val = ms.getValue();
            int btnW = Math.min(80, textRenderer.getWidth(val) + 14);
            int btnX = x + w - btnW;
            ctx.fill(btnX, y, btnX + btnW, y + 14, 0xFF222222);
            drawBorder(ctx, btnX, y, btnX + btnW, y + 14, colorWithAlpha(ACCENT, 150), 1);
            ctx.drawCenteredTextWithShadow(textRenderer, "§b" + val, btnX + btnW / 2, y + 3, TEXT);

        } else if (setting instanceof ColorSetting cs) {
            int color = cs.getValue();
            int swatchSize = 14;
            int swatchX = x + w - swatchSize - 2;
            ctx.fill(swatchX, y, swatchX + swatchSize, y + swatchSize, color | 0xFF000000);
            drawBorder(ctx, swatchX, y, swatchX + swatchSize, y + swatchSize, TEXT, 1);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Mouse events
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int imx = (int) mx, imy = (int) my;

        // Settings panel interactions
        if (settingsTarget != null) {
            int px = settingsPanelX, py = settingsPanelY;
            // Close button
            if (imx >= px + PANEL_W - 16 && imx <= px + PANEL_W - 2 && imy >= py + 2 && imy <= py + 18) {
                settingsTarget = null;
                return true;
            }
            // Drag title bar
            if (imy >= py && imy <= py + 20 && imx >= px && imx <= px + PANEL_W) {
                dragging = true;
                dragOffX = imx - px;
                dragOffY = imy - py;
                return true;
            }
            // Interact with settings
            if (imx >= px && imx <= px + PANEL_W) {
                handleSettingClick(imx, imy, button);
                return true;
            }
        }

        // Category tabs
        int guiW = TAB_W + 200 + 4;
        int guiH = Math.min(height - 40, 400);
        int guiX = (width - guiW) / 2;
        int guiY = (height - guiH) / 2 + 18 + SEARCH_H;

        Feature.Category[] cats = Feature.Category.values();
        for (int i = 0; i < cats.length; i++) {
            int ty = guiY + i * TAB_H;
            if (imx >= guiX && imx < guiX + TAB_W && imy >= ty && imy < ty + TAB_H) {
                selectedCategory = cats[i];
                moduleScroll = 0;
                return true;
            }
        }

        // Module list
        int listX = guiX + TAB_W;
        int listW = 200;
        int listH = guiH - 18 - SEARCH_H;
        if (imx >= listX && imx < listX + listW) {
            List<Feature> features = getFilteredFeatures();
            int fy = guiY - moduleScroll;
            for (Feature f : features) {
                if (imy >= fy && imy < fy + MOD_H) {
                    if (button == 0) { // left click = toggle
                        f.toggle();
                    } else if (button == 1) { // right click = open settings
                        settingsTarget = f;
                        settingScroll = 0;
                        // Position panel to the right of the module list
                        settingsPanelX = listX + listW + 4;
                        settingsPanelY = guiY;
                        // Clamp
                        if (settingsPanelX + PANEL_W > width) settingsPanelX = listX - PANEL_W - 4;
                        if (settingsPanelX < 0) settingsPanelX = 4;
                    }
                    return true;
                }
                fy += MOD_H;
            }
        }

        // Search bar click
        int sgX = (width - guiW) / 2;
        int sgY = (height - Math.min(height - 40, 400)) / 2 + 18;
        if (imx >= sgX && imx < sgX + guiW && imy >= sgY && imy < sgY + SEARCH_H) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        return super.mouseClicked(mx, my, button);
    }

    private void handleSettingClick(int mx, int my, int button) {
        if (settingsTarget == null) return;
        Feature f = settingsTarget;
        List<Setting<?>> settings = f.getSettings();
        int px = settingsPanelX, py = settingsPanelY;

        // Keybind button (first KeybindSetting — drawn at py+24)
        int sy = py + 44;
        for (Setting<?> s : settings) {
            if (s instanceof KeybindSetting kb) {
                int btnW = 80;
                int btnX = px + PANEL_W - btnW - 4;
                if (mx >= btnX && mx <= btnX + btnW && my >= py + 24 && my <= py + 38) {
                    if (button == 0) capturingKeybind = kb;
                }
                break;
            }
        }

        int drawY = py + 44 - settingScroll;
        for (Setting<?> setting : settings) {
            if (setting instanceof KeybindSetting) continue;
            int sw = PANEL_W - 8;
            int sx = px + 4;

            if (setting instanceof BooleanSetting bs && my >= drawY && my <= drawY + 14) {
                int toggleX = sx + sw - 28;
                if (mx >= toggleX && mx <= toggleX + 26) {
                    bs.setValue(!bs.getValue());
                }
            } else if (setting instanceof ModeSetting ms && my >= drawY && my <= drawY + 14) {
                int btnW = Math.min(80, textRenderer.getWidth(ms.getValue()) + 14);
                int btnX = sx + sw - btnW;
                if (mx >= btnX && mx <= btnX + btnW) {
                    if (button == 0) ms.next();
                    else if (button == 1) ms.previous();
                }
            } else if (setting instanceof SliderSetting ss && my >= drawY && my <= drawY + 14) {
                int barX = sx + sw / 2;
                int barW = sw / 2;
                if (mx >= barX && mx <= barX + barW) {
                    draggingSlider = ss;
                    sliderBarStartX = barX;
                    updateSlider(mx);
                }
            }
            drawY += 26;
        }
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        int imx = (int) mx, imy = (int) my;
        if (dragging) {
            settingsPanelX = imx - dragOffX;
            settingsPanelY = imy - dragOffY;
            return true;
        }
        if (draggingSlider != null) {
            updateSlider(imx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    private void updateSlider(int mx) {
        if (draggingSlider == null) return;
        int barX = sliderBarStartX;
        int barW = (PANEL_W - 8) / 2;
        float pct = Math.max(0f, Math.min(1f, (mx - barX) / (float) barW));
        double val = draggingSlider.getMin() + pct * (draggingSlider.getMax() - draggingSlider.getMin());
        if (draggingSlider.isInt()) val = Math.round(val);
        draggingSlider.setValue(val);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        draggingSlider = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        int imx = (int) mx, imy = (int) my;
        int guiW = TAB_W + 200 + 4;
        int guiH = Math.min(height - 40, 400);
        int guiX = (width - guiW) / 2;
        int guiY = (height - guiH) / 2;
        int listX = guiX + TAB_W;

        if (settingsTarget != null && imx >= settingsPanelX && imx <= settingsPanelX + PANEL_W) {
            settingScroll = Math.max(0, settingScroll - (int)(vAmount * 10));
        } else if (imx >= listX && imx <= listX + 200) {
            List<Feature> features = getFilteredFeatures();
            int maxScroll = Math.max(0, features.size() * MOD_H - (guiH - 18 - SEARCH_H));
            moduleScroll = Math.max(0, Math.min(moduleScroll - (int)(vAmount * 10), maxScroll));
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    //  Keyboard events
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (capturingKeybind != null) {
            if (keyCode == 256) { // Escape = unbind
                capturingKeybind.setValue(-1);
            } else {
                capturingKeybind.setValue(keyCode);
            }
            capturingKeybind = null;
            return true;
        }

        if (keyCode == 256) { // Escape
            if (settingsTarget != null) { settingsTarget = null; return true; }
            this.close();
            return true;
        }

        if (searchFocused) {
            if (keyCode == 259 && !searchQuery.isEmpty()) { // Backspace
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                moduleScroll = 0;
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && chr >= 32) {
            searchQuery += chr;
            moduleScroll = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    private List<Feature> getFilteredFeatures() {
        List<Feature> list = new ArrayList<>();
        String q = searchQuery.toLowerCase();
        for (Feature f : FeatureRegistry.get().all()) {
            if (!q.isEmpty() && !f.getName().toLowerCase().contains(q)) continue;
            if (q.isEmpty() && f.getCategory() != selectedCategory) continue;
            list.add(f);
        }
        return list;
    }

    private void drawBorder(DrawContext ctx, int x1, int y1, int x2, int y2, int color, int thickness) {
        ctx.fill(x1, y1, x2, y1 + thickness, color);
        ctx.fill(x1, y2 - thickness, x2, y2, color);
        ctx.fill(x1, y1, x1 + thickness, y2, color);
        ctx.fill(x2 - thickness, y1, x2, y2, color);
    }

    private int colorWithAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; } // we handle Esc manually
}
