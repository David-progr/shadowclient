package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.SliderSetting;

/** Legit-allowed. itemUseCooldown reduced via mixin. */
public class FastPlaceFeature extends Feature {
    public final SliderSetting delay = addSetting(new SliderSetting("Delay","Placement cooldown ticks",0,0,4,1));
    public FastPlaceFeature() { super("FastPlace","Removes block placement delay", Category.MOVEMENT, true); }
}
