package net.dynart.neonsignal.core.controller;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;

import java.util.HashMap;
import java.util.Map;

public class GamepadProfile {

    private final GamepadType gamepadType;
    private final Map<Button, Integer> joyMap = new HashMap<>();
    private final Map<Button, AxisData> axisMap = new HashMap<>();

    public GamepadProfile(Controller controller, int unusedButtonCode) {
        gamepadType = GamepadType.fromControllerName(controller.getName());
        ControllerMapping mapping = controller.getMapping();

        // Face buttons
        joyMap.put(Button.A, mappedOrUnused(mapping.buttonA, unusedButtonCode));
        joyMap.put(Button.B, mappedOrUnused(mapping.buttonB, unusedButtonCode));
        joyMap.put(Button.X, mappedOrUnused(mapping.buttonX, unusedButtonCode));
        joyMap.put(Button.Y, mappedOrUnused(mapping.buttonY, unusedButtonCode));

        // Menu
        joyMap.put(Button.MENU, mappedOrUnused(mapping.buttonStart, unusedButtonCode));

        // D-pad buttons
        joyMap.put(Button.LEFT, mappedOrUnused(mapping.buttonDpadLeft, unusedButtonCode));
        joyMap.put(Button.RIGHT, mappedOrUnused(mapping.buttonDpadRight, unusedButtonCode));
        joyMap.put(Button.UP, mappedOrUnused(mapping.buttonDpadUp, unusedButtonCode));
        joyMap.put(Button.DOWN, mappedOrUnused(mapping.buttonDpadDown, unusedButtonCode));

        // Left stick axes
        if (mapping.axisLeftX >= 0) {
            axisMap.put(Button.LEFT, new AxisData(mapping.axisLeftX, -1));
            axisMap.put(Button.RIGHT, new AxisData(mapping.axisLeftX, 1));
        }
        if (mapping.axisLeftY >= 0) {
            axisMap.put(Button.UP, new AxisData(mapping.axisLeftY, -1));
            axisMap.put(Button.DOWN, new AxisData(mapping.axisLeftY, 1));
        }
    }

    private int mappedOrUnused(int code, int unusedButtonCode) {
        return code >= 0 ? code : unusedButtonCode;
    }

    public GamepadType getGamepadType() {
        return gamepadType;
    }

    public Map<Button, Integer> getJoyMap() {
        return joyMap;
    }

    public Map<Button, AxisData> getAxisMap() {
        return axisMap;
    }

}
