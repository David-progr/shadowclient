package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.movement.NoFallFeature;
import com.davidprogr.shadowclient.feature.movement.AntiKnockbackFeature;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ClientPlayerEntityMixin {

    // ── AntiKnockback: cancel velocity change on damage ──
    // Targeting LivingEntity where takeKnockback(DDD)V is actually defined.
    @Inject(method = "takeKnockback(DDD)V", at = @At("HEAD"), cancellable = true)
    private void onTakeKnockback(double strength, double x, double z, CallbackInfo ci) {
        try {
            // Only apply to the local player
            if (!(((Object)this) instanceof ClientPlayerEntity)) return;
            AntiKnockbackFeature akb = FeatureRegistry.get().antiKnockback;
            if (akb == null || !akb.isEnabled()) return;
            String mode = akb.acMode.getValue();
            if ("Full".equals(mode)) {
                ci.cancel();
            }
            // Partial modes handled in AntiKnockbackFeature.onTick via velocity clamping
        } catch (Exception ignored) {}
    }
}
