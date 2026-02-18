package net.dynart.neonsignal.screens;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.dynart.neonsignal.core.controller.AxisData;
import net.dynart.neonsignal.core.controller.ControlNameProvider;
import net.dynart.neonsignal.core.controller.GamepadListener;
import net.dynart.neonsignal.core.controller.GamepadProfile;
import net.dynart.neonsignal.core.controller.GamepadType;
import net.dynart.neonsignal.core.controller.Button;
import net.dynart.neonsignal.core.listeners.AxisMovedListener;
import net.dynart.neonsignal.core.listeners.ButtonUpListener;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;
import net.dynart.neonsignal.core.Engine;

public class CustomizeGamepadScreen extends CustomizeButtonsScreen implements ButtonUpListener, AxisMovedListener {

    private final ControlNameProvider controlNameProvider;
    private GamepadListener gamepadListener;
    private int buttonCodeForAssign;
    private int axisCodeForAssign;
    private int axisSignForAssign;
    private MenuButton resetButton;

    public CustomizeGamepadScreen(final Engine engine) {
        super(engine);
        controlNameProvider = engine.getControlNameProvider();
        gamepadListener = engine.getGamepadListener();

        resetButton = new MenuButton(engine, "Reset", styles.getDefaultButtonStyle());
        resetButton.setSize(200, 80);
        resetButton.setY(240);
        resetButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resetToAutoDetected();
            }
        });
        group.addActor(resetButton);
    }

    @Override
    public void init() {
        super.init();
        MenuCursorItem resetItem = menuCursor.addItem(resetButton);
        resetItem.setNeighbour(MenuCursor.Neighbour.RIGHT, backButton);
        resetItem.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                resetToAutoDetected();
            }
        });
        backButtonItem.setNeighbour(MenuCursor.Neighbour.LEFT, resetButton);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        resetButton.setX(backButton.getX() - resetButton.getWidth() - 20);
    }

    @Override
    void savePreferences() {
        for (Button button : Button.values()) {
            settings.setJoy(button, gameController.getJoyCode(button));
            if (gameController.hasAxisData(button)) {
                settings.setAxisData(button, gameController.getAxisData(button));
            }
        }
        settings.save();
    }

    @Override
    String getControlName(Button button) {
        return controlNameProvider.getJoyControlName(button);
    }

    @Override
    void menuButtonClicked(MenuButton menuButton) {
        int unusedButtonCode = config.getUnusedButtonCode();
        buttonCodeForAssign = unusedButtonCode;
        axisCodeForAssign = unusedButtonCode;
        addJoyListeners();
        showDialog("button", menuButton);
    }

    private void addJoyListeners() {
        gamepadListener.setAxisMovedListener(this);
        gamepadListener.setButtonUpListener(this);
    }

    private MenuButton getButtonByCode(int buttonCode) {
        for (Button button : menuButtonMap.keySet()) {
            MenuButton menuButton = menuButtonMap.get(button);
            if (menuButton != menuButtonForSet && gameController.getJoyCode(button) == buttonCode) {
                return menuButton;
            }
        }
        return null;
    }

    private MenuButton getButtonByAxisData(int axisCode, int sign) {
        for (Button button : menuButtonMap.keySet()) {
            MenuButton menuButton = menuButtonMap.get(button);
            if (menuButton != menuButtonForSet && gameController.hasAxisData(button)) {
                AxisData data = gameController.getAxisData(button);
                if (data.getCode() == axisCode && data.getSign() == sign) {
                    return menuButton;
                }
            }
        }
        return null;
    }

    private void assignButtonOrAxis() {
        Button button = (Button)menuButtonForSet.getUserObject();
        if (buttonCodeForAssign != config.getUnusedButtonCode()) {
            resetAxis(button);
            gameController.setJoyCode(button, buttonCodeForAssign);
        } else {
            resetButton(button);
            gameController.setAxisData(button, axisCodeForAssign, axisSignForAssign);
        }
        updateButtonText(button);
        if (menuButtonForWarning != null) {
            button = (Button) menuButtonForWarning.getUserObject();
            resetButton(button);
            resetAxis(button);
            updateButtonText(button);
        }
        hideDialog();
    }

    private void resetAxis(Button button) {
        if (gameController.hasAxisData(button)) {
            gameController.setAxisData(button, config.getUnusedButtonCode(), 0);
        }
    }

    private void resetButton(Button button) {
        gameController.setJoyCode(button, config.getUnusedButtonCode());
    }

    private void updateButtonText(Button button) {
        MenuButton menuButton = menuButtonMap.get(button);
        menuButton.setText(getControlName(button));
    }

    private void refreshAllButtonTexts() {
        for (Button button : menuButtonMap.keySet()) {
            MenuButton menuButton = menuButtonMap.get(button);
            menuButton.setText(getControlName(button));
        }
    }

    private void resetToAutoDetected() {
        settings.clearJoyMappings();
        Controller activeController = gamepadListener.getActiveController();
        if (activeController != null) {
            GamepadProfile profile = new GamepadProfile(activeController, config.getUnusedButtonCode());
            gameController.applyGamepadProfile(profile);
        } else {
            for (Button button : config.getDefaultJoyMapping().keySet()) {
                gameController.setJoyCode(button, config.getDefaultJoyMapping().get(button));
            }
            for (Button button : config.getDefaultAxisMapping().keySet()) {
                AxisData data = config.getDefaultAxisMapping().get(button);
                if (gameController.hasAxisData(button)) {
                    gameController.setAxisData(button, data.getCode(), data.getSign());
                }
            }
            gameController.setActiveGamepadType(GamepadType.UNKNOWN);
        }
        refreshAllButtonTexts();
    }

    @Override
    public void axisMoved(int axisCode, float value) {
        Button button = (Button) menuButtonForSet.getUserObject();
        if (!gameController.hasAxisData(button) || Math.abs(value) < 0.5f) {
            return;
        }
        axisCodeForAssign = axisCode;
        axisSignForAssign = value < 0 ? -1 : 1;
        menuButtonForWarning = getButtonByAxisData(axisCodeForAssign, axisSignForAssign);
        if (menuButtonForWarning != null) {
            showWarning("axis");
        } else {
            assignButtonOrAxis();
        }
    }

    @Override
    public void buttonUp(int buttonCode) {
        buttonCodeForAssign = buttonCode;
        menuButtonForWarning = getButtonByCode(buttonCode);
        if (menuButtonForWarning != null) {
            showWarning("button");
        } else {
            assignButtonOrAxis();
        }
    }

    @Override
    public void dialogButtonClicked(int index) {
        switch (index) {
            case 0: hideDialog(); break;
            case 1: assignButtonOrAxis(); break;
        }
    }

    private void hideDialog() {
        gamepadListener.setAxisMovedListener(null);
        gamepadListener.setButtonUpListener(null);
        dialogScreen.moveOut();
    }

}
