package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;

/**
 * Speed — Shadow only.
 *
 * AC bypass per mode:
 * ─ Strafe (default): Boosts horizontal velocity directly.
 *   Detected by most ACs at high values — keep multiplier ≤ 1.5 for bypass.
 * ─ Vulcan: Uses Vulcan's Speed bypass — applies velocity only every other tick
 *   and keeps sprint enabled. Stays under Vulcan's motion threshold with
 *   gradual acceleration. Tested max safe value: ~1.35× base.
 * ─ Grim:   "BHop" style — applies a small burst at the peak of each jump so
 *   Grim's ground simulation sees normal ground speed. Airborne boost of 0.005
 *   per tick is under Grim's air-speed deviation threshold.
 * ─ Rage:   Maximum velocity, no restrictions.
 */
public class SpeedFeature extends Feature {

    public final ModeSetting   mode  = addSetting(new ModeSetting("Mode","Speed method",
            new String[]{"Strafe","Vulcan","Grim","Rage"},"Strafe"));
    public final SliderSetting speed = addSetting(new SliderSetting("Speed","Multiplier",1.3,1.0,5.0,0.05));

    private int    altTick   = 0;
    private double savedSpeed = 0;

    public SpeedFeature() {
        super("Speed","Move faster than normal", Category.MOVEMENT, false);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        var vel = mc.player.getVelocity();
        double len = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (len < 0.001) return; // not moving — don't apply

        altTick++;

        switch (mode.getValue()) {
            case "Strafe" -> {
                double target = speed.getValue() * 0.28;
                mc.player.setVelocity(vel.x / len * target, vel.y, vel.z / len * target);
                mc.player.setSprinting(true);
            }
            case "Vulcan" -> {
                // Apply boost every other tick only (Vulcan checks sustained high speed)
                if (altTick % 2 == 0) {
                    double cap = Math.min(speed.getValue(), 1.35) * 0.28;
                    if (len < cap) mc.player.setVelocity(vel.x / len * cap, vel.y, vel.z / len * cap);
                }
                mc.player.setSprinting(true);
            }
            case "Grim" -> {
                // BHop: extra burst only on the tick the player leaves the ground
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
                // Small per-tick air boost — under Grim deviation threshold
                if (!mc.player.isOnGround() && len > 0.001) {
                    double boost = len + 0.005 * (speed.getValue() - 1.0);
                    mc.player.setVelocity(vel.x / len * boost, vel.y, vel.z / len * boost);
                }
            }
            case "Rage" -> {
                double target = speed.getValue() * 0.28;
                mc.player.setVelocity(vel.x / len * target, vel.y, vel.z / len * target);
                mc.player.setSprinting(true);
            }
        }
    }
}
