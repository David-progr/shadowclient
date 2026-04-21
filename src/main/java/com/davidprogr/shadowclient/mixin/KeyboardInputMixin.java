package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.movement.SafeWalkFeature;
import com.davidprogr.shadowclient.feature.movement.ToggleSprintFeature;
import com.davidprogr.shadowclient.feature.movement.FastPlaceFeature;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null) return;

            Input input = (Input)(Object)this;
            FeatureRegistry reg = FeatureRegistry.get();

            // ── ToggleSprint: keep sprinting if moving forward ──
            ToggleSprintFeature ts = reg.toggleSprint;
            if (ts != null && ts.isEnabled()) {
                // movementForward > 0 means player is moving forward
                if (input.movementForward > 0) {
                    mc.player.setSprinting(true);
                }
            }

            // ── SafeWalk: force sneaking at edges ──
            SafeWalkFeature sw = reg.safeWalk;
            if (sw != null && sw.isEnabled()) {
                mc.player.setSneaking(true);
            }

            // ── FastPlace: reset item use cooldown via reflection ──
            FastPlaceFeature fp = reg.fastPlace;
            if (fp != null && fp.isEnabled()) {
                try {
                    var f = net.minecraft.entity.LivingEntity.class
                            .getDeclaredField("itemUseTimeLeft");
                    f.setAccessible(true);
                    f.setInt(mc.player, 0);
                } catch (Exception ignored) {}
            }

        } catch (Exception ignored) {}
    }
}
