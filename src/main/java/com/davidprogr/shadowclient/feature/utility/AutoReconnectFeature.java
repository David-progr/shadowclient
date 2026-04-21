package com.davidprogr.shadowclient.feature.utility;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;

public class AutoReconnectFeature extends Feature {

    public final SliderSetting delay = addSetting(new SliderSetting("Delay","Seconds before reconnecting",5,1,60,1));

    private int    countdown = 0;
    private boolean pending  = false;

    public AutoReconnectFeature() {
        super("AutoReconnect","Automatically reconnects after disconnect", Category.UTILITY);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen instanceof DisconnectedScreen screen) {
            if (!pending) {
                pending   = true;
                countdown = (int)(delay.getValue() * 20);
            }
            if (countdown-- <= 0) {
                pending = false;
                // Press the "Reconnect" button if present
                screen.children().stream()
                    .filter(w -> w instanceof net.minecraft.client.gui.widget.ButtonWidget btn
                            && btn.getMessage().getString().contains("Reconnect"))
                    .findFirst()
                    .ifPresent(w -> ((net.minecraft.client.gui.widget.ButtonWidget)w).onPress());
            }
        } else {
            pending = false;
        }
    }
}
