package com.davidprogr.shadowclient.mixin;

import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.combat.CriticalHitFeature;
import com.davidprogr.shadowclient.feature.cosmetic.ParticleEffectsFeature;
import com.davidprogr.shadowclient.feature.misc.HitmarkerFeature;
import com.davidprogr.shadowclient.feature.sound.CustomSoundsFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into ClientPlayerInteractionManager.attackEntity to apply:
 *  - CriticalHit  : spoof jump packets before damage
 *  - CustomSounds : play themed hit / crit / kill sound
 *  - Particles    : burst on hit / kill
 *  - Hitmarker    : trigger display timer
 */
@Mixin(ClientPlayerInteractionManager.class)
public class PlayerInteractionMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null) return;
            FeatureRegistry reg = FeatureRegistry.get();

            // ── Criticals ────────────────────────────────────────────────
            CriticalHitFeature crit = reg.criticals;   // field is "criticals"
            if (crit != null && crit.isEnabled()) {
                crit.applyCrit(mc);
            }

            boolean killed = target instanceof LivingEntity le && le.getHealth() <= 1f;
            boolean isCrit = crit != null && crit.isEnabled() && !mc.player.isOnGround();

            // ── Custom Sounds ─────────────────────────────────────────────
            CustomSoundsFeature sounds = reg.customSounds;
            if (sounds != null) sounds.onHit(isCrit, killed);

            // ── Particle Effects ──────────────────────────────────────────
            ParticleEffectsFeature particles = reg.particles;  // field is "particles"
            if (particles != null) {
                if (killed) particles.onKill();
                else        particles.onHit();
            }

            // ── Hitmarker ─────────────────────────────────────────────────
            HitmarkerFeature hm = reg.hitmarker;
            if (hm != null && hm.isEnabled()) hm.onHit();

        } catch (Exception ignored) {}
    }
}
