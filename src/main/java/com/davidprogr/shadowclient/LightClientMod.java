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
 * Light Client — Legit-only edition.
 * Only registers features with legitAllowed = true (no hacks).
 * Uses cyan accent colour.
 */
public class LightClientMod implements ClientModInitializer {

    public static final String MOD_ID  = "lightclient";
    public static final String NAME    = "Light Client";
    public static final String VERSION = "2.0.0";
    public static final Logger LOGGER  = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing v{}", NAME, VERSION);

        // Cyan accent for Light client
        ClickGUI.ACCENT = 0xFF00FFFF;

        // Init registry with ONLY legit-allowed features
        FeatureRegistry.init(true);

        ConfigManager.load();

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.lightclient.gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "Light Client"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (guiKey.wasPressed()) {
                client.setScreen(new ClickGUI());
            }

            for (Feature feature : FeatureRegistry.get().all()) {
                KeybindSetting kb = feature.getKeybind();
                if (kb != null && kb.getValue() != -1) {
                    if (InputUtil.isKeyPressed(
                            MinecraftClient.getInstance().getWindow().getHandle(),
                            kb.getValue())) {
                        if (!kb.isListening()) {
                            kb.setListening(true);
                            feature.toggle();
                        }
                    } else {
                        kb.setListening(false);
                    }
                }

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { ConfigManager.save(); } catch (Exception ignored) {}
        }));

        LOGGER.info("[{}] {} legit features loaded.", NAME, FeatureRegistry.get().all().size());
    }
}
