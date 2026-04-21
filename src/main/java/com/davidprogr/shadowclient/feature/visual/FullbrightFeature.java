package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.SliderSetting;
import net.minecraft.client.MinecraftClient;

/** Legit-allowed. */
public class FullbrightFeature extends Feature {

    public final SliderSetting gamma = addSetting(new SliderSetting("Gamma","Brightness level",16.0,1.0,16.0,1.0));
    private double prevGamma = 1.0;

    public FullbrightFeature() { super("Fullbright","Makes dark areas fully visible", Category.VISUAL, true); }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) { prevGamma = mc.options.getGamma().getValue(); mc.options.getGamma().setValue(gamma.getValue()); }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) mc.options.getGamma().setValue(prevGamma);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) mc.options.getGamma().setValue(gamma.getValue());
    }
}
