package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;

/** Legit-allowed. Master HUD controller. All rendering in HUDRenderer. */
public class HUDFeature extends Feature {

    // Coordinates panel
    public final BooleanSetting showCoords    = addSetting(new BooleanSetting("Coordinates","XYZ position",true));
    public final BooleanSetting showDirection = addSetting(new BooleanSetting("Direction","Facing direction",true));
    public final BooleanSetting showDimension = addSetting(new BooleanSetting("Dimension","Current dimension",true));
    public final BooleanSetting showBiome     = addSetting(new BooleanSetting("Biome","Current biome",true));

    // Performance panel
    public final BooleanSetting showFPS       = addSetting(new BooleanSetting("FPS","Frames per second",true));
    public final BooleanSetting showPing      = addSetting(new BooleanSetting("Ping","Server ping ms",true));
    public final BooleanSetting showTPS       = addSetting(new BooleanSetting("TPS","Server TPS estimate",false));

    // Player status
    public final BooleanSetting showArmor     = addSetting(new BooleanSetting("Armor Status","Armor durability bars",true));
    public final BooleanSetting showSpeed     = addSetting(new BooleanSetting("Speed","Blocks per second",true));
    public final BooleanSetting showLightLevel= addSetting(new BooleanSetting("Light Level","Block light level",true));

    // Potion effects panel
    public final BooleanSetting showPotions   = addSetting(new BooleanSetting("Potion Effects","Active effects + timers",true));
    public final ModeSetting    potionLayout  = addSetting(new ModeSetting("Potion Layout","Display style",new String[]{"Vertical","Horizontal","Icons Only"},"Vertical"));

    // Target HUD (shows when attacking / looking at entity)
    public final BooleanSetting showTargetHUD = addSetting(new BooleanSetting("Target HUD","Show target HP + armor",true));
    public final BooleanSetting targetArmor   = addSetting(new BooleanSetting("Target Armor","Show target armor pieces",true));
    public final BooleanSetting targetDist    = addSetting(new BooleanSetting("Target Distance","Show distance to target",true));
    public final SliderSetting  targetFade    = addSetting(new SliderSetting("Target Fade","Seconds to hold after losing target",3,1,10,1));

    // World info
    public final BooleanSetting showTime      = addSetting(new BooleanSetting("World Time","In-game clock",true));
    public final BooleanSetting showWeather   = addSetting(new BooleanSetting("Weather","Current weather",true));

    // Module list (ArrayList)
    public final BooleanSetting showModList   = addSetting(new BooleanSetting("Module List","Enabled modules on screen",true));
    public final ModeSetting    modListPos    = addSetting(new ModeSetting("List Position","Corner",new String[]{"Top Right","Top Left","Bottom Right","Bottom Left"},"Top Right"));
    public final BooleanSetting modListColors = addSetting(new BooleanSetting("Category Colors","Color by category",true));

    // Sound Locator HUD element
    public final BooleanSetting showSoundLocator = addSetting(new BooleanSetting("Sound Locator","Show recent sound directions",false));

    // Styling
    public final ModeSetting    theme         = addSetting(new ModeSetting("Theme","Visual style",new String[]{"Shadow","Light","Neon","Minimal","Classic"},"Shadow"));
    public final SliderSetting  scale         = addSetting(new SliderSetting("Scale","HUD size",1.0,0.5,2.0,0.05));
    public final SliderSetting  bgAlpha       = addSetting(new SliderSetting("BG Alpha","Panel background opacity",0.5,0.0,1.0,0.05));
    public final BooleanSetting rounded       = addSetting(new BooleanSetting("Rounded","Rounded panel corners",true));
    public final BooleanSetting rainbow       = addSetting(new BooleanSetting("Rainbow","Rainbow accent color",false));

    public HUDFeature() { super("HUD","Rich heads-up display", Category.VISUAL, true); }
}
