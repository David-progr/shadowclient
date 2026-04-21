package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;

/**
 * AntiKnockback — Shadow only. Applied via ClientPlayerEntityMixin.
 *
 * AC bypass per mode:
 * ─ Full:     Multiplies received knockback by (1 - strength). Simple, detected
 *             by Vulcan's Velocity check at strength > 0.5.
 * ─ Vulcan:   Reduces horizontal KB only (vertical kept intact). Vulcan's
 *             velocity check tracks vertical velocity to validate knockback;
 *             keeping y intact bypasses it. Max safe horizontal: ~0.4 cancel.
 * ─ Grim:     Grim checks expected velocity after hit using simulation. We accept
 *             the knockback fully for 1 tick then cancel on tick 2 (looks like
 *             natural deceleration to Grim's model). strength > 0.7 still flags.
 * ─ NCP:      Same as Full — NCP's velocity check is less precise.
 */
public class AntiKnockbackFeature extends Feature {

    public final ModeSetting   acMode   = addSetting(new ModeSetting("AC Bypass","Bypass mode",
            new String[]{"Full","Vulcan","Grim","NCP"},"Vulcan"));
    public final SliderSetting strength = addSetting(new SliderSetting("Strength",
            "How much to cancel (0=none, 1=full)",0.6,0.0,1.0,0.05));

    // Grim mode: delay counter
    private int grimDelay = 0;

    public AntiKnockbackFeature() {
        super("AntiKnockback","Reduce knockback received", Category.MOVEMENT, false);
    }

    /**
     * Called from ClientPlayerEntityMixin.takeKnockback.
     * Returns the velocity multiplier to apply to the incoming knockback vector.
     * x/z components multiplied by this value. y untouched for Vulcan mode.
     */
    public float getHorizontalMultiplier() {
        if (!isEnabled()) return 1.0f;
        switch (acMode.getValue()) {
            case "Full", "NCP" -> { return 1.0f - (float)(double) strength.getValue(); }
            case "Vulcan"      -> { return 1.0f - (float) Math.min(strength.getValue(), 0.4); }
            case "Grim"        -> {
                // Accept first tick, cancel second
                if (grimDelay == 0) { grimDelay = 1; return 1.0f; }
                grimDelay = 0;
                return 1.0f - (float) Math.min(strength.getValue(), 0.7);
            }
            default -> { return 1.0f; }
        }
    }

    /** Returns whether y-axis knockback should be preserved (Vulcan bypass). */
    public boolean keepVertical() {
        return isEnabled() && "Vulcan".equals(acMode.getValue());
    }
}
