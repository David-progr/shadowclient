package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;

/** Shadow-only. */
public class TracersFeature extends Feature {
    public final BooleanSetting players     = addSetting(new BooleanSetting("Players","Trace to players",true));
    public final BooleanSetting mobs        = addSetting(new BooleanSetting("Mobs","Trace to mobs",false));
    public final BooleanSetting animals     = addSetting(new BooleanSetting("Animals","Trace to animals",false));
    public final ModeSetting    origin      = addSetting(new ModeSetting("Origin","Line start",new String[]{"Crosshair","Eyes","Bottom"},"Crosshair"));
    public final SliderSetting  width       = addSetting(new SliderSetting("Width","Line thickness",1.0,0.5,3.0,0.5));
    public final ColorSetting   playerColor = addSetting(new ColorSetting("Player Color","Line color for players",0xFF4FC3F7));
    public final ColorSetting   mobColor    = addSetting(new ColorSetting("Mob Color","Line color for mobs",0xFFF44336));
    public TracersFeature() { super("Tracers","Draws lines to entities", Category.VISUAL, false); }
}
