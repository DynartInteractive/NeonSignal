package net.dynart.neonsignal.core.controller;

public enum GamepadType {

    UNKNOWN,
    XBOX_360,
    XBOX_ONE,
    PS4,
    PS5;

    public static GamepadType fromControllerName(String name) {
        if (name == null) return UNKNOWN;
        String lower = name.toLowerCase();
        if (lower.contains("dualsense") || lower.contains("ps5")) {
            return PS5;
        }
        if (lower.contains("dualshock 4") || lower.contains("ps4") || lower.contains("wireless controller")) {
            return PS4;
        }
        if (lower.contains("xbox series") || lower.contains("xbox one")) {
            return XBOX_ONE;
        }
        if (lower.contains("xbox 360")) {
            return XBOX_360;
        }
        if (lower.contains("xbox")) {
            return XBOX_ONE;
        }
        return UNKNOWN;
    }

}
