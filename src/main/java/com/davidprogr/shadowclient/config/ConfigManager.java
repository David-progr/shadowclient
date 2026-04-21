package com.davidprogr.shadowclient.config;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.setting.*;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

/**
 * Saves and loads all feature states + settings to/from:
 *   .minecraft/shadowclient/config.json
 *
 * Format:
 * {
 *   "KillAura": { "enabled": true, "settings": { "Mode": "Grim", "Range": 3.5 } },
 *   "Zoom":     { "enabled": false, "settings": { "Zoom Level": 4.0 } },
 *   ...
 * }
 */
public class ConfigManager {

    private static final String CONFIG_DIR  = "shadowclient";
    private static final String CONFIG_FILE = "config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path getConfigPath() {
        return FabricLoader.getInstance()
            .getGameDir()
            .resolve(CONFIG_DIR)
            .resolve(CONFIG_FILE);
    }

    // ─────────────────────────────────────────────────────────────
    //  Save
    // ─────────────────────────────────────────────────────────────

    public static void save() {
        try {
            Path path = getConfigPath();
            Files.createDirectories(path.getParent());

            JsonObject root = new JsonObject();

            for (Feature feature : FeatureRegistry.get().all()) {
                JsonObject featureObj = new JsonObject();
                featureObj.addProperty("enabled", feature.isEnabled());

                JsonObject settingsObj = new JsonObject();
                for (Setting<?> setting : feature.getSettings()) {
                    serializeSetting(settingsObj, setting);
                }
                featureObj.add("settings", settingsObj);
                root.add(feature.getName(), featureObj);
            }

            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            System.err.println("[ShadowClient] Failed to save config: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Load
    // ─────────────────────────────────────────────────────────────

    public static void load() {
        try {
            Path path = getConfigPath();
            if (!Files.exists(path)) return;

            JsonObject root;
            try (Reader r = Files.newBufferedReader(path)) {
                root = JsonParser.parseReader(r).getAsJsonObject();
            }

            for (Feature feature : FeatureRegistry.get().all()) {
                if (!root.has(feature.getName())) continue;
                JsonObject featureObj = root.getAsJsonObject(feature.getName());

                // Enabled state
                if (featureObj.has("enabled")) {
                    boolean enabled = featureObj.get("enabled").getAsBoolean();
                    if (enabled != feature.isEnabled()) feature.toggle();
                }

                // Settings
                if (featureObj.has("settings")) {
                    JsonObject settingsObj = featureObj.getAsJsonObject("settings");
                    for (Setting<?> setting : feature.getSettings()) {
                        deserializeSetting(settingsObj, setting);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ShadowClient] Failed to load config: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Serialization helpers
    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static void serializeSetting(JsonObject obj, Setting<?> setting) {
        String key = setting.getName();
        if (setting instanceof BooleanSetting bs) {
            obj.addProperty(key, bs.getValue());
        } else if (setting instanceof SliderSetting ss) {
            obj.addProperty(key, ss.getValue());
        } else if (setting instanceof ModeSetting ms) {
            obj.addProperty(key, ms.getValue());
        } else if (setting instanceof KeybindSetting kb) {
            obj.addProperty(key, kb.getValue());
        } else if (setting instanceof ColorSetting cs) {
            obj.addProperty(key, cs.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static void deserializeSetting(JsonObject obj, Setting<?> setting) {
        String key = setting.getName();
        if (!obj.has(key)) return;
        try {
            JsonElement el = obj.get(key);
            if (setting instanceof BooleanSetting bs) {
                bs.setValue(el.getAsBoolean());
            } else if (setting instanceof SliderSetting ss) {
                ss.setValue(el.getAsDouble());
            } else if (setting instanceof ModeSetting ms) {
                ms.set(el.getAsString());
            } else if (setting instanceof KeybindSetting kb) {
                kb.setValue(el.getAsInt());
            } else if (setting instanceof ColorSetting cs) {
                cs.setValue(el.getAsInt());
            }
        } catch (Exception ignored) {}
    }
}
