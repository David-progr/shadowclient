package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.SliderSetting;
import net.minecraft.client.MinecraftClient;

/** Legit-allowed (camera detachment, no movement exploit). */
public class FreeCamFeature extends Feature {

    public final SliderSetting speed = addSetting(new SliderSetting("Speed","Camera speed",0.3,0.05,2.0,0.05));

    private double savedX, savedY, savedZ;
    private float  savedYaw, savedPitch;

    public FreeCamFeature() { super("FreeCam","Detach camera from your body", Category.MOVEMENT, true); }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        savedX = mc.player.getX(); savedY = mc.player.getY(); savedZ = mc.player.getZ();
        savedYaw = mc.player.getYaw(); savedPitch = mc.player.getPitch();
        mc.player.noClip = true;
        mc.player.setVelocity(0, 0, 0);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.noClip = false;
        mc.player.requestTeleport(savedX, savedY, savedZ);
        mc.player.setYaw(savedYaw); mc.player.setPitch(savedPitch);
        mc.player.setVelocity(0, 0, 0);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.noClip = true;
        mc.player.setVelocity(0, 0, 0);
        mc.player.fallDistance = 0;
    }
}
