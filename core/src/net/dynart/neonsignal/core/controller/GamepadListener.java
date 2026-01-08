package net.dynart.neonsignal.core.controller;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
//import com.badlogic.gdx.controllers.PovDirection;

import net.dynart.neonsignal.core.listeners.AxisMovedListener;
import net.dynart.neonsignal.core.listeners.ButtonUpListener;

import java.util.HashMap;
import java.util.Map;

public class GamepadListener extends ControllerAdapter {

    private static final Map<Button, Integer> signMap = new HashMap<>() {{
        put(Button.LEFT, -1);
        put(Button.RIGHT, 1);
        put(Button.UP, 1);
        put(Button.DOWN, -1);
    }};

    private final GameController gameController;

    private ButtonUpListener buttonUpListener;
    private AxisMovedListener axisMovedListener;

    public GamepadListener(GameController gameController) {
        this.gameController = gameController;
    }

    public void setButtonUpListener(ButtonUpListener buttonUpListener) {
        this.buttonUpListener = buttonUpListener;
    }

    public void setAxisMovedListener(AxisMovedListener axisMovedListener) {
        this.axisMovedListener = axisMovedListener;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return gameController.keyEvent(buttonCode, true, false);
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        if (buttonUpListener != null) {
            buttonUpListener.buttonUp(buttonCode);
        }
        return gameController.keyEvent(buttonCode, false, false);
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (axisMovedListener != null) {
            axisMovedListener.axisMoved(axisCode, value);
        }
        for (Button button : gameController.getAxisKeys()) {
            AxisData data = gameController.getAxisData(button);
            int sign = value < 0 ? -1 : 1;
            if (data.getCode() == axisCode && data.getSign() == sign) {
                if (button == Button.LEFT || button == Button.RIGHT) {
                    gameController.setAxisX(Math.abs(value) * signMap.get(button));
                } else {
                    gameController.setAxisY(Math.abs(value) * signMap.get(button));
                }
                return true;
            }
        }
        return true;
    }

}
