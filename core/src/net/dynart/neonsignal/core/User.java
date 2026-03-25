package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class User {

    private final Preferences preferences;

    public User(Engine engine) {
        EngineConfig config = engine.getConfig();
        preferences = Gdx.app.getPreferences(config.getName() + "_user");
    }

    public boolean isLevelUnlocked(Level level) {
        return true;
        /*
         * TODO: just for testing, remove it later String key = getLevelDataKey(level,
         * "unlocked"); return preferences.getBoolean(key, false);
         */
    }

    public void setLevelUnlocked(Level level) {
        String key = getLevelDataKey(level, "unlocked");
        preferences.putBoolean(key, true);
    }

    public int getStars(Level level) {
        String key = getLevelDataKey(level, "stars");
        return preferences.getInteger(key, 0);
    }

    public void setStars(Level level, int num) {
        String key = getLevelDataKey(level, "stars");
        preferences.putInteger(key, num);
    }

    public static final int MAX_DASH_COOLDOWN_LEVEL = 5;
    public static final int MAX_DASH_LONGEVITY_LEVEL = 3;

    public int getDashCooldownLevel() {
        return preferences.getInteger("dash_cooldown_level", 1);
    }

    public void setDashCooldownLevel(int level) {
        preferences.putInteger(
            "dash_cooldown_level", Math.max(1, Math.min(level, MAX_DASH_COOLDOWN_LEVEL))
        );
    }

    public int getDashLongevityLevel() {
        return preferences.getInteger("dash_longevity_level", 1);
    }

    public void setDashLongevityLevel(int level) {
        preferences.putInteger(
            "dash_longevity_level", Math.max(1, Math.min(level, MAX_DASH_LONGEVITY_LEVEL))
        );
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void save() {
        preferences.flush();
    }

    private String getLevelDataKey(Level level, String name) {
        return "user_level_" + level.getName() + "_" + name;
    }

}
