package net.dynart.neonsignal.core.analytics;

import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.core.Settings;
import net.dynart.neonsignal.core.User;

public interface AnalyticsManagerFactory {
    AnalyticsManager create(EngineConfig config, User user, Settings settings);
}
