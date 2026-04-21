package com.davidprogr.shadowclient.feature;

import com.davidprogr.shadowclient.feature.combat.*;
import com.davidprogr.shadowclient.feature.cosmetic.*;
import com.davidprogr.shadowclient.feature.misc.*;
import com.davidprogr.shadowclient.feature.combat.BowAimbotFeature;
import com.davidprogr.shadowclient.feature.movement.ScaffoldFeature;
import com.davidprogr.shadowclient.feature.movement.VelocityFeature;
import com.davidprogr.shadowclient.feature.movement.SprintResetFeature;
import com.davidprogr.shadowclient.feature.visual.AntiBlindFeature;
import com.davidprogr.shadowclient.feature.utility.InventorySorterFeature;
import com.davidprogr.shadowclient.feature.misc.TimerFeature;
import com.davidprogr.shadowclient.feature.visual.OreSimulatorFeature;
import com.davidprogr.shadowclient.feature.misc.SeedCrackerFeature;

import com.davidprogr.shadowclient.feature.movement.*;
import com.davidprogr.shadowclient.feature.sound.*;
import com.davidprogr.shadowclient.feature.utility.*;
import com.davidprogr.shadowclient.feature.visual.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central registry. ShadowClientMod registers ALL features.
 * LightClientMod only registers those with legitAllowed = true.
 */
public class FeatureRegistry {

    private static FeatureRegistry INSTANCE;

    // ── Combat ──────────────────────────────────────────────────
    public final KillAuraFeature killAura = new KillAuraFeature();
    public final CriticalHitFeature criticals = new CriticalHitFeature();
    public final ReachFeature reach = new ReachFeature();
    public final AutoArmorFeature autoArmor = new AutoArmorFeature();
    public final AimAssistFeature aimAssist = new AimAssistFeature();
    public final ChestAuraFeature chestAura = new ChestAuraFeature();
    public final AutoEatFeature autoEat = new AutoEatFeature();
    public final AutoPotFeature autoPot = new AutoPotFeature();
    public final AutoExtinguishFeature autoExtinguish = new AutoExtinguishFeature();

    // ── Movement ─────────────────────────────────────────────────
    public final FlightFeature flight = new FlightFeature();
    public final SpeedFeature speed = new SpeedFeature();
    public final AntiKnockbackFeature antiKnockback = new AntiKnockbackFeature();
    public final NoFallFeature noFall = new NoFallFeature();
    public final SafeWalkFeature safeWalk = new SafeWalkFeature();
    public final ToggleSprintFeature toggleSprint = new ToggleSprintFeature();
    public final FreeCamFeature freeCam = new FreeCamFeature();
    public final FastPlaceFeature fastPlace = new FastPlaceFeature();
    public final NoJumpDelayFeature noJumpDelay = new NoJumpDelayFeature();
    public final TreeAuraFeature treeAura = new TreeAuraFeature();

    // ── Visual ───────────────────────────────────────────────────
    public final FullbrightFeature fullbright = new FullbrightFeature();
    public final ZoomFeature zoom = new ZoomFeature();
    public final ESPFeature esp = new ESPFeature();
    public final TracersFeature tracers = new TracersFeature();
    public final XRayFeature xray = new XRayFeature();
    public final NoFogFeature noFog = new NoFogFeature();
    public final NoHurtCamFeature noHurtCam = new NoHurtCamFeature();
    public final NametagsFeature nametags = new NametagsFeature();
    public final ProjectileTrajectoryFeature trajectories = new ProjectileTrajectoryFeature();
    public final HUDFeature hud = new HUDFeature();
    public final CoordsHUDFeature coordsHud = new CoordsHUDFeature();

    // ── Sound ─────────────────────────────────────────────────────
    public final CustomSoundsFeature customSounds = new CustomSoundsFeature();

    // ── Cosmetic ──────────────────────────────────────────────────
    public final CustomCrosshairFeature customCrosshair = new CustomCrosshairFeature();
    public final HitColorFeature hitColor = new HitColorFeature();
    public final ParticleEffectsFeature particles = new ParticleEffectsFeature();

    // ── Utility ───────────────────────────────────────────────────
    public final AutoReconnectFeature autoReconnect = new AutoReconnectFeature();
    public final AntiAFKFeature antiAfk = new AntiAFKFeature();
    public final ChatPrefixFeature chatPrefix = new ChatPrefixFeature();

    // ── Misc ──────────────────────────────────────────────────────
    public final WatermarkFeature watermark = new WatermarkFeature();
    public final HitmarkerFeature hitmarker = new HitmarkerFeature();
    public final AutoFarmFeature autoFarm = new AutoFarmFeature();
    public final NewChunksFeature newChunks = new NewChunksFeature();
    public final OverlayModeFeature overlayMode = new OverlayModeFeature();
    public final ScreenshareBypassFeature screenshareBypass = new ScreenshareBypassFeature();

    // ── Extra movement ───────────────────────────────────────────
    public final ScaffoldFeature      scaffold      = new ScaffoldFeature();
    public final VelocityFeature      velocity      = new VelocityFeature();
    public final SprintResetFeature   sprintReset   = new SprintResetFeature();

    // ── Extra combat ─────────────────────────────────────────────
    public final BowAimbotFeature     bowAimbot     = new BowAimbotFeature();

    // ── Extra visual ─────────────────────────────────────────────
    public final AntiBlindFeature     antiBlind     = new AntiBlindFeature();

    // ── Extra utility ────────────────────────────────────────────
    public final InventorySorterFeature invSorter   = new InventorySorterFeature();

    // ── Extra misc ───────────────────────────────────────────────
    public final TimerFeature         timer         = new TimerFeature();

    // ── New features ─────────────────────────────────────────────
    public final OreSimulatorFeature  oreSimulator  = new OreSimulatorFeature();
    public final SeedCrackerFeature   seedCracker   = new SeedCrackerFeature();

    // ── Internal list ────────────────────────────────────────────
    private final List<Feature> all = new ArrayList<>();

    private FeatureRegistry(boolean legitOnly) {
        // Add all fields that are Features
        add(killAura, criticals, reach, autoArmor, aimAssist, chestAura,
            autoEat, autoPot, autoExtinguish);
        add(flight, speed, antiKnockback, noFall, safeWalk, toggleSprint,
            freeCam, fastPlace, noJumpDelay, treeAura);
        add(fullbright, zoom, esp, tracers, xray, noFog, noHurtCam,
            nametags, trajectories, hud, coordsHud);
        add(customSounds);
        add(customCrosshair, hitColor, particles);
        add(autoReconnect, antiAfk, chatPrefix);
        add(watermark, hitmarker, autoFarm, newChunks, overlayMode, screenshareBypass, timer);
        add(scaffold, velocity, sprintReset);
        add(bowAimbot);
        add(antiBlind);
        add(invSorter);
        add(oreSimulator);
        add(seedCracker);

        if (legitOnly) {
            all.removeIf(f -> !f.isLegitAllowed());
        }
    }

    private void add(Feature... features) {
        for (Feature f : features) all.add(f);
    }

    /** Returns an unmodifiable view of all registered features. */
    public List<Feature> all() {
        return Collections.unmodifiableList(all);
    }

    /** Returns features in a given category. */
    public List<Feature> byCategory(Feature.Category category) {
        List<Feature> result = new ArrayList<>();
        for (Feature f : all) {
            if (f.getCategory() == category) result.add(f);
        }
        return result;
    }

    /** Find a feature by name (case-insensitive). */
    public Feature find(String name) {
        for (Feature f : all) {
            if (f.getName().equalsIgnoreCase(name)) return f;
        }
        return null;
    }

    // ── Singleton ────────────────────────────────────────────────

    public static void init(boolean legitOnly) {
        if (INSTANCE == null) {
            INSTANCE = new FeatureRegistry(legitOnly);
        }
    }

    public static FeatureRegistry get() {
        if (INSTANCE == null) throw new IllegalStateException("FeatureRegistry not initialized!");
        return INSTANCE;
    }
}
