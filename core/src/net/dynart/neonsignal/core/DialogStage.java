package net.dynart.neonsignal.core;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.dynart.neonsignal.core.listeners.KeyUpListener;

public class DialogStage extends Stage {

    private KeyUpListener keyUpListener;

    public DialogStage(Viewport viewport, Batch batch) {
        super(viewport, batch);
    }

    public void setKeyUpListener(KeyUpListener keyUpListener) {
        this.keyUpListener = keyUpListener;
    }

    @Override
    public boolean keyUp(int keyCode) {
        if (keyUpListener != null) {
            keyUpListener.keyUp(keyCode);
        }
        return false;
    }

}
