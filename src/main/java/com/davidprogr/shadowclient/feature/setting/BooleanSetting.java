package com.davidprogr.shadowclient.feature.setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String desc, boolean def) {
        super(name, desc, def);
    }
    public void toggle() { value = !value; }
    @Override public Object toJson()           { return value; }
    @Override public void   fromJson(Object o) { if (o instanceof Boolean b) value = b; }
}
