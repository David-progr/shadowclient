package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Criticals — Shadow only.
 *
 * AC bypass per mode:
 * ─ Packet:  Sends spoof fall packets (y+0.11 → y+0.0625 → y) before the hit.
 *            Works on most servers. Flagged by Grim GroundProof.
 * ─ Grim:    Single jump packet (y+0.42, onGround=false) — passes GroundProof
 *            because Grim's simulation accepts one airborne frame as a real jump.
 * ─ Vulcan/NCP: Real jump-reset (setVelocity y=0.42) — produces a legitimate crit.
 *            KillAura fires 1 tick later while the player is airborne.
 *
 * NOTE: PlayerMoveC2SPacket.PositionAndOnGround in 1.21.4 Yarn takes
 *       (double x, double y, double z, boolean onGround, boolean horizontalCollision)
 */
public class CriticalHitFeature extends Feature {

    public final ModeSetting mode = addSetting(new ModeSetting(
            "AC Bypass","Bypass method",
            new String[]{"Packet","Grim","Vulcan","NCP"}, "Vulcan"));

    private boolean pendingCrit = false;

    public CriticalHitFeature() {
        super("Criticals","Forces critical hits", Category.COMBAT, false);
    }

    /** Called by ClientPlayerEntityMixin just before an attack is sent. */
    public void applyCrit(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.getNetworkHandler() == null) return;
        if (!mc.player.isOnGround()) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        switch (mode.getValue()) {
            case "Packet" -> {
                var net = mc.getNetworkHandler();
                // Extra boolean = horizontalCollision — always false here
                net.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.11,   z, false, false));
                net.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false, false));
                net.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y,          z, false, false));
                net.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y,          z, true,  false));
            }
            case "Grim" -> {
                var net = mc.getNetworkHandler();
                // Single jump frame — passes Grim GroundProof simulation
                net.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.42, z, false, false));
                net.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y,        z, false, false));
            }
            case "Vulcan", "NCP" -> {
                // Real physics jump — no packet manipulation, no flags
                mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                pendingCrit = true;
            }
        }
    }

    public boolean isPendingCrit() {
        if (!pendingCrit) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) { pendingCrit = false; return false; }
        if (mc.player.isOnGround()) { pendingCrit = false; return false; }
        return true;
    }
}
