package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import net.dynart.neonsignal.core.controller.AxisData;
import net.dynart.neonsignal.core.controller.Button;
import net.dynart.neonsignal.core.controller.ButtonMap;
import net.dynart.neonsignal.core.controller.ControllerType;
import net.dynart.neonsignal.core.controller.ControllerTypeMap;
import net.dynart.neonsignal.core.script.ScriptLoader;
import net.dynart.neonsignal.core.utils.JsonUtil;
import net.dynart.neonsignal.core.utils.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused") // the methods called by reflection
public class EngineConfig {

    private EntityFactory entityFactory;
    private FadeRenderer fadeRenderer;
    private ScriptLoader scriptLoader;

    protected String name = "NeonSignal";
    protected String section = "";
    protected float gameVirtualHeight = 180;
    protected float gameMaxVirtualWidth = 373;
    protected float stageVirtualHeight = 720;
    protected float stageMaxVirtualWidth = 1680;
    protected float maxVelocity;
    protected int tilemapMaxWidth = 64;
    protected int tilemapMaxHeight = 64;
    protected int tileWidth = 16;
    protected int tileHeight = 16;
    protected float playerJumpVelocity = 4.5f;
    protected int playerMaxJumpCount = 2; // TODO: playerDefaultMaxJumpCount ?
    protected int playerMaxAirCountToJump = 5;
    protected float playerMinVerticalAxis = 0.6f;
    protected float playerMinHorizontalAxis = 0.4f;
    protected float playerAcceleration = 0.3f;
    protected float playerMaxRunningVelocity = 1.7f;
    protected float playerInactiveHealthTime = 3;
    protected float playerJumpVelocityAddition = 3f;
    protected float playerSlidingDivider = 4f;
    protected float playerPainHorizontalVelocity = 50f;
    protected float playerPainTime = 0.3f;
    protected float defaultGravity = 0.25f;
    protected float defaultSoundVolume = 0.75f;
    protected float defaultMusicVolume = 1f;
    protected float audioMaxDistance = 160f;
    protected int unusedButtonCode = -999;
    protected boolean mobile;
    protected ControllerType defaultControllerType = ControllerType.TOUCH;
    protected String[] touchButtonNames = {"left", "right", "a", "b"};
    protected float touchButtonSize = 200;
    protected Map<String, Vector2> defaultTouchPositions = new HashMap<>();
    protected Button[] buttonOrder = { Button.LEFT, Button.RIGHT, Button.UP, Button.DOWN, Button.A, Button.B, Button.X, Button.Y, Button.MENU};
    protected String[] buttonLabels = { "Left", "Right", "Up", "Down", "Jump", "Dash", "Fire", "Switch", "Pause" };
    protected Map<Button, Integer> defaultKeyMapping = new HashMap<>();
    protected Map<Button, Integer> defaultJoyMapping = new HashMap<>();
    protected Map<Button, AxisData> defaultAxisMapping = new HashMap<>();

    public void setEntityFactory(EntityFactory value) {
        entityFactory = value;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setDefaultFadeRenderer(FadeRenderer value) {
        fadeRenderer = value;
    }

    public FadeRenderer getDefaultFadeRenderer() {
        return fadeRenderer;
    }

    public void setScriptLoader(ScriptLoader value) {
        scriptLoader = value;
    }

    public ScriptLoader getScriptLoader() {
        return scriptLoader;
    }

    public void load(String section) {
        this.section = section;
        JsonReader jsonReader = new JsonReader();
        JsonValue internalJson = JsonUtil.tryToLoad(jsonReader, Gdx.files.internal("data/config.json"));
        JsonValue externalJson = JsonUtil.tryToLoad(jsonReader, Gdx.files.local("data/config-custom.json"));
        JsonValue configJson = JsonUtil.mergeJson(internalJson, externalJson);
        loadFromJson(configJson.get("all"));
        loadFromJson(configJson.get(section));
    }

    public String getSection() {
        return section;
    }

    public void loadFromJson(JsonValue json) {
        for (JsonValue child = json.child(); child != null; child = child.next()) {
            String propertyName = StringUtil.camelize(child.name, false);
            try {
                setField(propertyName, child, getField(propertyName));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("No field found: " + getPropertyFullName(propertyName));
            }
        }
    }

    private Field getField(String propertyName) throws NoSuchFieldException {
        try {
            return getClass().getDeclaredField(propertyName);
        } catch (NoSuchFieldException e) {
            return getClass().getSuperclass().getDeclaredField(propertyName);
        }
    }

    private String getPropertyFullName(String propertyName) {
        return getClass() + "::" + propertyName;
    }

    private void setField(String propertyName, JsonValue child, Field field) {
        try {
            if (field.getType() == int.class && child.isLong()) {
                field.setInt(this, (int)child.asLong());
            } else if (field.getType() == float.class && child.isNumber()) {
                field.setFloat(this, child.asFloat());
            } else if (field.getType() == String.class && child.isString()) {
                field.set(this, child.asString());
            } else if (field.getType() == boolean.class && child.isBoolean()) {
                field.set(this, child.asBoolean());
            } else {
                callLoadMethodForField(child, propertyName);
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Can't access field: " + getPropertyFullName(propertyName));
        }
    }

    private void callLoadMethodForField(JsonValue child, String propertyName) {
        String methodName = "load" + StringUtil.capitalizeFirstChar(propertyName);
        String fullMethodName = getClass() + "::" + methodName + "()";
        try {
            Method method = getClass().getMethod(methodName, JsonValue.class);
            method.invoke(this, child);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't load config value (no such method): " + fullMethodName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't load config value (illegal access): " + fullMethodName);
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
            throw new RuntimeException("Can't load config value (runtime error): " + fullMethodName);
        }
    }

    public void loadDefaultControllerType(JsonValue data) {
        defaultControllerType = ControllerTypeMap.getByName(data.asString());
    }

    public void loadTouchButtonNames(JsonValue data) {
        touchButtonNames = data.asStringArray();
    }

    public void loadDefaultTouchPositions(JsonValue data) {
        defaultTouchPositions.clear();
        for (JsonValue child = data.child(); child != null; child = child.next()) {
            defaultTouchPositions.put(child.name, new Vector2(child.getInt(0), child.getInt(1)));
        }
    }

    public void loadButtonOrder(JsonValue data) {
        String[] buttonNames = data.asStringArray();
        buttonOrder = new Button[buttonNames.length];
        for (int i = 0; i < buttonOrder.length; i++) {
            buttonOrder[i] = ButtonMap.getByName(buttonNames[i]);
        }
    }

    public void loadButtonLabels(JsonValue data) {
        buttonLabels = data.asStringArray();
    }

    public void loadDefaultKeyMapping(JsonValue data) {
        defaultKeyMapping.clear();
        for (JsonValue child = data.child(); child != null; child = child.next()) {
            defaultKeyMapping.put(ButtonMap.getByName(child.name), child.asInt());
        }
    }

    public void loadDefaultJoyMapping(JsonValue data) {
        defaultJoyMapping.clear();
        for (JsonValue child = data.child(); child != null; child = child.next()) {
            defaultJoyMapping.put(ButtonMap.getByName(child.name), child.asInt());
        }
    }

    public void loadDefaultAxisMapping(JsonValue data) {
        defaultAxisMapping.clear();
        for (JsonValue child = data.child(); child != null; child = child.next()) {
            defaultAxisMapping.put(ButtonMap.getByName(child.name), new AxisData(child.getInt(0), child.getInt(1)));
        }
    }

    public String getName() {
        return name;
    }

    public float getGameVirtualHeight() {
        return gameVirtualHeight;
    }

    public float getGameMaxVirtualWidth() {
        return gameMaxVirtualWidth;
    }

    public float getStageVirtualHeight() {
        return stageVirtualHeight;
    }

    public float getStageMaxVirtualWidth() {
        return stageMaxVirtualWidth;
    }

    public int getTilemapMaxWidth() {
        return tilemapMaxWidth;
    }

    public int getTilemapMaxHeight() {
        return tilemapMaxHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public float getPlayerJumpVelocity() {
        return playerJumpVelocity;
    }

    public int getPlayerMaxJumpCount() {
        return playerMaxJumpCount;
    }

    public int getPlayerMaxAirCountToJump() {
        return playerMaxAirCountToJump;
    }

    public float getPlayerMinVerticalAxis() {
        return playerMinVerticalAxis;
    }

    public float getPlayerMinHorizontalAxis() {
        return playerMinHorizontalAxis;
    }

    public float getPlayerAcceleration() {
        return playerAcceleration;
    }

    public float getPlayerMaxRunningVelocity() {
        return playerMaxRunningVelocity;
    }

    public float getPlayerInactiveHealthTime() {
        return playerInactiveHealthTime;
    }

    public float getPlayerJumpVelocityAddition() {
        return playerJumpVelocityAddition;
    }

    public float getPlayerSlidingDivider() {
        return playerSlidingDivider;
    }

    public float getPlayerPainHorizontalVelocity() {
        return playerPainHorizontalVelocity;
    }

    public float getPlayerPainTime() {
        return playerPainTime;
    }

    public float getDefaultGravity() {
        return defaultGravity;
    }

    public float getDefaultSoundVolume() {
        return defaultSoundVolume;
    }

    public float getDefaultMusicVolume() {
        return defaultMusicVolume;
    }

    public float getAudioMaxDistance() {
        return audioMaxDistance;
    }

    public ControllerType getDefaultControllerType() {
        return defaultControllerType;
    }

    public int getUnusedButtonCode() {
        return unusedButtonCode;
    }

    public String[] getTouchButtonNames() {
        return touchButtonNames;
    }

    public float getTouchButtonSize() { return touchButtonSize; }

    public Map<String, Vector2> getDefaultTouchPositions() {
        return defaultTouchPositions;
    }

    public Button[] getButtonOrder() {
        return buttonOrder;
    }

    public String[] getButtonLabels() {
        return buttonLabels;
    }

    public Map<Button, Integer> getDefaultKeyMapping() {
        return defaultKeyMapping;
    }

    public Map<Button, Integer> getDefaultJoyMapping() {
        return defaultJoyMapping;
    }

    public Map<Button, AxisData> getDefaultAxisMapping() {
        return defaultAxisMapping;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public boolean isMobile() {
        return mobile;
    }


    protected Map<String, Vector2> playerGunBarrelPosition = new HashMap<>();

    protected String analyticsMeasurementId = "";
    protected String analyticsApiSecret = "";

    public String getAnalyticsMeasurementId() {
        return analyticsMeasurementId;
    }

    public String getAnalyticsApiSecret() {
        return analyticsApiSecret;
    }
}
