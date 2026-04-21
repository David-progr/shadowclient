package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import net.minecraft.client.MinecraftClient;

/** Legit-allowed. */
public class ToggleSprintFeature extends Feature {
    public ToggleSprintFeature() { super("ToggleSprint","Always sprint when moving forward", Category.MOVEMENT, true); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (mc.player.input.movementForward > 0 && !mc.player.isTouchingWater() && !mc.player.isClimbing())
            mc.player.setSprinting(true);
    }
}
