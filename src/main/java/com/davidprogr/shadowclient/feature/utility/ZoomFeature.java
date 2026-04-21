package com.davidprogr.shadowclient.feature.utility;

import com.davidprogr.shadowclient.feature.Feature;

public class ZoomFeature extends Feature {

    public ZoomFeature() {
        super("Zoom", "Hold V to zoom in like a spyglass", Category.UTILITY);
    }

    // FOV override handled in GameRendererMixin
}
