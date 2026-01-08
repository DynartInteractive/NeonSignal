package net.dynart.neonsignal.core.controller;

import com.badlogic.gdx.InputAdapter;

public class KeyboardListener extends InputAdapter {

    private final GameController gameController;

    public KeyboardListener(GameController gameController) {
        this.gameController = gameController;
    }

    public boolean keyDown(int keyCode) {
        return gameController.keyEvent(keyCode, true, true);
    }

    public boolean keyUp(int keyCode) {
        return gameController.keyEvent(keyCode, false, true);
    }

}
