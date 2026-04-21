package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;

/**
 * Velocity — modifies knockback multipliers so the player takes reduced/modified
 * knockback without completely cancelling it (less suspicious than AntiKB Full).
 * Shadow-only.
 */
public class VelocityFeature extends Feature {
    public final SliderSetting horizontal = addSetting(new SliderSetting("Horizontal","Horizontal KB %",30,0,100,5,"%"));
    public final SliderSetting vertical   = addSetting(new SliderSetting("Vertical","Vertical KB %",100,0,100,5,"%"));
    public final ModeSetting   mode       = addSetting(new ModeSetting("Mode","How to reduce",new String[]{"Multiply","Cancel on Ground","Jump Cancel"},"Multiply"));

    public VelocityFeature() { super("Velocity","Reduces or modifies knockback velocity", Category.MOVEMENT, false); }

    /** Called from ClientPlayerEntityMixin.takeKnockback to scale velocity. */
    public double scaleH(double v) { return v * (horizontal.getValue() / 100.0); }
    public double scaleV(double v) { return v * (vertical.getValue()   / 100.0); }
}
