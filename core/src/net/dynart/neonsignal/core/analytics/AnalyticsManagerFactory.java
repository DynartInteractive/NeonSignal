package net.dynart.neonsignal.core.analytics;

import net.dynart.lisa.core.Settings;
import net.dynart.lisa.core.User;
import net.dynart.lisa.core.analytics.AnalyticsManager;
import net.dynart.neonsignal.NeonSignalEngineConfig;

public interface AnalyticsManagerFactory {
    AnalyticsManager create(NeonSignalEngineConfig config, User user, Settings settings);
}
