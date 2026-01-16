package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.screens.GameScreen;

public class CameraHandler {

    public static final String LOG_TAG = "CameraHandler";

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;

    public static final float CHANGE_LIMIT_FULL_TIME = 0.5f;

    private final float[] limit = { -1, -1, -1, -1 };
    private final float[] newLimit = { -1, -1, -1, -1 };
    private float changeLimitStartX;
    private float changeLimitStartY;
    private float changeLimitDx;
    private float changeLimitDy;

    private final Camera camera;
    private final Grid grid;
    private final Viewport viewport;

    private Entity target;
    private float targetX;
    private float targetY;
    private float offsetX;
    private float offsetY;

    private float quakeElapsedTime = -1;
    private float quakeDuration = 0.33f;
    private float quakeOffset = 5f;

    private final MoveOnSegmentUtil mosUtil;
    private final Engine engine;

    private boolean moving;
    private float changeLimitTime;
    private boolean fadeToLimit;
    private boolean fading;

    private final Action fadeEndAction;

    public CameraHandler(final Engine engine) {
        this.engine = engine;

        GameScene gameScene = engine.getGameScene();
        this.viewport = gameScene.getViewport();
        this.grid = gameScene.getGrid();

        camera = viewport.getCamera();

        mosUtil = new MoveOnSegmentUtil();
        mosUtil.setSpeed(96f);
        mosUtil.setDistanceToSlow(4,true, true);

        fadeEndAction = new Action() {
            @Override
            public boolean act(float delta) {
                System.arraycopy(newLimit, 0, limit, 0, 4);
                fading = false;
                fadeToLimit = false;
                engine.getScreen("empty").fadeIn();
                engine.setScreen("empty");
                return true;
            }
        };

    }

    public void setTarget(Entity target) {
        setTarget(target, false);
    }

    public void setTarget(Entity target, boolean useNew) {
        this.target = target;
        BodyComponent body = target.getComponent(BodyComponent.class);
        targetX = adjustX(body.getCenterX(), useNew);
        targetY = adjustY(body.getBottom() + 16f, useNew);
    }

    public void startQuake(float time) {
        startQuake(time, 5f);
    }

    public void startQuake(float time, float offset) {
        quakeOffset = offset;
        quakeDuration = time;
        quakeElapsedTime = 0;
    }

    public void update(float delta) {
        if (fadeToLimit) {
            if (!fading) {
                fading = true;
                GameScreen gameScreen = (GameScreen)engine.getScreen("game");
                gameScreen.fadeOut(fadeEndAction);
            }
            return;
        }
        quake(delta);
        if (moving) {
            if (!mosUtil.update(delta)) {
                moving = false;
            }
            camera.position.x = adjustX(mosUtil.getCurrentX() + offsetX, false);
            camera.position.y = adjustY(mosUtil.getCurrentY() + offsetY, false);
        } else {
            if (changeLimitTime > 0) {
                setTarget(target, true);
                changeLimitTime -= delta;
                if (changeLimitTime < 0) { // before set camera.position!
                    System.arraycopy(newLimit, 0, limit, 0, 4);
                    changeLimitTime = 0;
                }
                float x = adjustX(targetX + offsetX, true);
                float y = adjustY(targetY + offsetY, true);
                float r = changeLimitTime / CHANGE_LIMIT_FULL_TIME;
                r = 1f - Interpolation.smoother.apply(r);
                camera.position.x = changeLimitStartX + r * (x - changeLimitStartX);
                camera.position.y = changeLimitStartY + r * (y - changeLimitStartY);
            } else {
                setTarget(target);
                camera.position.x = adjustX(targetX + offsetX, false);
                camera.position.y = adjustY(targetY + offsetY, false);
            }
        }
        camera.update();
    }

    public boolean isMoving() {
        return moving;
    }

    public void setNewLimit(int direction, float value) {
        newLimit[direction] = value;
        Gdx.app.log(LOG_TAG, "SetNewLimit: " + direction + " " + value);
    }

    public void changeLimit(boolean fade, boolean instant) {
        if (fade) {
            fadeToLimit = true;
        } else {
            setTarget(target, instant);
            if (instant) {
                System.arraycopy(newLimit, 0, limit, 0, 4);
            } else {
                startToChangeLimit();
            }
        }
    }

    public void startToChangeLimit() {
        changeLimitTime = CHANGE_LIMIT_FULL_TIME;
        changeLimitStartX = adjustX(targetX + offsetX, false);
        changeLimitStartY = adjustY(targetY + offsetY, false);
    }

    private float adjustX(float x, boolean useNew) {
        // don't go outside of the grid
        Screen screen = engine.getScreen("game"); // doesn't matter which screen
        float[] l = useNew ? newLimit : limit;
        float viewHalfWidth = (viewport.getWorldWidth() / 2f) - screen.getSideBlackBarWidth(true);
        float maxWidth = grid.getWorldX(grid.getWidth());
        float min = l[LEFT] != -1 ? l[LEFT] + viewHalfWidth : viewHalfWidth;
        float max = (l[RIGHT] != -1 ? l[RIGHT] : maxWidth) - viewHalfWidth;
        if (x < min) {
            x = min;
        } else if (x > max) {
            x = max;
        }
        /*
        // set camera position to the "exact" virtual pixels
        float gw = Gdx.graphics.getWidth();
        float ratioX = gw / (float)viewport.getWorldWidth();
        x = ((int)(x * ratioX)) / ratioX - 0.001f; // that 0.001 is needed for the display fix
        //
        */
        return x;
    }


    private float adjustY(float y, boolean useNew) {
        // don't go outside of the grid
        float[] l = useNew ? newLimit : limit;
        float viewHalfHeight = viewport.getWorldHeight() / 2f;
        float maxHeight = grid.getWorldY(grid.getHeight());
        float min = l[BOTTOM] != -1 ? l[BOTTOM] + viewHalfHeight : viewHalfHeight;
        float max = (l[TOP] != -1 ? l[TOP] : maxHeight) - viewHalfHeight;
        if (y > max) {
            y = max;
        }
        if (y < min) {
            y = min;
        }
        /*
        // set camera position to the "exact" virtual pixels
        float gh = Gdx.graphics.getHeight();
        float ratioY = gh / (float)viewport.getWorldHeight();
        y = ((int)(y * ratioY)) / ratioY - 0.001f; // that 0.001 is needed for the display fix
        //
         */
        return y;
    }

    private void quake(float deltaTime) {
        if (quakeElapsedTime == -1) {
            return;
        }
        quakeElapsedTime += deltaTime;
        if (quakeElapsedTime > quakeDuration) {
            quakeElapsedTime = -1;
            offsetX = 0;
            offsetY = 0;
            return;
        }
        float r = (quakeDuration - quakeElapsedTime) / quakeDuration;
        float offset = r * quakeOffset;
        float rnd = (float)Math.random();
        float rndOffset = rnd * offset;
        offsetX = offset/2 - rndOffset;
        rnd = (float)Math.random();
        rndOffset = rnd * offset;
        offsetY = -offset/2 - rndOffset;
    }

    public void moveTo(Entity entity, float speed) {
        setTarget(entity);
        moving = true;
        mosUtil.init(camera.position.x, camera.position.y, targetX, targetY);
        mosUtil.setSpeed(speed);
    }

    public void moveTo(Entity entity) {
        moveTo(entity, 96f); // TODO: default camera speed
    }
}
