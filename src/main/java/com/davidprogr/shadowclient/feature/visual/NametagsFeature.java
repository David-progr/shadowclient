package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;

/** Legit-allowed. Enhanced player nametags. */
public class NametagsFeature extends Feature {
    public final SliderSetting  scale    = addSetting(new SliderSetting("Scale","Nametag size",1.5,0.5,5.0,0.1));
    public final BooleanSetting showHP   = addSetting(new BooleanSetting("Show HP","Display health",true));
    public final BooleanSetting showPing = addSetting(new BooleanSetting("Show Ping","Display ping",true));
    public final BooleanSetting showItem = addSetting(new BooleanSetting("Show Item","Show held item",true));
    public NametagsFeature() { super("Nametags","Better player nametags", Category.VISUAL, true); }
}
