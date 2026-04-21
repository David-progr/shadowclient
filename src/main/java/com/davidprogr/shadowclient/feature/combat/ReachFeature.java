package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.SliderSetting;

/** Shadow-only. Extended attack range. Applied via mixin. */
public class ReachFeature extends Feature {

    public final SliderSetting reach = addSetting(
            new SliderSetting("Reach", "Extra reach added to base 3.0", 1.5, 0.1, 5.0, 0.1, "b"));

    public ReachFeature() {
        super("Reach", "Increases attack and interact range", Category.COMBAT, false);
    }

    public double getReach() {
        return isEnabled() ? 3.0 + reach.getValue() : 3.0;
    }
}
