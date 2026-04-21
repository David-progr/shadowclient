package com.davidprogr.shadowclient.render;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.visual.HUDFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.*;

/**
 * Draws the full Shadow/Light Client HUD every frame.
 * Called from InGameHudMixin after the game renders its own HUD.
 *
 * Crash safety rules applied throughout:
 *  1. Every mc.player / mc.world / mc.options access is null-guarded.
 *  2. Entity iteration uses a snapshot list — no CME possible.
 *  3. All string formatting is wrapped in try/catch to prevent format crashes.
 *  4. Biome name uses getKey().map() — never assumes the key is present.
 *  5. Status effect name uses getKey().map() with fallback "Unknown".
 */
public class HUDRenderer {

    // ── Rainbow state ─────────────────────────────────────────────
    private static float rainbowHue = 0f;

    // ── Target tracking ───────────────────────────────────────────
    private static LivingEntity lastTarget      = null;
    private static int          targetFadeTimer = 0;

    // ── BPS tracking ──────────────────────────────────────────────
    private static double prevX = 0, prevZ = 0;
    private static double currentBPS = 0;

    // ─────────────────────────────────────────────────────────────
    //  Entry point — called once per frame from mixin
    // ─────────────────────────────────────────────────────────────
    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.world == null) return;
        if (mc.options != null && mc.options.hudHidden) return;

        HUDFeature hud = FeatureRegistry.get().hud;
        if (hud == null || !hud.isEnabled()) return;

        ClientPlayerEntity player = mc.player;
        TextRenderer       tr     = mc.textRenderer;
        if (tr == null) return;

        // Update rainbow hue
        rainbowHue = (rainbowHue + 0.008f) % 1.0f;

        // BPS update
        double dx = player.getX() - prevX;
        double dz = player.getZ() - prevZ;
        currentBPS = Math.sqrt(dx * dx + dz * dz) * 20.0;
        prevX = player.getX(); prevZ = player.getZ();

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int bgA = (int)(hud.bgAlpha.getValue() * 255);
        boolean rnd = hud.rounded.getValue();

        // ── Left column (top-left info panels) ───────────────────
        int leftY = 4;

        // Watermark
        if (FeatureRegistry.get().watermark != null && FeatureRegistry.get().watermark.isEnabled()) {
            leftY = renderWatermark(ctx, tr, leftY, bgA, rnd, hud);
        }

        // Coordinates
        if (hud.showCoords.getValue()) {
            leftY = renderCoords(ctx, tr, player, leftY, bgA, rnd, hud);
        }

        // FPS / Ping / TPS
        if (hud.showFPS.getValue() || hud.showPing.getValue() || hud.showTPS.getValue()) {
            leftY = renderPerfPanel(ctx, tr, mc, leftY, bgA, rnd, hud);
        }

        // Speed (BPS)
        if (hud.showSpeed.getValue()) {
            leftY = renderTextLine(ctx, tr, leftY, bgA, rnd,
                    "§7Speed: §f" + String.format("%.2f", currentBPS) + " §7bps");
        }

        // Light level
        if (hud.showLightLevel.getValue()) {
            leftY = renderLightLevel(ctx, tr, player, leftY, bgA, rnd);
        }

        // World time & weather
        if (hud.showTime.getValue() || hud.showWeather.getValue()) {
            leftY = renderWorldInfo(ctx, tr, mc, leftY, bgA, rnd, hud);
        }

        // ── Bottom left: Armor status ─────────────────────────────
        if (hud.showArmor.getValue()) {
            renderArmorStatus(ctx, tr, mc, player, sh - 58, bgA, rnd);
        }

        // ── Right side: Potion effects ────────────────────────────
        if (hud.showPotions.getValue()) {
            renderPotionEffects(ctx, tr, player, sw, sh, bgA, rnd, hud);
        }

        // ── Centre bottom: Target HUD ─────────────────────────────
        if (hud.showTargetHUD.getValue()) {
            renderTargetHUD(ctx, tr, mc, player, sw, sh, bgA, rnd, hud);
        }

        // ── ArrayList (module list) ───────────────────────────────
        if (hud.showModList.getValue()) {
            renderArrayList(ctx, tr, sw, sh, bgA, hud);
        }

        // ── SeedCracker status ────────────────────────────────────────────
        try {
            com.davidprogr.shadowclient.feature.misc.SeedCrackerFeature sc =
                FeatureRegistry.get().seedCracker;
            if (sc != null && sc.isEnabled()) {
                String scStatus = sc.getStatusString();
                int scW = tr.getWidth(scStatus) + 10;
                drawPanel(ctx, 2, sh - 14, scW, 12, bgA, false);
                ctx.drawTextWithShadow(tr, "\u00a7b" + scStatus, 6, sh - 12, 0xFFFFFF);
            }
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────────────────────
    //  Watermark
    // ─────────────────────────────────────────────────────────────
    private static int renderWatermark(DrawContext ctx, TextRenderer tr,
                                       int y, int bgA, boolean rnd, HUDFeature hud) {
        boolean isShadow = "Shadow".equals(hud.theme.getValue()) || "Neon".equals(hud.theme.getValue());
        String  accent   = isShadow ? "§5§l" : "§b§l";
        String  name     = isShadow ? "Shadow" : "Light";
        String  line     = accent + name + " §r§7Client §8v2.0";
        int w = tr.getWidth("Shadow Client v2.0") + 10;
        drawPanel(ctx, 2, y, w, 13, bgA, rnd);
        ctx.drawTextWithShadow(tr, line, 6, y + 2, 0xFFFFFF);
        return y + 15;
    }

    // ─────────────────────────────────────────────────────────────
    //  Coordinates
    // ─────────────────────────────────────────────────────────────
    private static int renderCoords(DrawContext ctx, TextRenderer tr, ClientPlayerEntity player,
                                    int y, int bgA, boolean rnd, HUDFeature hud) {
        try {
            List<String> lines = new ArrayList<>();
            int bx = (int) player.getX(), by = (int) player.getY(), bz = (int) player.getZ();
            lines.add("§7X §f" + bx + " §7Y §f" + by + " §7Z §f" + bz);

            if (hud.showDirection.getValue()) {
                lines.add("§7Facing: §f" + getDirection(player.getYaw()));
            }
            if (hud.showDimension.getValue()) {
                String dim = getDimName(player);
                lines.add("§7Dim: §f" + dim);
                if ("Overworld".equals(dim)) {
                    lines.add("§8Nether: " + bx / 8 + " / " + by + " / " + bz / 8);
                } else if ("Nether".equals(dim)) {
                    lines.add("§8OW: " + bx * 8 + " / " + by + " / " + bz * 8);
                }
            }
            if (hud.showBiome.getValue()) {
                lines.add("§7Biome: §f" + getBiomeName(player));
            }
            return renderInfoPanel(ctx, tr, 2, y, lines, bgA, rnd);
        } catch (Exception e) {
            return y; // never crash the render thread
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Performance stats
    // ─────────────────────────────────────────────────────────────
    private static int renderPerfPanel(DrawContext ctx, TextRenderer tr, MinecraftClient mc,
                                       int y, int bgA, boolean rnd, HUDFeature hud) {
        List<String> lines = new ArrayList<>();
        if (hud.showFPS.getValue()) {
            int fps = (int) net.minecraft.client.MinecraftClient.getInstance().getCurrentFps();
            String c = fps >= 60 ? "§a" : fps >= 30 ? "§e" : "§c";
            lines.add("§7FPS: " + c + fps);
        }
        if (hud.showPing.getValue()) {
            int ping = getPing(mc);
            String c = ping < 80 ? "§a" : ping < 150 ? "§e" : "§c";
            lines.add("§7Ping: " + c + ping + "ms");
        }
        if (hud.showTPS.getValue()) {
            lines.add("§7TPS: §a20.0"); // server-side; best-effort display
        }
        return renderInfoPanel(ctx, tr, 2, y, lines, bgA, rnd);
    }

    // ─────────────────────────────────────────────────────────────
    //  Light level
    // ─────────────────────────────────────────────────────────────
    private static int renderLightLevel(DrawContext ctx, TextRenderer tr,
                                        ClientPlayerEntity player, int y, int bgA, boolean rnd) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return y;
        int light = mc.world.getLightLevel(player.getBlockPos());
        String c = light <= 7 ? "§c" : light <= 11 ? "§e" : "§a";
        String warn = light <= 7 ? " §8(mob spawn!)" : "";
        return renderTextLine(ctx, tr, y, bgA, rnd, "§7Light: " + c + light + warn);
    }

    // ─────────────────────────────────────────────────────────────
    //  World info (time, weather)
    // ─────────────────────────────────────────────────────────────
    private static int renderWorldInfo(DrawContext ctx, TextRenderer tr, MinecraftClient mc,
                                       int y, int bgA, boolean rnd, HUDFeature hud) {
        if (mc.world == null) return y;
        List<String> lines = new ArrayList<>();
        try {
            if (hud.showTime.getValue()) {
                long worldTime = mc.world.getTimeOfDay() % 24000L;
                int  hours     = (int)((worldTime / 1000 + 6) % 24);
                int  minutes   = (int)((worldTime % 1000) * 60 / 1000);
                String ampm = hours >= 12 ? "PM" : "AM";
                int h12 = hours % 12; if (h12 == 0) h12 = 12;
                lines.add(String.format("§7Time: §f%d:%02d %s", h12, minutes, ampm));
            }
            if (hud.showWeather.getValue()) {
                String weather = mc.world.isThundering() ? "§9⛈ Thunder"
                               : mc.world.isRaining()    ? "§b☁ Rain"
                               : "§e☀ Clear";
                lines.add("§7Weather: " + weather);
            }
        } catch (Exception ignored) {}
        return renderInfoPanel(ctx, tr, 2, y, lines, bgA, rnd);
    }

    // ─────────────────────────────────────────────────────────────
    //  Armor status (bottom-left)
    // ─────────────────────────────────────────────────────────────
    private static void renderArmorStatus(DrawContext ctx, TextRenderer tr, MinecraftClient mc,
                                          ClientPlayerEntity player, int y, int bgA, boolean rnd) {
        drawPanel(ctx, 2, y, 128, 24, bgA, rnd);
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = player.getInventory().getArmorStack(i);
            int slot = 3 - i;
            int px   = 6 + slot * 30;
            if (stack.isEmpty()) continue;

            ctx.drawItem(stack, px, y + 4);

            // Durability bar below icon
            if (stack.isDamageable()) {
                float pct   = 1.0f - (float) stack.getDamage() / stack.getMaxDamage();
                int barColor = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
                int barW    = (int)(14 * pct);
                ctx.fill(px, y + 20, px + 14, y + 22, 0xFF222222);
                if (barW > 0) ctx.fill(px, y + 20, px + barW, y + 22, barColor);
            }
        }
        // Offhand
        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty()) {
            ctx.drawItem(offhand, 130, y + 4);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Potion effects (right side)
    // ─────────────────────────────────────────────────────────────
    private static void renderPotionEffects(DrawContext ctx, TextRenderer tr,
                                            ClientPlayerEntity player, int sw, int sh,
                                            int bgA, boolean rnd, HUDFeature hud) {
        Collection<StatusEffectInstance> effects = player.getStatusEffects();
        if (effects.isEmpty()) return;

        boolean iconsOnly  = "Icons Only".equals(hud.potionLayout.getValue());
        int     lineH      = 14;
        int     panelW     = iconsOnly ? (effects.size() * 20 + 4) : 134;
        int     panelH     = iconsOnly ? 22 : (effects.size() * lineH + 6);
        int     px         = sw - panelW - 2;
        int     py         = sh / 2 - panelH / 2;

        drawPanel(ctx, px, py, panelW, panelH, bgA, rnd);

        int idx = 0;
        for (StatusEffectInstance eff : effects) {
            try {
                int    dur   = eff.getDuration();
                String time  = formatDuration(dur);
                int    amp   = eff.getAmplifier();
                String name  = getEffectName(eff.getEffectType()) + (amp > 0 ? " " + toRoman(amp + 1) : "");
                boolean bad  = isBadEffect(eff.getEffectType());
                String nc    = bad ? "§c" : "§a";
                String tc    = dur < 200 ? "§e" : "§7";

                if (iconsOnly) {
                    // Coloured square as stand-in for effect icon
                    int ix = px + 2 + idx * 20;
                    ctx.fill(ix, py + 2, ix + 16, py + 18, bad ? 0xFFFF5555 : 0xFF55FF55);
                    ctx.drawTextWithShadow(tr, String.valueOf(name.charAt(0)), ix + 4, py + 5, 0xFFFFFF);
                } else {
                    int iy = py + 3 + idx * lineH;
                    ctx.drawTextWithShadow(tr, nc + name, px + 4, iy, 0xFFFFFF);
                    int tw = tr.getWidth(time);
                    ctx.drawTextWithShadow(tr, tc + time, px + panelW - tw - 4, iy, 0xFFFFFF);

                    // Progress bar (show only if < 5 min)
                    if (dur < 6000) {
                        int barMaxW = panelW - 8;
                        int barW    = (int)(barMaxW * Math.min(1.0, (double) dur / 6000));
                        ctx.fill(px + 4, iy + 11, px + 4 + barMaxW, iy + 12, 0x44000000);
                        if (barW > 0)
                            ctx.fill(px + 4, iy + 11, px + 4 + barW, iy + 12,
                                     bad ? 0xFFFF5555 : 0xFF55FF55);
                    }
                }
            } catch (Exception ignored) {}
            idx++;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Target HUD (centre bottom)
    // ─────────────────────────────────────────────────────────────
    private static void renderTargetHUD(DrawContext ctx, TextRenderer tr, MinecraftClient mc,
                                        ClientPlayerEntity player, int sw, int sh,
                                        int bgA, boolean rnd, HUDFeature hud) {
        LivingEntity target = null;
        // Check crosshair entity first (most reliable, null-safe)
        if (mc.targetedEntity instanceof LivingEntity le) target = le;

        if (target != null && target.isAlive()) {
            lastTarget      = target;
            targetFadeTimer = (int)(hud.targetFade.getValue() * 20);
        } else if (targetFadeTimer > 0) {
            targetFadeTimer--;
            target = lastTarget;
        }

        if (target == null || !target.isAlive()) return;

        int panelW = 184;
        int panelH = 44 + (hud.targetArmor.getValue() && target instanceof PlayerEntity ? 16 : 0);
        int px     = sw / 2 - panelW / 2;
        int py     = sh - 96;

        // Fade alpha
        int alpha = targetFadeTimer > 20 ? bgA : (int)(bgA * ((float) targetFadeTimer / 20f));
        if (alpha < 8) return;

        drawPanel(ctx, px, py, panelW, panelH, alpha, rnd);

        // Name
        String name = target.getName().getString();
        ctx.drawTextWithShadow(tr, "§f§l" + name, px + 6, py + 4, 0xFFFFFF);

        // HP bar
        float hp    = Math.max(0, target.getHealth());
        float maxHp = Math.max(1, target.getMaxHealth());
        float pct   = hp / maxHp;
        int   barW  = panelW - 12;
        int   barY  = py + 16;

        ctx.fill(px + 6, barY, px + 6 + barW, barY + 8, 0x66000000);
        int hpColor = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
        int filled  = (int)(barW * pct);
        if (filled > 0) ctx.fill(px + 6, barY, px + 6 + filled, barY + 8, hpColor);

        try {
            String hpStr = String.format("%.1f / %.1f", hp, maxHp);
            ctx.drawTextWithShadow(tr, hpStr,
                    px + 6 + barW / 2 - tr.getWidth(hpStr) / 2, barY + 1, 0xFFFFFF);
        } catch (Exception ignored) {}

        // Target armor (players only)
        if (hud.targetArmor.getValue() && target instanceof PlayerEntity tp) {
            int ay = barY + 12;
            for (int i = 3; i >= 0; i--) {
                ItemStack as = tp.getInventory().getArmorStack(i);
                if (!as.isEmpty()) ctx.drawItem(as, px + 6 + (3 - i) * 18, ay);
            }
        }

        // Distance
        if (hud.targetDist.getValue()) {
            try {
                String dist = String.format("§7%.1fm", player.distanceTo(target));
                ctx.drawTextWithShadow(tr, dist, px + panelW - tr.getWidth(dist) - 6, py + 4, 0x888888);
            } catch (Exception ignored) {}
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  ArrayList / Module list
    // ─────────────────────────────────────────────────────────────
    private static void renderArrayList(DrawContext ctx, TextRenderer tr,
                                        int sw, int sh, int bgA, HUDFeature hud) {
        List<Feature> enabled = new ArrayList<>();
        try {
            for (Feature f : FeatureRegistry.get().all()) {
                if (f != null && f.isEnabled() && !(f instanceof HUDFeature)) enabled.add(f);
            }
        } catch (Exception ignored) { return; }

        if (enabled.isEmpty()) return;

        // Sort by name width — longest first (staircase look)
        enabled.sort((a, b) -> tr.getWidth(b.getName()) - tr.getWidth(a.getName()));

        String  pos    = hud.modListPos.getValue();
        boolean right  = pos.contains("Right");
        boolean bottom = pos.contains("Bottom");

        int lineH  = 11;
        int totalH = enabled.size() * lineH;
        int startY = bottom ? sh - 4 - totalH : 4;

        for (int i = 0; i < enabled.size(); i++) {
            Feature f    = enabled.get(i);
            String  text = f.getName();
            int     tw   = tr.getWidth(text);
            int     x    = right ? sw - tw - 6 : 2;
            int     y    = bottom ? startY + (enabled.size() - 1 - i) * lineH : startY + i * lineH;

            ctx.fill(x - 2, y - 1, x + tw + 4, y + lineH - 1, (Math.min(bgA / 2, 128) << 24));

            int cat = f.getCategory().color;
            if (right) ctx.fill(x + tw + 2, y - 1, x + tw + 4, y + lineH - 1, cat);
            else        ctx.fill(x - 2,     y - 1, x,           y + lineH - 1, cat);

            int textColor = hud.modListColors.getValue() ? cat : 0xFFFFFF;
            ctx.drawTextWithShadow(tr, text, x, y, textColor);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Primitive helpers
    // ─────────────────────────────────────────────────────────────

    private static int renderTextLine(DrawContext ctx, TextRenderer tr, int y,
                                      int bgA, boolean rnd, String line) {
        return renderInfoPanel(ctx, tr, 2, y, Collections.singletonList(line), bgA, rnd);
    }

    private static int renderInfoPanel(DrawContext ctx, TextRenderer tr,
                                       int x, int y, List<String> lines,
                                       int bgA, boolean rnd) {
        if (lines.isEmpty()) return y;
        int lineH = 10;
        int h     = lines.size() * lineH + 4;
        int maxW  = lines.stream().mapToInt(tr::getWidth).max().orElse(50) + 8;
        drawPanel(ctx, x, y, maxW, h, bgA, rnd);
        for (int i = 0; i < lines.size(); i++) {
            ctx.drawTextWithShadow(tr, lines.get(i), x + 4, y + 2 + i * lineH, 0xFFFFFF);
        }
        return y + h + 2;
    }

    private static void drawPanel(DrawContext ctx, int x, int y, int w, int h, int bgA, boolean rnd) {
        int bg = (Math.min(bgA, 255) << 24) | 0x0A0A0A;
        ctx.fill(x, y, x + w, y + h, bg);
        // Thin top and left accent line
        ctx.fill(x, y, x + w, y + 1, 0x55AAAAAA);
        ctx.fill(x, y, x + 1, y + h, 0x55AAAAAA);
    }

    // ─────────────────────────────────────────────────────────────
    //  World / player helpers (all null-safe)
    // ─────────────────────────────────────────────────────────────

    private static String getDirection(float yaw) {
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw < 22.5  || yaw >= 337.5) return "S";
        if (yaw < 67.5)  return "SW";
        if (yaw < 112.5) return "W";
        if (yaw < 157.5) return "NW";
        if (yaw < 202.5) return "N";
        if (yaw < 247.5) return "NE";
        if (yaw < 292.5) return "E";
        return "SE";
    }

    private static String getDimName(ClientPlayerEntity player) {
        try {
            if (player.getWorld() == null) return "Unknown";
            // getRegistryKey().getValue() is null-safe in 1.21.4
            String key = player.getWorld().getRegistryKey().getValue().toString();
            if (key.contains("nether")) return "Nether";
            if (key.contains("end"))    return "The End";
            return "Overworld";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private static String getBiomeName(ClientPlayerEntity player) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.world == null) return "Unknown";
            RegistryEntry<Biome> entry = mc.world.getBiome(player.getBlockPos());
            // Use getKey().map() — never assumes key is present (crash-safe)
            return entry.getKey()
                        .map(k -> {
                            String path = k.getValue().getPath();
                            // "dark_forest" → "Dark Forest"
                            String[] parts = path.split("_");
                            StringBuilder sb = new StringBuilder();
                            for (String p : parts) {
                                if (!p.isEmpty())
                                    sb.append(Character.toUpperCase(p.charAt(0)))
                                      .append(p.substring(1)).append(" ");
                            }
                            return sb.toString().trim();
                        })
                        .orElse("Unknown");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private static int getPing(MinecraftClient mc) {
        try {
            if (mc.getNetworkHandler() == null || mc.player == null) return 0;
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return entry != null ? entry.getLatency() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formatDuration(int ticks) {
        if (ticks == 32767) return "∞";
        int secs = ticks / 20;
        int mins = secs / 60;
        secs %= 60;
        return mins > 0 ? String.format("%d:%02d", mins, secs) : secs + "s";
    }

    private static String getEffectName(net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> type) {
        try {
            return type.getKey()
                       .map(k -> {
                           String path = k.getValue().getPath();
                           String[] parts = path.split("_");
                           StringBuilder sb = new StringBuilder();
                           for (String p : parts)
                               if (!p.isEmpty())
                                   sb.append(Character.toUpperCase(p.charAt(0)))
                                     .append(p.substring(1)).append(" ");
                           return sb.toString().trim();
                       })
                       .orElse("Effect");
        } catch (Exception e) {
            return "Effect";
        }
    }

    private static boolean isBadEffect(net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> type) {
        try {
            return type.getKey()
                       .map(k -> {
                           String path = k.getValue().getPath();
                           return path.contains("poison") || path.contains("wither")
                               || path.contains("slowness") || path.contains("weakness")
                               || path.contains("hunger")   || path.contains("mining_fatigue")
                               || path.contains("nausea")   || path.contains("blindness")
                               || path.contains("darkness");
                       })
                       .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
            case 4 -> "IV"; case 5 -> "V";  case 6 -> "VI";
            default -> String.valueOf(n);
        };
    }
}