package com.davidprogr.shadowclient.feature;

import com.davidprogr.shadowclient.feature.setting.*;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public abstract class Feature {

    private final String   name;
    private final String   description;
    private final Category category;
    private boolean        enabled;

    // Whether this feature is available in the legit (Light) build
    private final boolean  legitAllowed;

    public final KeybindSetting keybind;
    private final List<Setting<?>> settings = new ArrayList<>();

    // For HUD elements: draggable screen position
    private int hudX = 10, hudY = 10;

    // ── Constructors ──────────────────────────────────────────────

    public Feature(String name, String description, Category category,
                   int defaultKey, boolean legitAllowed) {
        this.name         = name;
        this.description  = description;
        this.category     = category;
        this.enabled      = false;
        this.legitAllowed = legitAllowed;
        this.keybind      = new KeybindSetting("Keybind", "Toggle key for this module", defaultKey);
        settings.add(keybind);
    }

    public Feature(String name, String description, Category category, boolean legitAllowed) {
        this(name, description, category, GLFW.GLFW_KEY_UNKNOWN, legitAllowed);
    }

    /** Hack-only feature (not available in Light client). */
    public Feature(String name, String description, Category category) {
        this(name, description, category, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    // ── Settings registration ─────────────────────────────────────

    protected <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    // ── Lifecycle ─────────────────────────────────────────────────

    public final void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) onEnable();
        else onDisable();
    }

    public void onEnable()  {}
    public void onDisable() {}
    public void onTick()    {}

    // ── Accessors ─────────────────────────────────────────────────

    public String   getName()              { return name; }
    public String   getDescription()       { return description; }
    public Category getCategory()          { return category; }
    public boolean  isEnabled()            { return enabled; }
    public boolean  isLegitAllowed()       { return legitAllowed; }

    public void setEnabled(boolean v) {
        if (v != this.enabled) toggle();
    }

    public List<Setting<?>> getSettings() { return settings; }
    public KeybindSetting   getKeybind()  { return keybind; }

    public int  getHudX()      { return hudX; }
    public int  getHudY()      { return hudY; }
    public void setHudX(int x) { this.hudX = x; }
    public void setHudY(int y) { this.hudY = y; }

    // ── Category enum ─────────────────────────────────────────────

    public enum Category {
        COMBAT   ("Combat",    0xFFE53935),
        MOVEMENT ("Movement",  0xFF43A047),
        VISUAL   ("Visual",    0xFF1E88E5),
        UTILITY  ("Utility",   0xFF8E24AA),
        SOUND    ("Sound",     0xFFFF6F00),
        COSMETIC ("Cosmetic",  0xFFE91E8C),
        MISC     ("Misc",      0xFFFB8C00);

        public final String displayName;
        public final int    color;
        Category(String n, int c) { displayName = n; color = c; }
    }
}
