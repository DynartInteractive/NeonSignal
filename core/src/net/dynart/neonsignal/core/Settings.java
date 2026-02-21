package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;

import net.dynart.neonsignal.VersionUtil;
import net.dynart.neonsignal.core.controller.AxisData;
import net.dynart.neonsignal.core.controller.Button;
import net.dynart.neonsignal.core.controller.ControllerType;
import net.dynart.neonsignal.core.controller.ControllerTypeMap;

public class Settings {

    private final EngineConfig config;
    private final Preferences preferences;
    private final Vector2 touchPosition = new Vector2();

    public Settings(EngineConfig config) {
        preferences = Gdx.app.getPreferences(config.getName());
        this.config = config;
        if (!preferences.contains("version")) {
            preferences.putString("version", VersionUtil.getVersion()); // TODO: use config version, not the game version!
        }
        preferences.flush();
    }

    public int getKey(Button button) {
        Integer defaultKey = config.getDefaultKeyMapping().get(button);
        return preferences.getInteger("key_" + button.getName(), defaultKey);
    }

    public void setKey(Button button, int code) {
        preferences.putInteger("key_" + button.getName(), code);
    }

    public int getJoy(Button button) {
        Integer defaultKey = config.getDefaultJoyMapping().get(button);
        return preferences.getInteger("joy_" + button.getName(), defaultKey);
    }

    public void setJoy(Button button, int code) {
        preferences.putInteger("joy_" + button.getName(), code);
    }

    public void setAxisData(Button button, AxisData axisData) {
        preferences.putString("axis_" + button.getName(), axisData.toString());
    }

    public AxisData getAxisData(Button button) {
        AxisData data = config.getDefaultAxisMapping().get(button);
        String prefKey = "axis_" + button.getName();
        if (preferences.contains(prefKey)) {
            data = new AxisData(preferences.getString(prefKey));
        }
        return data;
    }

    public float getSoundVolume() {
        return preferences.getFloat("sound_volume", config.getDefaultSoundVolume());
    }

    public float getMusicVolume() {
        return preferences.getFloat("music_volume", config.getDefaultMusicVolume());
    }

    public void setSoundVolume(float value) {
        preferences.putFloat("sound_volume", value);
    }

    public void setMusicVolume(float value) {
        preferences.putFloat("music_volume", value);
    }

    public void setControllerType(ControllerType value) {
        preferences.putString("controller_type", value.getName());
    }

    public ControllerType getControllerType() {
        ControllerType result = config.getDefaultControllerType();
        if (preferences.contains("controller_type")) {
            result = ControllerTypeMap.getByName(preferences.getString("controller_type"));
        }
        return result;
    }

    public boolean mustCatchMouse() {
        //return false;
        return getControllerType() != ControllerType.TOUCH;
    }

    public Vector2 getTouchPosition(String name, Screen screen) {
        Vector2 defaultPosition = config.getDefaultTouchPositions().get(name);
        touchPosition.x = preferences.getFloat("button_" + name + "_x", defaultPosition.x - screen.getSideBlackBarWidth());
        touchPosition.y = preferences.getFloat("button_" + name + "_y", defaultPosition.y);
        return touchPosition;
    }

    public void setTouchPosition(String name, float x, float y) {
        preferences.putFloat("button_" + name + "_x", x);
        preferences.putFloat("button_" + name + "_y", y);
    }

    public boolean isTouchSidesSwitched() {
        return preferences.getBoolean("touch_sides_switched", false);
    }

    public void setTouchSidesSwitched(boolean value) {
        preferences.putBoolean("touch_sides_switched", value);
    }

    public boolean hasCustomJoyMapping() {
        for (Button button : Button.values()) {
            if (preferences.contains("joy_" + button.getName()) ||
                preferences.contains("axis_" + button.getName())) {
                return true;
            }
        }
        return false;
    }

    public void clearJoyMappings() {
        for (Button button : Button.values()) {
            preferences.remove("joy_" + button.getName());
            preferences.remove("axis_" + button.getName());
        }
        preferences.flush();
    }

    public String getGamepadName() {
        return preferences.getString("gamepad_name", "");
    }

    public void setGamepadName(String name) {
        preferences.putString("gamepad_name", name);
        preferences.flush();
    }

    public boolean isAnalyticsEnabled() {
        return preferences.getBoolean("analytics_enabled", true);
    }

    public void setAnalyticsEnabled(boolean value) {
        preferences.putBoolean("analytics_enabled", value);
    }

    public void save() {
        preferences.flush();
    }

}
