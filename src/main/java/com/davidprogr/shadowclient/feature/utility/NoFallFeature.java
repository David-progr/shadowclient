package com.davidprogr.shadowclient.feature.utility;

import com.davidprogr.shadowclient.feature.Feature;

public class NoFallFeature extends Feature {

    public NoFallFeature() {
        super("No Fall", "Cancels fall damage", Category.UTILITY);
    }

    // Logic handled in ClientPlayerEntityMixin
}
