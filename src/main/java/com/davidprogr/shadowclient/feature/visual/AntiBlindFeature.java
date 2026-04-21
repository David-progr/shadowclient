package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;

/** Legit-allowed. Removes blindness/darkness visual overlay. */
public class AntiBlindFeature extends Feature {
    public AntiBlindFeature() { super("AntiBlind","Removes blindness and darkness effects", Category.VISUAL, true); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        try {
            mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
            mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        } catch (Exception ignored) {}
    }
}
