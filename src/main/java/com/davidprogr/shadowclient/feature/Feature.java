package com.davidprogr.shadowclient.feature;

public abstract class Feature {

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;

    public Feature(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
    }

    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) onEnable();
        else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public Category getCategory()  { return category; }
    public boolean isEnabled()     { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }

    public enum Category {
        VISUAL("Visual"),
        UTILITY("Utility"),
        MOVEMENT("Movement");

        public final String displayName;
        Category(String displayName) { this.displayName = displayName; }
    }
}
