package com.davidprogr.shadowclient.feature.misc;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.SliderSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Timer — changes the client tick rate to speed up/slow down gameplay.
 * Shadow-only. Uses reflection to call getTickManager().setTickRate(float).
 * Completely reflection-based — safe to compile even if API is different.
 */
public class TimerFeature extends Feature {
    public final SliderSetting speed = addSetting(new SliderSetting("Speed","Game tick multiplier",1.5,0.1,10.0,0.1,"x"));

    public TimerFeature() { super("Timer","Speeds up or slows down game ticks", Category.MISC, false); }

    private static void setTickRate(float rate) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            Object tickManager = mc.getClass().getMethod("getTickManager").invoke(mc);
            if (tickManager != null) {
                tickManager.getClass().getMethod("setTickRate", float.class).invoke(tickManager, rate);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onTick() {
        setTickRate((float)(20.0 * speed.getValue()));
    }

    @Override
    public void onDisable() {
        setTickRate(20.0f);
    }
}
