package com.davidprogr.shadowclient.feature.setting;

public class ModeSetting extends Setting<String> {
    private final String[] modes;

    public ModeSetting(String name, String desc, String[] modes, String def) {
        super(name, desc, def);
        this.modes = modes;
    }

    public String[] getModes() { return modes; }

    public void cycle() {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(value)) {
                value = modes[(i + 1) % modes.length];
                return;
            }
        }
        value = modes[0];
    }

    public void cyclePrev() {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(value)) {
                value = modes[(i - 1 + modes.length) % modes.length];
                return;
            }
        }
        value = modes[modes.length - 1];
    }

    /** Alias for cycle() — used by GUI. */
    public void next()     { cycle(); }
    /** Alias for cyclePrev() — used by GUI. */
    public void previous() { cyclePrev(); }
    /** Set by string value (no-op if not a valid mode). */
    public void set(String v) {
        for (String m : modes) { if (m.equals(v)) { value = v; return; } }
    }

    @Override public Object toJson()           { return value; }
    @Override public void   fromJson(Object o) { if (o instanceof String s) set(s); }
}
