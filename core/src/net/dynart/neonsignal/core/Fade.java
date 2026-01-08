package net.dynart.neonsignal.core;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.HashMap;
import java.util.Map;

public class Fade {

    public enum Direction {
        IN, OUT
    }

    private final Engine engine;
    private final Map<Direction, Action> actions = new HashMap<>();

    private float time;
    private Direction direction = Direction.IN;
    private boolean active;
    private FrameBuffer frameBuffer;
    private float value;

    Fade(Engine engine) {
        this.engine = engine;
    }

    void setAction(Direction direction, Action action) {
        actions.put(direction, action);
    }

    void in() {
        time = 0;
        active = true;
        direction = Direction.IN;
    }

    void out() {
        time = 0;
        active = true;
        direction = Direction.OUT;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isActive() {
        return active;
    }

    public void update(float delta) {
        if (!active) {
            return;
        }
        time += delta;
        float duration = 0.5f;
        if (time > duration) {
            time = duration;
            active = false;
            if (actions.containsKey(direction)) {
                actions.get(direction).act(delta);
            }
        }
        if (direction == Direction.OUT) {
            value = 1f - (time / duration);
        } else {
            value = time / duration;
        }
    }

    public void resize(int width, int height) {
        if (width != 0 && height != 0) {
            frameBuffer = new FrameBuffer(Pixmap.Format.RGB888, width, height, false);
        }
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public void draw(Stage stage) {
        if (!active) {
            return;
        }
        FadeRenderer renderer = engine.getFadeRenderer();
        renderer.draw(stage, value);
    }

    public void dispose() {
        frameBuffer.dispose();
    }

}
