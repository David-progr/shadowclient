package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;

public class TracersFeature extends Feature {

    public TracersFeature() {
        super("Tracers", "Draws lines from crosshair to nearby entities", Category.VISUAL);
    }

    // Rendering handled in ESPRenderer
}
