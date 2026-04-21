package com.davidprogr.shadowclient.feature.visual;

import com.davidprogr.shadowclient.feature.Feature;

public class FullbrightFeature extends Feature {

    public FullbrightFeature() {
        super("Fullbright", "Makes everything fully visible in the dark", Category.VISUAL);
    }

    // Actual gamma override is done in GameRendererMixin
}
