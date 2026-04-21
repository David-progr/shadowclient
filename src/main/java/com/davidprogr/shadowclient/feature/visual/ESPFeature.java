package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;

/** Shadow-only. */
public class ESPFeature extends Feature {
    public final BooleanSetting players   = addSetting(new BooleanSetting("Players","Highlight players",true));
    public final BooleanSetting mobs      = addSetting(new BooleanSetting("Mobs","Highlight hostile mobs",true));
    public final BooleanSetting animals   = addSetting(new BooleanSetting("Animals","Highlight animals",false));
    public final BooleanSetting items     = addSetting(new BooleanSetting("Items","Highlight dropped items",false));
    public final ModeSetting    mode      = addSetting(new ModeSetting("Mode","Box style",new String[]{"Box","Outline","Corners","2D"},"Box"));
    public final BooleanSetting fill      = addSetting(new BooleanSetting("Fill","Fill transparent",true));
    public final SliderSetting  fillAlpha = addSetting(new SliderSetting("Fill Alpha","Box transparency",0.2,0.02,0.8,0.02));
    public final SliderSetting  lineWidth = addSetting(new SliderSetting("Line Width","Outline thickness",1.5,0.5,4.0,0.5));
    public final ColorSetting   colorP    = addSetting(new ColorSetting("Player Color","Player box color",0xCC4FC3F7));
    public final ColorSetting   colorM    = addSetting(new ColorSetting("Mob Color","Mob box color",0xCCF44336));
    public ESPFeature() { super("ESP","See entities through walls", Category.VISUAL, false); }
}
