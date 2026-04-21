package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;

/** Shadow-only. Handled by KeyboardInputMixin. */
public class SafeWalkFeature extends Feature {
    public SafeWalkFeature() { super("SafeWalk","Prevent walking off edges", Category.MOVEMENT, false); }
}
