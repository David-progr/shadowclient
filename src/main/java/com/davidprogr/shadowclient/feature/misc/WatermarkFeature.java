package com.davidprogr.shadowclient.feature.misc;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;

public class WatermarkFeature extends Feature {

    public final ModeSetting style = addSetting(new ModeSetting("Style","Watermark style",new String[]{"Simple","Gradient","Fancy"},"Gradient"));

    public WatermarkFeature() {
        super("Watermark","Shows Shadow Client branding on screen", Category.MISC);
    }
}
