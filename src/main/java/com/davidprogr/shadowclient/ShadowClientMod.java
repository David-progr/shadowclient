package com.davidprogr.shadowclient;

import com.davidprogr.shadowclient.config.ConfigManager;
import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.FeatureRegistry;
import com.davidprogr.shadowclient.feature.setting.KeybindSetting;
import com.davidprogr.shadowclient.gui.ClickGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shadow Client — Hack + Legit edition.
 * Registers ALL features (including hack-only ones).
 */
public class ShadowClientMod implements ClientModInitializer {

    public static final String MOD_ID = "shadowclient";
    public static final String NAME   = "Shadow Client";
    public static final String VERSION = "2.0.0";
    public static final Logger LOGGER  = LoggerFactory.getLogger(MOD_ID);

    // Open GUI keybind — default R
    private static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing v{}", NAME, VERSION);

        // Init registry with ALL features
        FeatureRegistry.init(false);

        // Load saved config
        ConfigManager.load();

        // Register GUI keybind
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.shadowclient.gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "Shadow Client"
        ));

        // Tick event — runs each features onTick + checks keybinds
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Open ClickGUI
            if (guiKey.wasPressed()) {
                client.setScreen(new ClickGUI());
            }

            // Per-feature tick + keybind toggle
            for (Feature feature : FeatureRegistry.get().all()) {
                // Check toggle keybind
                KeybindSetting kb = feature.getKeybind();
                if (kb != null && kb.getValue() != -1) {
                    if (InputUtil.isKeyPressed(
                            MinecraftClient.getInstance().getWindow().getHandle(),
                            kb.getValue())) {
                        // Only trigger on key-press edge (not hold)
                        // Use the KeybindSetting's wasPressed tracking
                        if (!kb.isListening()) {
                            kb.setListening(true);
                            feature.toggle();
                        }
                    } else {
                        kb.setListening(false);
                    }
                }

                // Feature tick
                if (feature.isEnabled()) {
                    try {
                        feature.onTick();
                    } catch (Exception e) {
                        LOGGER.warn("[{}] Exception in {}.onTick(): {}",
                            NAME, feature.getName(), e.getMessage());
                    }
                }
            }
        });

        // Save config on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { ConfigManager.save(); } catch (Exception ignored) {}
        }));

        LOGGER.info("[{}] {} features loaded.", NAME, FeatureRegistry.get().all().size());
    }
}
