package com.davidprogr.shadowclient.feature.setting;

import org.lwjgl.glfw.GLFW;

public class KeybindSetting extends Setting<Integer> {
    private boolean listening = false;

    public KeybindSetting(String name, String desc, int defaultKey) {
        super(name, desc, defaultKey);
    }

    public boolean isListening()           { return listening; }
    public void    setListening(boolean l) { listening = l; }

    public String getKeyName() {
        if (value == GLFW.GLFW_KEY_UNKNOWN) return "None";
        String raw = GLFW.glfwGetKeyName(value, 0);
        if (raw != null) return raw.toUpperCase();
        return switch (value) {
            case GLFW.GLFW_KEY_LEFT_SHIFT    -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT   -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL  -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT      -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT     -> "RALT";
            case GLFW.GLFW_KEY_SPACE         -> "SPACE";
            case GLFW.GLFW_KEY_CAPS_LOCK     -> "CAPS";
            case GLFW.GLFW_KEY_TAB           -> "TAB";
            case GLFW.GLFW_KEY_ESCAPE        -> "ESC";
            case GLFW.GLFW_KEY_F1  -> "F1";  case GLFW.GLFW_KEY_F2  -> "F2";
            case GLFW.GLFW_KEY_F3  -> "F3";  case GLFW.GLFW_KEY_F4  -> "F4";
            case GLFW.GLFW_KEY_F5  -> "F5";  case GLFW.GLFW_KEY_F6  -> "F6";
            case GLFW.GLFW_KEY_F7  -> "F7";  case GLFW.GLFW_KEY_F8  -> "F8";
            case GLFW.GLFW_KEY_F9  -> "F9";  case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11"; case GLFW.GLFW_KEY_F12 -> "F12";
            default -> "KEY_" + value;
        };
    }

    @Override public Object toJson()           { return value; }
    @Override public void   fromJson(Object o) { if (o instanceof Number n) value = n.intValue(); }
}
