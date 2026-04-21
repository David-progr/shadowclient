package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;

/**
 * SprintReset — briefly stops sprint on hit (to reset attack cooldown interaction)
 * and instantly re-sprints, giving the classic 1.8 "w-tap" feel.
 * Legit-allowed (manual w-tap is legal everywhere).
 */
public class SprintResetFeature extends Feature {
    public final BooleanSetting autoWTap = addSetting(new BooleanSetting("Auto W-Tap","Auto w-tap on attack",true));

    private int resetTimer = 0;

    public SprintResetFeature() { super("SprintReset","W-tap automation for better hit registration", Category.MOVEMENT, true); }

    public void onAttack() {
        if (!isEnabled() || !autoWTap.getValue()) return;
        resetTimer = 2; // 2-tick sprint reset
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (resetTimer > 0) {
            resetTimer--;
            if (resetTimer == 1) mc.player.setSprinting(false);
            if (resetTimer == 0) mc.player.setSprinting(true);
        }
    }
}
