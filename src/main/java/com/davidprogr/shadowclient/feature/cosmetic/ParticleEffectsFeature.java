package com.davidprogr.shadowclient.feature.cosmetic;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.BooleanSetting;
import com.davidprogr.shadowclient.feature.setting.ModeSetting;
import com.davidprogr.shadowclient.feature.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;

/**
 * ParticleEffects — Legit-allowed.
 * Spawns colored trail and hit-burst particles.
 * Uses DustParticleEffect(int argb, float size) constructor (1.21.4 API).
 */
public class ParticleEffectsFeature extends Feature {
    private final ModeSetting    style      = new ModeSetting("Style", "Particle style",
            new String[]{"Rainbow","Solid","Fire","Ice"}, "Rainbow");
    private final ColorSetting   solidColor = new ColorSetting("Solid Color", "Color for Solid mode", 0xFFAA00FF);
    private final BooleanSetting onHit      = new BooleanSetting("On Hit",  "Spawn particles on hit",  true);
    private final BooleanSetting onKill     = new BooleanSetting("On Kill", "Spawn particles on kill", true);
    private final BooleanSetting trail      = new BooleanSetting("Trail",   "Spawn trail while walking", false);

    private float hue = 0f;

    public ParticleEffectsFeature() {
        super("Particles", "Custom particle effects for actions", Category.COSMETIC, true);
        addSetting(style);
        addSetting(solidColor);
        addSetting(onHit);
        addSetting(onKill);
        addSetting(trail);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        hue = (hue + 0.01f) % 1f;

        if (trail.getValue()) {
            spawnTrailParticles(mc);
        }
    }

    private void spawnTrailParticles(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        int argb = getParticleColor();
        double x = mc.player.getX() + (Math.random() - 0.5) * 0.5;
        double y = mc.player.getY();
        double z = mc.player.getZ() + (Math.random() - 0.5) * 0.5;
        // DustParticleEffect(int argbColor, float size)
        mc.world.addParticle(
            new DustParticleEffect(argb, 1.0f),
            x, y, z, 0.0, 0.05, 0.0
        );
    }

    public void onHit() {
        if (!isEnabled() || !onHit.getValue()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        spawnBurstParticles(mc, 8);
    }

    public void onKill() {
        if (!isEnabled() || !onKill.getValue()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        spawnBurstParticles(mc, 20);
    }

    private void spawnBurstParticles(MinecraftClient mc, int count) {
        if (mc.player == null || mc.world == null) return;
        int argb = getParticleColor();
        for (int i = 0; i < count; i++) {
            double vx = (Math.random() - 0.5) * 0.3;
            double vy = Math.random() * 0.3;
            double vz = (Math.random() - 0.5) * 0.3;
            mc.world.addParticle(
                new DustParticleEffect(argb, 1.0f),
                mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ(),
                vx, vy, vz
            );
        }
    }

    private int getParticleColor() {
        return switch (style.getValue()) {
            case "Rainbow" -> java.awt.Color.HSBtoRGB(hue, 1f, 1f);
            case "Fire"    -> java.awt.Color.HSBtoRGB(0.05f + (float)(Math.random() * 0.1), 1f, 1f);
            case "Ice"     -> java.awt.Color.HSBtoRGB(0.55f + (float)(Math.random() * 0.05), 0.8f, 1f);
            default        -> solidColor.getValue();
        };
    }
}
