package net.dynart.neonsignal.screens;

import com.badlogic.gdx.Input;

import net.dynart.neonsignal.core.controller.Button;
import net.dynart.neonsignal.core.controller.ControlNameProvider;
import net.dynart.neonsignal.core.DialogStage;
import net.dynart.neonsignal.core.listeners.KeyUpListener;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.Engine;

public class CustomizeKeyboardScreen extends CustomizeButtonsScreen implements KeyUpListener {

    private final ControlNameProvider controlNameProvider;
    private int keyCodeForAssign;
    private DialogStage dialogStage;

    public CustomizeKeyboardScreen(final Engine engine) {
        super(engine);
        controlNameProvider = engine.getControlNameProvider();
    }

    @Override
    public void init() {
        super.init();
        dialogStage = (DialogStage)dialogScreen.getStage();
    }

    @Override
    String getControlName(Button button) {
        return controlNameProvider.getKeyControlName(button);
    }

    @Override
    void savePreferences() {
        for (Button button : Button.values()) {
            settings.setKey(button, gameController.getKeyCode(button));
        }
    }

    @Override
    void menuButtonClicked(MenuButton menuButton) {
        if (isAnimating()) { return; }
        dialogStage.setKeyUpListener(this);
        showDialog("key", menuButton);
    }

    @Override
    public void keyUp(int keyCode) {
        dialogStage.setKeyUpListener(null);
        keyCodeForAssign = keyCode;
        Button buttonForSet = (Button)menuButtonForSet.getUserObject();
        for (Button button : Button.values()) {
            if (gameController.getKeyCode(button) == keyCode && button != buttonForSet) {
                menuButtonForWarning = getButtonByKeyCode(keyCodeForAssign);
                showWarning("key");
                return;
            }
        }
        assignKey();
    }

    private MenuButton getButtonByKeyCode(int keyCode) {
        for (Button button : menuButtonMap.keySet()) {
            if (gameController.getKeyCode(button) == keyCode) {
                return menuButtonMap.get(button);
            }
        }
        return null;
    }

    private void assignKey() {
        Button button = (Button)menuButtonForSet.getUserObject();
        gameController.setKeyCode(button, keyCodeForAssign);
        menuButtonForSet.setText(Input.Keys.toString(keyCodeForAssign));
        if (menuButtonForWarning != null) {
            menuButtonForWarning.setText("?");
            Button otherButton = (Button)menuButtonForWarning.getUserObject();
            gameController.setKeyCode(otherButton, config.getUnusedButtonCode());
        }
        hideDialog();
    }

    @Override
    public void dialogButtonClicked(int index) {
        switch (index) {
            case 0: hideDialog(); break;
            case 1: assignKey(); break;
        }
    }

    private void hideDialog() {
        dialogStage.setKeyUpListener(null);
        dialogScreen.moveOut();
    }

}
