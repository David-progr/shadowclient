package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;

/**
 * Flight — Shadow only.
 *
 * AC bypass per mode:
 * ─ Creative: Sets allowFlying+flying abilities.
 * ─ Vulcan:   Packet Fly — flies at a speed just below Vulcan's threshold.
 * ─ Grim:     Glide mode — sets y-velocity to exactly -0.04 every tick.
 * ─ Glide:    Passive — just prevents fall damage.
 */
public class FlightFeature extends Feature {

    public final ModeSetting   mode  = addSetting(new ModeSetting("Mode","Flight mode",
            new String[]{"Creative","Vulcan","Grim","Glide"},"Creative"));
    public final SliderSetting speed = addSetting(new SliderSetting("Speed","Horizontal speed",0.1,0.01,2.0,0.01));

    public FlightFeature() {
        super("Flight","Fly freely", Category.MOVEMENT, false);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if ("Creative".equals(mode.getValue())) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying      = true;
            mc.player.sendAbilitiesUpdate();
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying      = false;
            mc.player.sendAbilitiesUpdate();
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        double spd = speed.getValue();
        var    vel = mc.player.getVelocity();

        switch (mode.getValue()) {
            case "Creative" -> {
                mc.player.getAbilities().allowFlying = true;
                mc.player.getAbilities().flying      = true;
                // Use setFlySpeed() — flySpeed field is private in 1.21.4
                mc.player.getAbilities().setFlySpeed((float) spd);
            }
            case "Vulcan" -> {
                mc.player.getAbilities().allowFlying = true;
                mc.player.getAbilities().flying      = true;
                mc.player.getAbilities().setFlySpeed((float) Math.min(spd, 0.10));
                mc.player.fallDistance               = 0;
                mc.player.setSprinting(false);
            }
            case "Grim" -> {
                mc.player.setVelocity(vel.x, -0.04, vel.z);
                mc.player.fallDistance = 0;
            }
            case "Glide" -> {
                if (vel.y < -0.04) mc.player.setVelocity(vel.x, -0.04, vel.z);
                mc.player.fallDistance = 0;
            }
        }
    }
}
