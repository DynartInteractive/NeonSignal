package net.dynart.neonsignal.core.analytics;

import net.dynart.lisa.core.Settings;

public final class AnalyticsSettings {

    private static final String KEY = "analytics_enabled";

    private AnalyticsSettings() {
    }

    public static boolean isAnalyticsEnabled(Settings settings) {
        return settings.getPreferences().getBoolean(KEY, true);
    }

    public static void setAnalyticsEnabled(Settings settings, boolean value) {
        settings.getPreferences().putBoolean(KEY, value);
    }
}
