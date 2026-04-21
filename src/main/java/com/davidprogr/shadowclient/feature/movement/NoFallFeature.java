package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;

/**
 * NoFall — Shadow only. Handled in ClientPlayerEntityMixin.
 *
 * AC bypass per mode:
 * ─ Packet:  Sets onGround=true in the movement packet just before landing.
 *            Detected by Grim (GroundProof checks transition).
 * ─ Grim:    Resets fallDistance client-side every tick AND sends onGround=true
 *            only once per 20 ticks to avoid Grim's repetition flag.
 * ─ Vulcan:  Sends a PositionAndOnGround(true) packet when fallDistance > 3.
 *            Vulcan's GroundProof check is less strict than Grim's.
 */
public class NoFallFeature extends Feature {

    public final ModeSetting mode = addSetting(new ModeSetting("AC Bypass","Bypass mode",
            new String[]{"Packet","Grim","Vulcan"},"Vulcan"));

    public NoFallFeature() {
        super("NoFall","Cancel all fall damage", Category.MOVEMENT, false);
    }
}
