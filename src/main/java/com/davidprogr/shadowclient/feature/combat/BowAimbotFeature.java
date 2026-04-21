package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

/** Shadow-only. Predicts and aims at targets when drawing a bow or crossbow. */
public class BowAimbotFeature extends Feature {
    public final SliderSetting  range     = addSetting(new SliderSetting("Range","Max target range",20,5,60,1,"b"));
    public final BooleanSetting players   = addSetting(new BooleanSetting("Players","Target players",true));
    public final BooleanSetting mobs      = addSetting(new BooleanSetting("Mobs","Target mobs",false));
    public final BooleanSetting predict   = addSetting(new BooleanSetting("Predict","Lead-correct for moving targets",true));
    public final SliderSetting  fovCheck  = addSetting(new SliderSetting("FOV Check","Only aim in this FOV",90,10,180,5,"°"));

    private static final double ARROW_SPEED = 3.0; // blocks/tick approx at full draw

    public BowAimbotFeature() { super("BowAimbot","Auto-aims bow/crossbow at targets", Category.COMBAT, false); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        ItemStack held = mc.player.getMainHandStack();
        boolean hasBow = held.getItem() instanceof BowItem || held.getItem() instanceof CrossbowItem;
        if (!hasBow) return;

        // Only aim while drawing bow
        boolean drawing = mc.player.isUsingItem();
        if (!drawing) return;

        LivingEntity best = null;
        double bestDist = range.getValue() + 1;

        java.util.List<net.minecraft.entity.Entity> entityList = new java.util.ArrayList<>();
        mc.world.getEntities().forEach(entityList::add);
        for (var entity : entityList) {
            if (!(entity instanceof LivingEntity le)) continue;
            if (le == mc.player || le.isDead() || le.getHealth() <= 0) continue;
            if (le instanceof PlayerEntity && !players.getValue()) continue;
            if (!(le instanceof PlayerEntity) && !mobs.getValue()) continue;
            double d = mc.player.distanceTo(le);
            if (d > range.getValue()) continue;
            if (d < bestDist) { bestDist = d; best = le; }
        }
        if (best == null) return;

        Vec3d targetPos = best.getPos().add(0, best.getHeight() * 0.5, 0);
        if (predict.getValue()) {
            // Simple linear prediction
            double ticks = bestDist / ARROW_SPEED;
            targetPos = targetPos.add(
                best.getVelocity().x * ticks,
                best.getVelocity().y * ticks * 0.5,
                best.getVelocity().z * ticks
            );
        }

        Vec3d eyes = mc.player.getEyePos();
        Vec3d diff = targetPos.subtract(eyes);
        double hDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw   = (float)(Math.toDegrees(Math.atan2(-diff.x, diff.z)));
        float pitch = (float)(Math.toDegrees(-Math.atan2(diff.y, hDist)));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
}
