package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * KillAura — Shadow only.
 *
 * AC bypass per mode:
 * ─ Grim:   Micro-jitter on every rotation (prevents AimDuplicateLook).
 *            Random 0–2 extra ticks between attacks. Attack only when vanilla
 *            cooldown ≥ 90% so Grim simulation sees a plausible hit.
 * ─ Vulcan: Sprint-cancel before every hit (bypasses KeepSprint check).
 *            Smooth rotation capped at 15°/tick. CPS randomised ±20%.
 * ─ NCP:    Attack only when cooldown ≥ 95%. No rotation snap.
 * ─ Legit:  Slow smooth aim, 4–10 CPS, random extra delays, always jitter.
 * ─ Rage:   Max speed, no guards (singleplayer / private server).
 *
 * Crash safety:
 * ─ Entity list is COPIED to ArrayList before iteration (no CME).
 * ─ Every mc.player / mc.world access is null-guarded.
 * ─ target == mc.player guard prevents self-attack crash.
 */
public class KillAuraFeature extends Feature {

    public final ModeSetting    acMode       = addSetting(new ModeSetting("AC Bypass","Bypass mode",
            new String[]{"Grim","Vulcan","NCP","Legit","Rage"},"Vulcan"));
    public final SliderSetting  range        = addSetting(new SliderSetting("Range","Attack range",4.0,1.0,8.0,0.1,"b"));
    public final SliderSetting  cps          = addSetting(new SliderSetting("CPS","Max attacks/sec",8.0,1.0,20.0,1.0));
    public final SliderSetting  cpsRand      = addSetting(new SliderSetting("CPS Jitter","±CPS variance",1.0,0.0,5.0,0.5));
    public final ModeSetting    target       = addSetting(new ModeSetting("Target","What to attack",
            new String[]{"Players","Mobs","Animals","All"},"Players"));
    public final BooleanSetting rotations    = addSetting(new BooleanSetting("Rotate","Rotate to target",true));
    public final SliderSetting  rotSmooth    = addSetting(new SliderSetting("Rot Speed","Max °/tick",15.0,1.0,180.0,1.0));
    public final BooleanSetting rotJitter    = addSetting(new BooleanSetting("Rot Jitter","Micro-noise on aim",true));
    public final BooleanSetting swingAnim    = addSetting(new BooleanSetting("Swing Arm","Show arm swing",true));
    public final BooleanSetting cancelSprint = addSetting(new BooleanSetting("Cancel Sprint","Cancel sprint pre-hit (Vulcan)",true));

    private int    tickCooldown = 0;
    private final  Random rng   = new Random();
    public  LivingEntity currentTarget = null;

    public KillAuraFeature() {
        super("KillAura","Auto-attacks nearby entities", Category.COMBAT, false);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (tickCooldown > 0) { tickCooldown--; return; }

        // NCP: require near-full attack cooldown
        if ("NCP".equals(acMode.getValue())) {
            if (mc.player.getAttackCooldownProgress(0f) < 0.95f) return;
        }
        // Grim: require 90% cooldown to avoid impossible-hit flag
        if ("Grim".equals(acMode.getValue())) {
            if (mc.player.getAttackCooldownProgress(0f) < 0.90f) return;
        }

        currentTarget = findTarget(mc);
        if (currentTarget == null) return;

        if (rotations.getValue()) rotateTo(mc, currentTarget);

        // Vulcan: cancel sprint before the hit packet
        if ("Vulcan".equals(acMode.getValue()) && cancelSprint.getValue()) {
            mc.player.setSprinting(false);
        }

        mc.interactionManager.attackEntity(mc.player, currentTarget);
        if (swingAnim.getValue()) mc.player.swingHand(Hand.MAIN_HAND);

        tickCooldown = computeCooldown();
    }

    private LivingEntity findTarget(MinecraftClient mc) {
        double maxDistSq = range.getValue() * range.getValue();
        LivingEntity best = null;
        double bestDist   = maxDistSq;

        // Copy entity list to avoid ConcurrentModificationException
        List<Entity> entities = new java.util.ArrayList<>();
        mc.world.getEntities().forEach(entities::add);
        for (Entity e : entities) {
            if (e == mc.player || !(e instanceof LivingEntity le)) continue;
            if (le.getHealth() <= 0) continue;

            boolean valid = switch (target.getValue()) {
                case "Players" -> e instanceof PlayerEntity;
                case "Mobs"    -> e instanceof HostileEntity || e instanceof SlimeEntity;
                case "Animals" -> e instanceof AnimalEntity;
                default        -> e instanceof PlayerEntity || e instanceof HostileEntity
                               || e instanceof SlimeEntity  || e instanceof AnimalEntity;
            };
            if (!valid) continue;

            double d = mc.player.squaredDistanceTo(e);
            if (d < bestDist) { bestDist = d; best = le; }
        }
        return best;
    }

    private void rotateTo(MinecraftClient mc, Entity e) {
        double dx   = e.getX() - mc.player.getX();
        double dy   = e.getEyeY() - mc.player.getEyeY();
        double dz   = e.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist == 0) return;

        float targetYaw   = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));

        float maxStep = (float)(double) rotSmooth.getValue();
        float dYaw    = clampStep(targetYaw   - mc.player.getYaw(),   maxStep);
        float dPitch  = clampStep(targetPitch - mc.player.getPitch(), maxStep);

        float newYaw   = mc.player.getYaw()   + dYaw;
        float newPitch = mc.player.getPitch() + dPitch;

        // Grim / Legit: micro-jitter prevents AimDuplicateLook
        if (rotJitter.getValue()) {
            String m = acMode.getValue();
            if ("Grim".equals(m) || "Legit".equals(m)) {
                newYaw   += (rng.nextFloat() - 0.5f) * 0.08f;
                newPitch += (rng.nextFloat() - 0.5f) * 0.04f;
            }
        }

        mc.player.setYaw(newYaw);
        mc.player.setPitch(Math.max(-90, Math.min(90, newPitch)));
    }

    private float clampStep(float delta, float max) {
        while (delta >  180) delta -= 360;
        while (delta < -180) delta += 360;
        return Math.max(-max, Math.min(max, delta));
    }

    private int computeCooldown() {
        double raw   = cps.getValue() + (rng.nextDouble() * 2 - 1) * cpsRand.getValue();
        raw = Math.max(1, Math.min(20, raw));
        int base = (int)(20.0 / raw);
        int extra = switch (acMode.getValue()) {
            case "Legit"  -> rng.nextInt(4);
            case "Grim"   -> rng.nextInt(3);
            case "Vulcan" -> rng.nextInt(2);
            default       -> 0;
        };
        return Math.max(1, base + extra);
    }
}
