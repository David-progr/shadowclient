package com.davidprogr.shadowclient.feature.sound;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

/**
 * CustomSounds — Legit-allowed.
 * Replaces vanilla hit, crit, kill, pot-drink sounds with PvP-themed alternatives.
 *
 * Themes:
 *  Default — vanilla (no override)
 *  OldPvP  — Minecraft 1.8-style sounds
 *  Tap     — Click-style thin hit (KitPvP)
 *  Bass    — Deep punchy bass hit + reverb crit
 *  Krunker — FPS-inspired hit tick
 */
public class CustomSoundsFeature extends Feature {

    public final ModeSetting    theme      = addSetting(new ModeSetting("Theme","Sound theme",
            new String[]{"Default","OldPvP","Tap","Bass","Krunker"},"OldPvP"));
    public final SliderSetting  hitVol     = addSetting(new SliderSetting("Hit Volume","Hit sound volume",1.0,0.0,2.0,0.1));
    public final SliderSetting  critVol    = addSetting(new SliderSetting("Crit Volume","Crit sound volume",1.0,0.0,2.0,0.1));
    public final SliderSetting  killVol    = addSetting(new SliderSetting("Kill Volume","Kill sound volume",1.0,0.0,2.0,0.1));
    public final SliderSetting  potVol     = addSetting(new SliderSetting("Pot Volume","Potion sound volume",1.0,0.0,2.0,0.1));
    public final SliderSetting  gappleVol  = addSetting(new SliderSetting("Gapple Volume","Gapple eat volume",1.0,0.0,2.0,0.1));
    public final BooleanSetting playCrits  = addSetting(new BooleanSetting("Crit Sound","Play crit sound",true));
    public final BooleanSetting playKill   = addSetting(new BooleanSetting("Kill Sound","Play kill sound",true));
    public final BooleanSetting playPot    = addSetting(new BooleanSetting("Pot Sound","Play pot sound",true));

    public CustomSoundsFeature() {
        super("CustomSounds","PvP-themed sound replacements", Category.SOUND, true);
    }

    /** Called from ClientPlayerEntityMixin when player damages an entity. */
    public void onHit(boolean isCrit, boolean killed) {
        if (!isEnabled() || "Default".equals(theme.getValue())) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        String t = theme.getValue().toLowerCase();
        if (killed && playKill.getValue()) {
            playSound(mc, "shadowclient:sounds/" + t + "/kill", (float)(double) killVol.getValue());
        } else if (isCrit && playCrits.getValue()) {
            playSound(mc, "shadowclient:sounds/" + t + "/crit", (float)(double) critVol.getValue());
        } else {
            playSound(mc, "shadowclient:sounds/" + t + "/hit", (float)(double) hitVol.getValue());
        }
    }

    /** Called from ClientPlayerEntityMixin when player drinks/throws a potion. */
    public void onPotionUse() {
        if (!isEnabled() || !playPot.getValue() || "Default".equals(theme.getValue())) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        playSound(mc, "shadowclient:sounds/" + theme.getValue().toLowerCase() + "/pot",
                (float)(double) potVol.getValue());
    }

    private void playSound(MinecraftClient mc, String idStr, float volume) {
        try {
            Identifier id = Identifier.of(idStr);
            // Look up the registered SoundEvent; fall back to a vanilla sound if missing
            SoundEvent event = Registries.SOUND_EVENT.get(id);
            if (event == null) {
                // Fallback: use experience orb pickup as a thin click-like sound
                event = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            }
            // World.playSound(player, blockPos, SoundEvent, category, volume, pitch)
            mc.world.playSound(mc.player, mc.player.getBlockPos(), event,
                    SoundCategory.PLAYERS, volume, 1.0f);
        } catch (Exception ignored) {
            // Never crash if a sound file is missing
        }
    }
}
