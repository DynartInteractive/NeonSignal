package net.dynart.neonsignal.core;

import net.dynart.neonsignal.core.controller.Button;
import net.dynart.neonsignal.core.controller.ButtonMap;
import net.dynart.neonsignal.core.controller.ControlNameProvider;
import net.dynart.neonsignal.core.controller.ControllerType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves tutorial text variables like {movement_text} and {jump_text},
 * as well as button references like [A] and [LEFT],
 * based on the current controller type and settings.
 */
public class TutorialTextProvider {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([a-z_]+)\\}");
    private static final Pattern BUTTON_PATTERN = Pattern.compile("\\[([A-Z]+)\\]");

    private final Engine engine;

    public TutorialTextProvider(Engine engine) {
        this.engine = engine;
    }

    /**
     * Resolves all variables and button references in the given text.
     * Variables are in the format {variable_name}.
     * Button references are in the format [BUTTON_NAME] (e.g., [A], [LEFT]).
     *
     * @param text The text containing variables/buttons to resolve
     * @return The text with all variables and buttons replaced
     */
    public String resolveVariables(String text) {
        if (text == null) {
            return text;
        }

        // First resolve {variables}
        if (text.contains("{")) {
            Matcher matcher = VARIABLE_PATTERN.matcher(text);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                String variableName = matcher.group(1);
                String replacement = getVariableValue(variableName);
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(result);
            text = result.toString();
        }

        // Then resolve [BUTTON] references
        if (text.contains("[")) {
            Matcher matcher = BUTTON_PATTERN.matcher(text);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                String buttonName = matcher.group(1);
                String replacement = getButtonControlName(buttonName);
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(result);
            text = result.toString();
        }

        return text;
    }

    /**
     * Gets the control name for a button reference.
     * Wraps keyboard and gamepad names in square brackets, touch names are plain text.
     *
     * @param buttonName The button name in uppercase (e.g., "A", "LEFT")
     * @return The resolved control name, or the original [name] if unknown
     */
    private String getButtonControlName(String buttonName) {
        Button button = ButtonMap.getByName(buttonName.toLowerCase());
        if (button == null) {
            return "[" + buttonName + "]";
        }
        ControlNameProvider provider = engine.getControlNameProvider();
        String controlName = provider.getControlName(button);

        // Wrap in brackets for keyboard and gamepad, plain text for touch
        ControllerType type = engine.getSettings().getControllerType();
        if (type == ControllerType.TOUCH) {
            return controlName;
        }
        return "[" + controlName + "]";
    }

    /**
     * Gets the value for a specific variable based on controller type and settings.
     *
     * @param name The variable name (without braces)
     * @return The resolved value, or the original {name} if unknown
     */
    public String getVariableValue(String name) {
        Settings settings = engine.getSettings();
        ControllerType type = settings.getControllerType();

        switch (name) {
            case "movement_text":
                return getMovementText(type, settings);
            case "jump_text":
                return getJumpText(type, settings);
            default:
                return "{" + name + "}";
        }
    }

    private String getMovementText(ControllerType type, Settings settings) {
        switch (type) {
            case KEYBOARD:
                return "LEFT and RIGHT keys";
            case TOUCH:
                return settings.isTouchSidesSwitched()
                    ? "right and left sides of the screen"
                    : "left and right sides of the screen";
            case GAMEPAD:
                return "D-pad or left stick";
            default:
                return "movement controls";
        }
    }

    private String getJumpText(ControllerType type, Settings settings) {
        switch (type) {
            case KEYBOARD:
                return "SPACE";
            case TOUCH:
                return settings.isTouchSidesSwitched()
                    ? "left side of the screen"
                    : "right side of the screen";
            case GAMEPAD:
                return "A button";
            default:
                return "jump button";
        }
    }

}
