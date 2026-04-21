package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;

/** Shadow-only. Reloads world renderer on toggle. */
public class XRayFeature extends Feature {
    public final BooleanSetting diamonds  = addSetting(new BooleanSetting("Diamonds","Show diamond ore",true));
    public final BooleanSetting ancient   = addSetting(new BooleanSetting("Ancient Debris","Show ancient debris",true));
    public final BooleanSetting emeralds  = addSetting(new BooleanSetting("Emeralds","Show emerald ore",true));
    public final BooleanSetting gold      = addSetting(new BooleanSetting("Gold","Show gold ore",true));
    public final BooleanSetting iron      = addSetting(new BooleanSetting("Iron","Show iron ore",false));
    public final BooleanSetting redstone  = addSetting(new BooleanSetting("Redstone","Show redstone ore",false));
    public final BooleanSetting chests    = addSetting(new BooleanSetting("Chests","Show chests",true));

    public XRayFeature() { super("XRay","See ores through walls", Category.VISUAL, false); }

    private void reload() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.worldRenderer != null) mc.worldRenderer.reload();
    }
    @Override public void onEnable()  { reload(); }
    @Override public void onDisable() { reload(); }
}
