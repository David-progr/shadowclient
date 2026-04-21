package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.BooleanSetting;

public class CoordsHUDFeature extends Feature {
    public final BooleanSetting showDim    = addSetting(new BooleanSetting("Show Dimension","Show current dimension",true));
    public final BooleanSetting showDir    = addSetting(new BooleanSetting("Show Direction","Show facing direction",true));
    public final BooleanSetting showBiome  = addSetting(new BooleanSetting("Show Biome","Show current biome",false));

    public CoordsHUDFeature() {
        super("Coords HUD", "Shows X/Y/Z and extra info on screen", Category.VISUAL);
    }
}
