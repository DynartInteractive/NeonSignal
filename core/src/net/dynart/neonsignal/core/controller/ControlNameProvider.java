package net.dynart.neonsignal.core.controller;

import com.badlogic.gdx.Input;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.core.Settings;

/**
 * Provides human-readable control names for buttons based on controller type.
 * Used by tutorial text and customize screens.
 */
public class ControlNameProvider {

    private final Engine engine;
    private final GameController gameController;
    private final EngineConfig config;
    private final Settings settings;

    public ControlNameProvider(Engine engine) {
        this.engine = engine;
        this.gameController = engine.getGameController();
        this.config = engine.getConfig();
        this.settings = engine.getSettings();
    }

    /**
     * Gets the control name for a button based on the current controller type setting.
     */
    public String getControlName(Button button) {
        ControllerType type = settings.getControllerType();
        switch (type) {
            case KEYBOARD:
                return getKeyControlName(button);
            case GAMEPAD:
                return getJoyControlName(button);
            case TOUCH:
                return getTouchControlName(button);
            default:
                return button.getName();
        }
    }

    /**
     * Gets the keyboard key name for a button.
     */
    public String getKeyControlName(Button button) {
        int code = gameController.getKeyCode(button);
        if (code == config.getUnusedButtonCode()) {
            return "?";
        }
        return Input.Keys.toString(code);
    }

    /**
     * Gets the gamepad button/axis name for a button.
     */
    public String getJoyControlName(Button button) {
        Integer joyCode = gameController.getJoyCode(button);
        int unusedButtonCode = config.getUnusedButtonCode();

        if (joyCode != unusedButtonCode) {
            String name = getGamepadButtonName(button);
            if (name != null) return name;
            return joyCode.toString();
        }

        if (gameController.hasAxisData(button)) {
            AxisData data = gameController.getAxisData(button);
            if (data.getCode() != unusedButtonCode) {
                String signStr = data.getSign() < 0 ? "-" : "+";
                return "Axis " + data.getCode() + signStr;
            }
        }

        return "?";
    }

    private String getGamepadButtonName(Button button) {
        GamepadType type = gameController.getActiveGamepadType();
        if (type == GamepadType.PS4 || type == GamepadType.PS5) {
            switch (button) {
                case A: return "Cross";
                case B: return "Circle";
                case X: return "Square";
                case Y: return "Triangle";
                case MENU: return "Options";
                default: return null;
            }
        }
        if (type == GamepadType.XBOX_360 || type == GamepadType.XBOX_ONE) {
            switch (button) {
                case A: return "A";
                case B: return "B";
                case X: return "X";
                case Y: return "Y";
                case MENU: return "Start";
                default: return null;
            }
        }
        return null;
    }

    /**
     * Gets the touch control description for a button.
     */
    public String getTouchControlName(Button button) {
        boolean switched = settings.isTouchSidesSwitched();

        switch (button) {
            case LEFT:
                return switched ? "right side" : "left side";
            case RIGHT:
                return switched ? "left side" : "right side";
            case A:
                return switched ? "left side" : "right side";
            case B:
                return "B button";
            case X:
                return "X button";
            case Y:
                return "Y button";
            case UP:
                return "up";
            case DOWN:
                return "down";
            case MENU:
                return "menu button";
            default:
                return button.getName();
        }
    }

}
