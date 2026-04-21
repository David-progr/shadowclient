package com.davidprogr.shadowclient.feature.utility;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;

public class AntiAFKFeature extends Feature {

    public final ModeSetting   action   = addSetting(new ModeSetting("Action","What to do",new String[]{"Jump","Rotate","Sneak","Jump+Rotate"},"Jump+Rotate"));
    public final SliderSetting interval = addSetting(new SliderSetting("Interval","Seconds between actions",30,5,120,5));

    private int ticker = 0;
    private int rotDir = 1;

    public AntiAFKFeature() {
        super("AntiAFK","Prevents AFK disconnect", Category.UTILITY);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int ticks = (int)(interval.getValue() * 20);
        if (++ticker < ticks) return;
        ticker = 0;

        switch (action.getValue()) {
            case "Jump"   -> mc.player.jump();
            case "Rotate" -> { mc.player.setYaw(mc.player.getYaw() + rotDir * 90); rotDir *= -1; }
            case "Sneak"  -> mc.player.setSneaking(!mc.player.isSneaking());
            default       -> { mc.player.jump(); mc.player.setYaw(mc.player.getYaw() + rotDir * 30); rotDir *= -1; }
        }
    }
}
