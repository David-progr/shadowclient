package com.davidprogr.shadowclient;

import com.davidprogr.shadowclient.feature.FeatureManager;
import com.davidprogr.shadowclient.gui.ClickGUI;
import com.davidprogr.shadowclient.render.HUDRenderer;
import com.davidprogr.shadowclient.render.ESPRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.lwjgl.glfw.GLFW;

public class ShadowClientMod implements ClientModInitializer {

    public static KeyBinding OPEN_GUI_KEY;
    public static KeyBinding ZOOM_KEY;

    @Override
    public void onInitializeClient() {
        // Init all features
        FeatureManager.init();

        // Register keybinds
        OPEN_GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowclient.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.category.shadowclient.general"
        ));

        ZOOM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowclient.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.category.shadowclient.general"
        ));

        // Tick: open GUI on key press, hold zoom
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI_KEY.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGUI(null));
                }
            }
            FeatureManager.ZOOM.setEnabled(ZOOM_KEY.isPressed());
        });

        // HUD rendering
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            HUDRenderer.render(drawContext, net.minecraft.client.MinecraftClient.getInstance());
        });

        // World / ESP rendering
        ESPRenderer.register();
    }
}
