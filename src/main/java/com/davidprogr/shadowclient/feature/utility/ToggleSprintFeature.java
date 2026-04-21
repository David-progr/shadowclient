package com.davidprogr.shadowclient.feature.utility;

import com.davidprogr.shadowclient.feature.Feature;

public class ToggleSprintFeature extends Feature {

    public ToggleSprintFeature() {
        super("Toggle Sprint", "Always sprints without holding Ctrl", Category.MOVEMENT);
    }

    // Logic handled in KeyboardInputMixin
}
