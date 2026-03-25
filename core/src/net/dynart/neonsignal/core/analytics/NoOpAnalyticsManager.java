package net.dynart.neonsignal.core.analytics;

import net.dynart.neonsignal.components.PlayerComponent;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.Level;

public class NoOpAnalyticsManager implements AnalyticsManager {

    @Override
    public void setEnabled(boolean value) {
    }

    @Override
    public void trackScreen(String screenName) {
    }

    @Override
    public void trackLevelStart(Level level) {
    }

    @Override
    public void trackDeath(Level level, float x, float y) {
    }

    @Override
    public void trackCheckpoint(Level level, float x, float y) {
    }

    @Override
    public void trackLevelCompleted(Level level, PlayerComponent player, GameScene scene) {
    }
}
