package net.dynart.neonsignal.core.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class ButtonImageDragListener extends InputListener {

    private float takeX;
    private float takeY;
    private int draggingPointer = -1;
    private final Vector2 position;

    public ButtonImageDragListener(Vector2 position) {
        this.position = position;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (draggingPointer != -1) {
            return false;
        }
        takeX = x;
        takeY = y;
        draggingPointer = pointer;
        return true;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        draggingPointer = -1;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        if (pointer != draggingPointer) {
            return;
        }
        Actor actor = event.getListenerActor();
        Stage stage = actor.getStage();
        int newX = (int)(actor.getX() + x - takeX);
        int newY = (int)(actor.getY() + y - takeY);
        if (newX < 0) {
            newX = 0;
        }
        if (newY < 0) {
            newY = 0;
        }
        float w = actor.getWidth();
        float h = actor.getHeight();
        if (newX + w > stage.getWidth()) {
            newX = (int)(stage.getWidth() - w);
        }
        if (newX < 0) {
            newX = 0;
        }
        if (newY + h > stage.getHeight()) {
            newY = (int)(stage.getHeight() - h);
        }
        if (newY < 0) {
            newY = 0;
        }
        actor.setPosition(newX, newY);
        position.set((newX > stage.getWidth() / 2f ? newX - stage.getWidth() : newX) + actor.getWidth() / 2f, newY + actor.getHeight() / 2f);
    }
}
