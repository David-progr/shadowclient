package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * NoFall — Shadow only.
 *
 * AC bypass per mode:
 * ─ Packet:  Sets onGround=true in a movement packet as soon as
 *            fallDistance exceeds 2.  Simple, works on most servers.
 * ─ Grim:    Resets fallDistance every tick AND sends onGround=true only
 *            once per 20 ticks to dodge Grim's repetition flag.
 * ─ Vulcan:  Sends a single PositionAndOnGround(true) packet when
 *            fallDistance > 3 — passes Vulcan's less-strict check.
 */
public class NoFallFeature extends Feature {

    public final ModeSetting mode = addSetting(new ModeSetting("AC Bypass","Bypass mode",
            new String[]{"Packet","Grim","Vulcan"},"Vulcan"));

    private int grimTimer = 0;

    public NoFallFeature() {
        super("NoFall","Cancel all fall damage", Category.MOVEMENT, false);
    }

    @Override
    public void onDisable() { grimTimer = 0; }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        float fall = mc.player.fallDistance;

        double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();

        switch (mode.getValue()) {
            case "Packet" -> {
                if (fall > 2.0f) {
                    mc.getNetworkHandler().sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true, false));
                    mc.player.fallDistance = 0;
                }
            }
            case "Grim" -> {
                // Reset fall every tick so server never sees a dangerous fall distance
                mc.player.fallDistance = 0;
                // Send onGround=true packet periodically so Grim's timer is satisfied
                if (++grimTimer >= 20) {
                    grimTimer = 0;
                    mc.getNetworkHandler().sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true, false));
                }
            }
            case "Vulcan" -> {
                if (fall > 3.0f) {
                    mc.getNetworkHandler().sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true, false));
                    mc.player.fallDistance = 0;
                }
            }
        }
    }
}
