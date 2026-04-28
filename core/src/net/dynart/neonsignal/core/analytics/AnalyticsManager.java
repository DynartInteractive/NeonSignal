package net.dynart.neonsignal.core.analytics;

import net.dynart.neonsignal.components.PlayerComponent;
import net.dynart.lisa.core.GameScene;
import net.dynart.lisa.core.Level;

public interface AnalyticsManager {
    void setEnabled(boolean value);

    void trackScreen(String screenName);

    void trackLevelStart(Level level);

    void trackDeath(Level level, float x, float y);

    void trackCheckpoint(Level level, float x, float y);

    void trackLevelCompleted(Level level, PlayerComponent player, GameScene scene);
}
