package com.davidprogr.shadowclient.feature.misc;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;

public class HitmarkerFeature extends Feature {

    public final SliderSetting  size     = addSetting(new SliderSetting("Size","Hitmarker size in pixels",10,4,30,1));
    public final SliderSetting  duration = addSetting(new SliderSetting("Duration","Frames to show hitmarker",8,2,20,1));
    public final BooleanSetting sound    = addSetting(new BooleanSetting("Sound","Play click sound on hit",false));
    public final ModeSetting    color    = addSetting(new ModeSetting("Color","Hitmarker color",new String[]{"White","Red","Green","Cyan"},"White"));

    private int displayTimer = 0;

    public HitmarkerFeature() {
        super("Hitmarker","Shows a hitmarker when you damage an entity", Category.MISC);
    }

    public void onHit() {
        displayTimer = duration.getValue().intValue();
    }

    public boolean shouldRender() {
        if (displayTimer > 0) { displayTimer--; return true; }
        return false;
    }
}
