package com.davidprogr.shadowclient.feature;

import com.davidprogr.shadowclient.feature.utility.*;
import com.davidprogr.shadowclient.feature.visual.*;

import java.util.ArrayList;
import java.util.List;

public class FeatureManager {

    public static final List<Feature> FEATURES = new ArrayList<>();

    // Visual
    public static final FullbrightFeature FULLBRIGHT   = new FullbrightFeature();
    public static final ESPFeature        ESP          = new ESPFeature();
    public static final TracersFeature    TRACERS      = new TracersFeature();
    public static final CoordsHUDFeature  COORDS       = new CoordsHUDFeature();

    // Movement / Utility
    public static final ToggleSprintFeature TOGGLE_SPRINT = new ToggleSprintFeature();
    public static final NoFallFeature       NO_FALL       = new NoFallFeature();
    public static final ZoomFeature         ZOOM          = new ZoomFeature();

    public static void init() {
        FEATURES.add(FULLBRIGHT);
        FEATURES.add(ESP);
        FEATURES.add(TRACERS);
        FEATURES.add(COORDS);
        FEATURES.add(TOGGLE_SPRINT);
        FEATURES.add(NO_FALL);
        FEATURES.add(ZOOM);
    }

    public static List<Feature> getByCategory(Feature.Category cat) {
        return FEATURES.stream().filter(f -> f.getCategory() == cat).toList();
    }
}
