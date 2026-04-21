package com.davidprogr.shadowclient.feature.setting;

public class SliderSetting extends Setting<Double> {
    private final double min, max, step;
    private final String unit;

    public SliderSetting(String name, String desc, double def, double min, double max, double step) {
        this(name, desc, def, min, max, step, "");
    }

    public SliderSetting(String name, String desc, double def, double min, double max, double step, String unit) {
        super(name, desc, def);
        this.min = min; this.max = max; this.step = step; this.unit = unit;
    }

    public double getMin()  { return min; }
    public double getMax()  { return max; }
    public double getStep() { return step; }
    public String getUnit() { return unit; }

    @Override
    public void setValue(Double v) {
        double snapped = Math.round(v / step) * step;
        this.value = Math.max(min, Math.min(max, snapped));
    }

    public String getDisplayValue() {
        if (step >= 1.0) return String.valueOf(value.intValue()) + unit;
        return String.format("%.2f", value) + unit;
    }

    /** True when step >= 1 and value is a whole number — used by GUI to display as int. */
    public boolean isInt() { return step >= 1.0; }

    @Override public Object toJson()           { return value; }
    @Override public void   fromJson(Object o) { if (o instanceof Number n) setValue(n.doubleValue()); }
}
