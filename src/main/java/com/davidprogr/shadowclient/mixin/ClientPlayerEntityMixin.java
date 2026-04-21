package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureManager;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    // ---- No Fall: reset fall distance every tick ----
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
        if (FeatureManager.NO_FALL.isEnabled()) {
            player.fallDistance = 0f;
        }
    }
}
