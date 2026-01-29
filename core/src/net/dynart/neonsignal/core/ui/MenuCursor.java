package net.dynart.neonsignal.core.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.dynart.neonsignal.core.TextureManager;

import java.util.HashMap;
import java.util.Map;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.controller.GameController;

public class MenuCursor {

    private static boolean inUse = false;

    private final Engine engine;
    private final Vector2 tempActorPosition = new Vector2();
    private final Image cursorImage;
    private final Map<Actor, MenuCursorItem> items = new HashMap<>();
    private final GameController gameController;

    private float lastAxisX;
    private float lastAxisY;
    private MenuCursorItem currentItem;
    private float cursorImageAlpha;
    private float cursorImageAlphaDir;
    private float globalAlpha = 1f;
    private boolean actionButtonWasDown;
    private float buttonDownTime = 0.001f;
    private boolean buttonDownFirst = true;
    private boolean disabled;

    public enum Event {
        LEFT, RIGHT, UP, DOWN, ENTER
    }

    public enum Neighbour {
        LEFT, RIGHT, UP, DOWN
    }

    public interface Listener {
        void handle(MenuCursorItem item);
    }

    public MenuCursor(Engine engine) {
        this.engine = engine;
        gameController = engine.getGameController();
        TextureManager textureManager = engine.getTextureManager();
        Skin skin = textureManager.getSkin("ui");
        cursorImage = new FadeImage(skin.getDrawable("menu_cursor"));
        cursorImageAlpha = 1f;
        cursorImageAlphaDir = -1;
        cursorImage.setTouchable(null);
        adjustInUse();
    }

    public Image getCursorImage() {
        return cursorImage;
    }

    public void adjustInUse() {
        inUse = engine.getSettings().mustCatchMouse();
    }

    public void setDisabled(boolean value) {
        disabled = value;
    }

    public MenuCursorItem addItem(Actor actor) {
        MenuCursorItem result = new MenuCursorItem(actor);
        items.put(actor, result);
        if (currentItem == null) {
            setCurrentItem(result);
        }
        return result;
    }

    public void setCurrentItem(MenuCursorItem item) {
        currentItem = item;
        adjustCursorImage();
    }

    public void setCurrentItem(Actor actor) {
        if (items.containsKey(actor)) {
            setCurrentItem(items.get(actor));
        }
    }

    public void update() {
        cursorImage.setVisible(!disabled && inUse);
        if (!inUse || disabled || currentItem == null) {
            return;
        }
        adjustButtonDownTime();
        animateActor();
        if (isRightPressed()) {
            goToOrFireEvent(Neighbour.RIGHT);
        } else if (isLeftPressed()) {
            goToOrFireEvent(Neighbour.LEFT);
        } else if (isUpPressed()) {
            goToOrFireEvent(Neighbour.UP);
        } else if (isDownPressed()) {
            goToOrFireEvent(Neighbour.DOWN);
        } else if (isActionButtonPressed()) {
            fireEvent(Event.ENTER);
        }
        saveLastButtonState();
        adjustCursorImage();
    }

    private void adjustButtonDownTime() {
        if (isAnyButtonDown()) {
            buttonDownTime += engine.getDeltaTime();
            if (shouldButtonBePressed()) {
                buttonDownTime = 0;
                buttonDownFirst = false;
            }
        } else {
            buttonDownTime = 0.001f;
            buttonDownFirst = true;
        }
    }

    private boolean shouldButtonBePressed() {
        return (buttonDownFirst && buttonDownTime > 0.5f) || (!buttonDownFirst && buttonDownTime > 0.05f);
    }

    private boolean isAnyButtonDown() {
        return Math.abs(gameController.getAxisX()) > 0.5f
            || Math.abs(gameController.getAxisY()) > 0.5f
            || gameController.isADown();
    }


    private boolean isDownPressed() {
        return (buttonDownTime == 0 || lastAxisY > -0.5f)
            && gameController.getAxisY() < -0.5f;
    }

    private boolean isUpPressed() {
        return (buttonDownTime == 0 || lastAxisY < 0.5f)
            && gameController.getAxisY() > 0.5f;
    }

    private boolean isLeftPressed() {
        return (buttonDownTime == 0 || lastAxisX > -0.5f)
            && gameController.getAxisX() < -0.5f;
    }

    private boolean isRightPressed() {
        return (buttonDownTime == 0 || lastAxisX < 0.5f)
            && gameController.getAxisX() > 0.5f;
    }

    private boolean isActionButtonPressed() {
        return actionButtonWasDown && !isActionButtonDown();
    }

    private boolean isActionButtonDown() {
        return gameController.isADown() || gameController.isXDown();
    }

    private void goToOrFireEvent(Neighbour neighbour) {
        Event event = convertToEvent(neighbour);
        if (currentItem.hasListener(event)) {
            fireEvent(event);
        } else {
            goTo(neighbour);
        }
    }

    private Event convertToEvent(Neighbour neighbour) {
        Event result = Event.LEFT;
        switch (neighbour) {
            case RIGHT: result = Event.RIGHT; break;
            case DOWN: result = Event.DOWN; break;
            case UP: result = Event.UP; break;
        }
        return result;
    }

    private void fireEvent(Event event) {
        if (currentItem.hasListener(event)) {
            Listener listener = currentItem.getListener(event);
            listener.handle(currentItem);
        }
    }

    private void goTo(Neighbour neighbour) {
        if (currentItem.hasNeighbour(neighbour)) {
            Actor actor = currentItem.getNeighbour(neighbour);
            if (actor.isVisible()) { // TODO: find next neighbour, do with disabled as well
                setCurrentItem(items.get(actor));
            }
        }
    }

    private void saveLastButtonState() {
        lastAxisX = gameController.getAxisX();
        lastAxisY = gameController.getAxisY();
        actionButtonWasDown = isActionButtonDown();
    }

    private void animateActor() {
        if (currentItem.getActor() instanceof MenuButton) {
            MenuButton menuButton = (MenuButton)currentItem.getActor();
            if (isActionButtonDown()) {
                menuButton.push();
            } else {
                menuButton.release();
            }
        }
    }

    public void setGlobalAlpha(float value) {
        globalAlpha = value;
    }

    private void adjustCursorImage() {
        cursorImageAlpha += engine.getDeltaTime() * 2f * cursorImageAlphaDir;
        if (cursorImageAlpha < 0) {
            cursorImageAlpha = 0;
            cursorImageAlphaDir = 1;
        }
        if (cursorImageAlpha > 0.75f) {
            cursorImageAlpha = 0.75f;
            cursorImageAlphaDir = -1;
        }
        cursorImage.getColor().a = cursorImageAlpha * globalAlpha;
        Actor actor = currentItem.getActor();
        Actor parent = cursorImage.getParent();
        tempActorPosition.set(0, 0);
        Vector2 stagePosition = actor.localToStageCoordinates(tempActorPosition);
        Vector2 localPosition = stagePosition;
        if (parent != null) {
            localPosition = parent.stageToLocalCoordinates(stagePosition);
        }
        cursorImage.setPosition(localPosition.x - 4, localPosition.y - 4);
        cursorImage.setSize(actor.getWidth() + 9, actor.getHeight() + 9);
    }

}
