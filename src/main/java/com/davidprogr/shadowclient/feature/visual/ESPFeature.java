package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;

public class ESPFeature extends Feature {

    public ESPFeature() {
        super("ESP", "Shows entity bounding boxes through walls", Category.VISUAL);
    }

    // Rendering handled in ESPRenderer + WorldRendererMixin
}
