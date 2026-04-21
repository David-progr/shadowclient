package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Shadow-only. Soft aim toward nearest target with jitter for AC bypass. */
public class AimAssistFeature extends Feature {

    public final SliderSetting  range    = addSetting(new SliderSetting("Range","Target scan range",6.0,1.0,15.0,0.5,"b"));
    public final SliderSetting  strength = addSetting(new SliderSetting("Strength","Snap speed (1=instant)",0.15,0.01,1.0,0.01));
    public final BooleanSetting players  = addSetting(new BooleanSetting("Players","Aim at players",true));
    public final BooleanSetting mobs     = addSetting(new BooleanSetting("Mobs","Aim at mobs",false));
    public final BooleanSetting jitter   = addSetting(new BooleanSetting("Jitter","Add micro-noise (AC bypass)",true));

    private final Random rng = new Random();

    public AimAssistFeature() {
        super("AimAssist","Softly snaps aim toward nearby targets", Category.COMBAT, false);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        double maxDistSq = range.getValue() * range.getValue();
        Entity best = null;
        double bestDist = maxDistSq;

        // Safe copy to avoid CME
        List<net.minecraft.entity.Entity> entityList = new ArrayList<>();
        mc.world.getEntities().forEach(entityList::add);
        for (Entity e : entityList) {
            if (e == mc.player || !(e instanceof LivingEntity le)) continue;
            if (le.getHealth() <= 0) continue;
            if (!players.getValue() && e instanceof PlayerEntity) continue;
            if (!mobs.getValue()    && !(e instanceof PlayerEntity)) continue;

            double d = mc.player.squaredDistanceTo(e);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        if (best == null) return;

        double dx   = best.getX() - mc.player.getX();
        double dy   = best.getEyeY() - mc.player.getEyeY();
        double dz   = best.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist == 0) return;

        float targetYaw   = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        float spd         = (float)(double) strength.getValue();

        float newYaw   = mc.player.getYaw()   + (targetYaw   - mc.player.getYaw())   * spd;
        float newPitch = mc.player.getPitch() + (targetPitch - mc.player.getPitch()) * spd;

        // Micro-jitter to prevent AimDuplicateLook (Grim) and Aim check (Vulcan)
        if (jitter.getValue()) {
            newYaw   += (rng.nextFloat() - 0.5f) * 0.1f;
            newPitch += (rng.nextFloat() - 0.5f) * 0.05f;
        }

        mc.player.setYaw(newYaw);
        mc.player.setPitch(Math.max(-90, Math.min(90, newPitch)));
    }
}
