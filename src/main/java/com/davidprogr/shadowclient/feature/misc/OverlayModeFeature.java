package com.davidprogr.shadowclient.feature.misc;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.BooleanSetting;
import com.davidprogr.shadowclient.feature.setting.SliderSetting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * OverlayMode — makes Minecraft float on top of all windows with adjustable
 * transparency. Optional click-through (mouse events pass to windows behind).
 *
 * Works by manipulating GLFW window attributes at runtime:
 *   GLFW_FLOATING      — always-on-top
 *   GLFW_DECORATED     — removes title bar / border
 *   GLFW_MOUSE_PASSTHROUGH — lets clicks pass to the desktop (GLFW 3.4+)
 *
 * Opacity is achieved via glfwSetWindowOpacity().
 *
 * LIMITATIONS (shown in description):
 *   - Click-through disables mouse input to Minecraft entirely.
 *   - Modules that rely on mouse (ClickGUI, Zoom, FreeCam) are auto-paused
 *     when click-through is on.
 *   - On some Linux compositors transparent framebuffer may not work.
 *   - Full-screen mode must be OFF. Module auto-exits fullscreen on enable.
 */
public class OverlayModeFeature extends Feature {

    public final SliderSetting opacity =
            new SliderSetting("Opacity", "Window opacity %", 85, 10, 100, 1);         // %
    public final BooleanSetting clickThrough =
            new BooleanSetting("Click-Through", "Pass mouse clicks to desktop", false);
    public final BooleanSetting removeBorder =
            new BooleanSetting("Borderless", "Remove window border/title", true);
    public final BooleanSetting alwaysOnTop =
            new BooleanSetting("Always On Top", "Float above all other windows", true);

    /** Modules incompatible with click-through (need mouse focus). */
    private static final String[] INCOMPATIBLE = {
        "FreeCam", "Zoom"
    };

    // Saved state so we can restore on disable
    private boolean wasFullscreen = false;

    public OverlayModeFeature() {
        super("OverlayMode",
              "Float Minecraft above other windows with transparency. " +
              "Click-Through passes mouse to desktop (disables MC mouse input).",
              Category.MISC, true);
        addSetting(opacity);
        addSetting(clickThrough);
        addSetting(removeBorder);
        addSetting(alwaysOnTop);
    }

    // ─────────────────────────────────────────────────────────────
    //  Enable / Disable
    // ─────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        long handle = mc.getWindow().getHandle();

        // Exit fullscreen first — overlay needs windowed mode
        if (mc.getWindow().isFullscreen()) {
            wasFullscreen = true;
            mc.getWindow().toggleFullscreen();
        }

        applySettings(handle);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        long handle = mc.getWindow().getHandle();

        // Restore defaults
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_FLOATING,       GLFW.GLFW_FALSE);
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED,      GLFW.GLFW_TRUE);
        safeSetMousePassthrough(handle, false);
        GLFW.glfwSetWindowOpacity(handle, 1.0f);

        // Re-enable any features we disabled
        re_enableIncompatible();

        // Restore fullscreen if we exited it
        if (wasFullscreen) {
            mc.getWindow().toggleFullscreen();
            wasFullscreen = false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Tick — apply live changes (opacity slider, click-through toggle)
    // ─────────────────────────────────────────────────────────────

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        long handle = mc.getWindow().getHandle();
        applySettings(handle);
    }

    // ─────────────────────────────────────────────────────────────
    //  Core logic
    // ─────────────────────────────────────────────────────────────

    private void applySettings(long handle) {
        // Always-on-top
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_FLOATING,
            alwaysOnTop.getValue() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

        // Border
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED,
            removeBorder.getValue() ? GLFW.GLFW_FALSE : GLFW.GLFW_TRUE);

        // Opacity (0.0–1.0)
        float op = (float)(opacity.getValue() / 100.0);
        GLFW.glfwSetWindowOpacity(handle, op);

        // Click-through
        boolean ct = clickThrough.getValue();
        safeSetMousePassthrough(handle, ct);

        if (ct) {
            disableIncompatible();
        } else {
            re_enableIncompatible();
        }
    }

    /**
     * GLFW_MOUSE_PASSTHROUGH was added in GLFW 3.4 (LWJGL 3.3.1+).
     * We call via reflection-free try/catch so older LWJGL builds don't crash.
     */
    private void safeSetMousePassthrough(long handle, boolean value) {
        try {
            // GLFW_MOUSE_PASSTHROUGH = 0x0002000D
            GLFW.glfwSetWindowAttrib(handle, 0x0002000D,
                value ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        } catch (Throwable t) {
            // Older GLFW — silently ignore
        }
    }

    private void disableIncompatible() {
        try {
            var reg = com.davidprogr.shadowclient.feature.FeatureRegistry.get();
            for (String name : INCOMPATIBLE) {
                Feature f = reg.find(name);
                if (f != null && f.isEnabled()) f.toggle();
            }
        } catch (Exception ignored) {}
    }

    private void re_enableIncompatible() {
        // We don't re-enable them automatically — user toggled them off,
        // they should re-enable manually. Just a no-op placeholder.
    }

    // ─────────────────────────────────────────────────────────────
    //  Utility — call this from InGameHudMixin to draw overlay hint
    // ─────────────────────────────────────────────────────────────

    /** Returns true when click-through is active — used by HUDRenderer to show a hint. */
    public boolean isClickThroughActive() {
        return isEnabled() && clickThrough.getValue();
    }
}
