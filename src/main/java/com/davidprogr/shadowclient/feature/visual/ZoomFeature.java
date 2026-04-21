package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;

/** Legit-allowed. FOV override via GameRendererMixin.getFov. */
public class ZoomFeature extends Feature {

    public final SliderSetting  fov          = addSetting(new SliderSetting("FOV","Zoomed FOV degrees",10.0,1.0,50.0,1.0,"°"));
    public final BooleanSetting smoothZoom   = addSetting(new BooleanSetting("Smooth","Smooth interpolation",true));
    public final BooleanSetting scrollAdjust = addSetting(new BooleanSetting("Scroll","Adjust FOV with scroll wheel",true));

    private double currentFov = 70.0;
    private double targetFov  = 10.0;
    private double baseFov    = 70.0;

    public ZoomFeature() { super("Zoom","OptiFine-style zoom", Category.VISUAL, true); }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) baseFov = mc.options.getFov().getValue();
        targetFov = fov.getValue();
        currentFov = baseFov;
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) targetFov = mc.options.getFov().getValue();
    }

    public float getRenderedFov(float baseMcFov) {
        if (!isEnabled()) return baseMcFov;
        if (smoothZoom.getValue()) {
            currentFov += (targetFov - currentFov) * 0.15;
            return (float) currentFov;
        }
        return (float) targetFov;
    }

    public void onScroll(double delta) {
        if (!isEnabled() || !scrollAdjust.getValue()) return;
        fov.setValue(fov.getValue() - delta * 2.0);
        targetFov = fov.getValue();
    }
}
