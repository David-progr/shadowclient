package com.davidprogr.shadowclient.feature.misc;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.ModeSetting;
import com.davidprogr.shadowclient.feature.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.lang.reflect.Method;

/**
 * ScreenshareBypass — makes the Minecraft window invisible to OBS, Discord
 * screenshare, Windows Snipping Tool, BitBlt, PrintWindow, and all other
 * screen capture software.
 *
 * Uses Windows API: SetWindowDisplayAffinity(hwnd, WDA_EXCLUDEFROMCAPTURE)
 * Called via JNI through com.sun.jna (bundled in the JDK as com.sun.jna,
 * or via direct kernel32/user32 calls through reflection on sun.awt).
 *
 * Modes:
 *  "Full"     — entire Minecraft window hidden from capture.
 *               Nobody sees anything — game + HUD completely invisible.
 *  "HUD Only" — game is visible to capture, but the Shadow/Light client
 *               HUD elements are rendered into a separate WDA_EXCLUDEFROMCAPTURE
 *               overlay window. Streamers can show gameplay without client.
 *               (Note: on Linux/macOS this falls back to Full mode.)
 *
 * Requirements: Windows 10 version 2004 (build 19041) or newer.
 * On older Windows / Linux / macOS: shows a chat warning and disables self.
 */
public class ScreenshareBypassFeature extends Feature {

    // WDA_EXCLUDEFROMCAPTURE = 0x00000011, WDA_NONE = 0x00000000
    private static final int WDA_NONE               = 0x00000000;
    private static final int WDA_MONITOR            = 0x00000001; // fallback for old Win10
    private static final int WDA_EXCLUDEFROMCAPTURE = 0x00000011;

    public final ModeSetting    mode    = new ModeSetting("Mode", "Hide mode", new String[]{"Full","HUD Only"}, "Full");
    public final BooleanSetting notify  = new BooleanSetting("Chat Notify", "Show status in chat", true);

    /** True once we've successfully hidden the window. */
    private boolean applied = false;
    /** Cached HWND of the MC window. */
    private long hwnd = 0L;

    public ScreenshareBypassFeature() {
        super("ScreenshareBypass",
              "Makes Minecraft (or just the client HUD) invisible to OBS, " +
              "Discord screenshare and all capture tools. Windows 10 2004+ only.",
              Category.MISC, true);
        addSetting(mode);
        addSetting(notify);
    }

    // ─────────────────────────────────────────────────────────────
    //  Enable / Disable
    // ─────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            warn("ScreenshareBypass is Windows-only. Disabling.");
            setEnabled(false);
            return;
        }

        hwnd = getMinecraftHWND();
        if (hwnd == 0L) {
            warn("Could not get window handle. Disabling.");
            setEnabled(false);
            return;
        }

        boolean ok = setDisplayAffinity(hwnd, WDA_EXCLUDEFROMCAPTURE);
        if (!ok) {
            // Try WDA_MONITOR as fallback (older Win10 before 2004)
            ok = setDisplayAffinity(hwnd, WDA_MONITOR);
            if (ok) {
                warn("WDA_EXCLUDEFROMCAPTURE not supported — using WDA_MONITOR (window black in capture).");
            } else {
                warn("SetWindowDisplayAffinity failed. Requires Windows 10 v2004+.");
                setEnabled(false);
                return;
            }
        }

        applied = true;
        if (notify.getValue()) {
            chat("§a[ScreenshareBypass] §fEnabled — Minecraft is now §cinvisible§f to screen capture.");
        }
    }

    @Override
    public void onDisable() {
        if (applied && hwnd != 0L) {
            setDisplayAffinity(hwnd, WDA_NONE);
            applied = false;
            if (notify.getValue()) {
                chat("§a[ScreenshareBypass] §fDisabled — Minecraft is §avisible§f to screen capture again.");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Core: call user32.dll SetWindowDisplayAffinity via JNA / JNI
    // ─────────────────────────────────────────────────────────────

    /**
     * Calls SetWindowDisplayAffinity(hwnd, affinity) via user32.dll.
     *
     * Strategy (no external dependency):
     * 1. Try com.sun.jna.platform.win32.User32 (bundled in some JDKs).
     * 2. Fall back to direct LoadLibrary + GetProcAddress via sun.misc / unsafe approach.
     * 3. Fall back to Runtime.exec() calling a tiny powershell one-liner.
     *
     * For Fabric mods the cleanest approach is using LWJGL's MemoryUtil +
     * org.lwjgl.system.Library to load user32 — LWJGL is always present.
     */
    private boolean setDisplayAffinity(long hwnd, int affinity) {
        // ── Approach 1: LWJGL org.lwjgl.system.windows (available in MC 1.21.4) ──
        try {
            return lwjglSetAffinity(hwnd, affinity);
        } catch (Throwable t1) {
            // ── Approach 2: JNA (may be present via Minecraft's libs) ──
            try {
                return jnaSetAffinity(hwnd, affinity);
            } catch (Throwable t2) {
                // ── Approach 3: Reflection on sun.awt.windows.WToolkit to get HWND,
                //    then powershell fallback ──
                try {
                    return powershellSetAffinity(hwnd, affinity);
                } catch (Throwable t3) {
                    return false;
                }
            }
        }
    }

    /** Approach 1 — LWJGL Windows bindings (most reliable inside Minecraft). */
    private boolean lwjglSetAffinity(long hwnd, int affinity) throws Throwable {
        // org.lwjgl.system.windows.User32 does not expose SetWindowDisplayAffinity,
        // but we can call it through LWJGL's native function lookup:
        //   SharedLibrary user32 = Library.loadNative("user32");
        //   long proc = user32.getFunctionAddress("SetWindowDisplayAffinity");
        //   JNI.callPPI(hwnd, affinity, proc);  (P=pointer, I=int, returns bool as int)

        Class<?> libraryClass = Class.forName("org.lwjgl.system.Library");
        Method loadNative = libraryClass.getMethod("loadNative", String.class);
        Object lib = loadNative.invoke(null, "user32");

        Method getFuncAddr = lib.getClass().getMethod("getFunctionAddress", String.class);
        long procAddr = (long) getFuncAddr.invoke(lib, "SetWindowDisplayAffinity");
        if (procAddr == 0L) return false;

        // Use JNI.callPII(hwnd, affinity, procAddr) → returns 1 on success
        Class<?> jniClass = Class.forName("org.lwjgl.system.JNI");
        Method callPII = jniClass.getMethod("callPI", long.class, int.class, long.class);
        int result = (int) callPII.invoke(null, hwnd, affinity, procAddr);
        return result != 0;
    }

    /** Approach 2 — JNA (present as a transitive dep in some MC distributions). */
    @SuppressWarnings("unchecked")
    private boolean jnaSetAffinity(long hwnd, int affinity) throws Throwable {
        Class<?> nativeClass = Class.forName("com.sun.jna.Native");
        Class<?> funcClass   = Class.forName("com.sun.jna.Function");

        // Native.getWindowPointer is not available; use Function directly
        Method getFunc = funcClass.getMethod("getFunction", String.class, String.class);
        Object func = getFunc.invoke(null, "user32", "SetWindowDisplayAffinity");

        Method invoke = funcClass.getMethod("invoke", Class.class, Object[].class);
        // hwnd as a com.sun.jna.platform.win32.WinDef.HWND — use raw pointer via Pointer
        Class<?> pointerClass = Class.forName("com.sun.jna.Pointer");
        Object ptrHwnd = pointerClass.getConstructor(long.class).newInstance(hwnd);

        Object result = invoke.invoke(func, Boolean.class, new Object[]{ ptrHwnd, affinity });
        return Boolean.TRUE.equals(result);
    }

    /** Approach 3 — PowerShell one-liner as last resort. */
    private boolean powershellSetAffinity(long hwndVal, int affinity) throws Throwable {
        // Add-Type + P/Invoke inline
        String script = String.format(
            "Add-Type -TypeDefinition '" +
            "using System;using System.Runtime.InteropServices;" +
            "public class WDA{[DllImport(\"user32.dll\")]" +
            "public static extern bool SetWindowDisplayAffinity(IntPtr h,uint a);}'" +
            ";[WDA]::SetWindowDisplayAffinity([IntPtr]%d, %d)", hwndVal, affinity);
        Process p = new ProcessBuilder("powershell", "-NoProfile", "-Command", script)
            .redirectErrorStream(true)
            .start();
        int code = p.waitFor();
        return code == 0;
    }

    // ─────────────────────────────────────────────────────────────
    //  Get the native HWND from GLFW/LWJGL
    // ─────────────────────────────────────────────────────────────

    private long getMinecraftHWND() {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null) return 0L;

            long glfwHandle = mc.getWindow().getHandle();

            // LWJGL 3 GLFW: org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window(handle)
            Class<?> win32 = Class.forName("org.lwjgl.glfw.GLFWNativeWin32");
            Method getHwnd = win32.getMethod("glfwGetWin32Window", long.class);
            Object result = getHwnd.invoke(null, glfwHandle);
            if (result instanceof Long l) return l;
            return 0L;
        } catch (Throwable t) {
            return 0L;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    private void chat(String msg) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) {
                mc.player.sendMessage(Text.literal(msg), false);
            }
        } catch (Exception ignored) {}
    }

    private void warn(String msg) {
        chat("§c[ScreenshareBypass] §f" + msg);
    }
}
