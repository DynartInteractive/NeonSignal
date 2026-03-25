package net.dynart.neonsignal;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import net.dynart.neonsignal.core.analytics.AnalyticsManager;
import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.Level;
import net.dynart.neonsignal.core.Settings;
import net.dynart.neonsignal.core.User;
import net.dynart.neonsignal.components.PlayerComponent;

public class FirebaseAnalyticsManager implements AnalyticsManager {

    private final FirebaseAnalytics firebaseAnalytics;
    private boolean enabled;

    public FirebaseAnalyticsManager(Context context, Settings settings) {
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        this.enabled = settings.isAnalyticsEnabled();
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean value) {
        enabled = value;
        firebaseAnalytics.setAnalyticsCollectionEnabled(value);
    }

    @Override
    public void trackScreen(String screenName) {
        if (!enabled) return;
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    @Override
    public void trackLevelStart(Level level) {
        if (!enabled) return;
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, level.getName());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_START, bundle);
    }

    @Override
    public void trackDeath(Level level, float x, float y) {
        if (!enabled) return;
        Bundle bundle = new Bundle();
        bundle.putString("level_name", level.getName());
        bundle.putInt("death_x", (int) x);
        bundle.putInt("death_y", (int) y);
        firebaseAnalytics.logEvent("player_death", bundle);
    }

    @Override
    public void trackCheckpoint(Level level, float x, float y) {
        if (!enabled) return;
        Bundle bundle = new Bundle();
        bundle.putString("level_name", level.getName());
        bundle.putInt("checkpoint_x", (int) x);
        bundle.putInt("checkpoint_y", (int) y);
        firebaseAnalytics.logEvent("checkpoint_reached", bundle);
    }

    @Override
    public void trackLevelCompleted(Level level, PlayerComponent player, GameScene scene) {
        if (!enabled) return;
        Bundle bundle = new Bundle();
        bundle.putString("level_name", level.getName());
        bundle.putInt("enemies_defeated", player.getKnockoutCount());
        bundle.putInt("total_enemies", scene.getEnemyCount());
        bundle.putInt("secrets_found", player.getSecretCount());
        bundle.putInt("total_secrets", scene.getSecretCount());
        bundle.putInt("items_collected", player.getItemCount());
        bundle.putInt("total_items", scene.getItemCount());
        bundle.putInt("score", player.getScore());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_END, bundle);
    }
}
