package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;

/** Legit-allowed. jumpingCooldown overridden in mixin. */
public class NoJumpDelayFeature extends Feature {
    public NoJumpDelayFeature() { super("NoJumpDelay","Removes the jump cooldown entirely", Category.MOVEMENT, true); }
}
