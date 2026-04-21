package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;

/** Legit-allowed. */
public class ProjectileTrajectoryFeature extends Feature {
    public final BooleanSetting arrows   = addSetting(new BooleanSetting("Arrows","Show arrow arc",true));
    public final BooleanSetting pearls   = addSetting(new BooleanSetting("Ender Pearls","Show pearl arc",true));
    public final BooleanSetting potions  = addSetting(new BooleanSetting("Potions","Show thrown potion arc",true));
    public final SliderSetting  steps    = addSetting(new SliderSetting("Steps","Accuracy (more=slower)",60,20,200,10));
    public final ModeSetting    style    = addSetting(new ModeSetting("Style","Visual",new String[]{"Line","Dots","Both"},"Line"));
    public final SliderSetting  thickness= addSetting(new SliderSetting("Thickness","Arc thickness",1.5,0.5,3.0,0.5));
    public ProjectileTrajectoryFeature() { super("Trajectories","Show projectile flight paths", Category.VISUAL, true); }
}
