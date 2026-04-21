package com.davidprogr.shadowclient.feature.cosmetic;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.ColorSetting;

public class HitColorFeature extends Feature {
    public final ColorSetting hitColor = new ColorSetting("Hit Color", "Hit indicator color", 0xFFFF4444);
    public final ColorSetting critColor = new ColorSetting("Crit Color", "Critical hit color", 0xFFFF8800);
    public final ColorSetting killColor = new ColorSetting("Kill Color", "Kill indicator color", 0xFFAA00FF);

    public HitColorFeature() {
        super("HitColor", "Custom damage indicator colors", Category.COSMETIC, true);
        addSetting(hitColor);
        addSetting(critColor);
        addSetting(killColor);
    }
}
