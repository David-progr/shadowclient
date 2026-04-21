package com.davidprogr.shadowclient.feature.setting;

/**
 * ARGB color setting, stored as packed int (0xAARRGGBB).
 */
public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, String desc, int defaultArgb) {
        super(name, desc, defaultArgb);
    }

    public int getAlpha() { return (value >> 24) & 0xFF; }
    public int getRed()   { return (value >> 16) & 0xFF; }
    public int getGreen() { return (value >> 8)  & 0xFF; }
    public int getBlue()  { return  value        & 0xFF; }

    public void setRGBA(int r, int g, int b, int a) {
        value = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public String toHex() {
        return String.format("#%08X", value);
    }

    @Override public Object toJson()           { return value; }
    @Override public void   fromJson(Object o) { if (o instanceof Number n) value = n.intValue(); }
}
