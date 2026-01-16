package net.dynart.neonsignal.screens;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.dynart.neonsignal.core.listeners.DialogListener;
import net.dynart.neonsignal.core.ui.FadeImage;
import net.dynart.neonsignal.core.ui.ButtonImageDragListener;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;

import java.util.HashMap;
import java.util.Map;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Settings;

public class CustomizeTouchScreen extends MenuScreen implements DialogListener {

    private final Settings settings;
    private final Map<String, FadeImage> buttonImages = new HashMap<>();
    private final Map<String, Vector2> buttonPositions = new HashMap<>();
    /*private MenuButton resetButton;*/
    private final MenuButton switchSidesButton;
    private final Label dragLabel;
    private final Label movingLabel;
    private final Image movingBg;
    private final Label jumpingLabel;
    private final Image jumpingBg;
    private final Map<Actor, Float> xPositions = new HashMap<>();
    private final Action backAction;

    private boolean sidesSwitched;

    public CustomizeTouchScreen(final Engine engine) {
        super(engine);

        settings = engine.getSettings();

        // back button
        backAction = new Action() {
            @Override
            public boolean act(float delta) {
                engine.moveToScreen("settings");
                return true;
            }
        };

/*        // reset button
        resetButton = new MenuButton(engine, "Reset", styles.getDefaultButtonStyle());
        resetButton.setWidth(420);
        resetButton.setHeight(120);
        resetButton.setY(60);
        resetButton.setX(-210);
        xPositions.put(resetButton, resetButton.getX());*/

        // switch sides button
        switchSidesButton = new MenuButton(engine, "Switch sides", styles.getDefaultButtonStyle());
        switchSidesButton.setWidth(420);
        switchSidesButton.setHeight(120);
        switchSidesButton.setY(60);
        switchSidesButton.setX(-210);
        xPositions.put(switchSidesButton, switchSidesButton.getX());

        // drag label
        dragLabel = new Label("Drag the buttons\nfor a new position!", styles.getSecondaryLabelStyle());
        dragLabel.setX(-200);
        dragLabel.setY(220);
        dragLabel.setWidth(400);
        dragLabel.setHeight(80);
        dragLabel.setAlignment(Align.center);
        xPositions.put(dragLabel, dragLabel.getX());

        // set up labels initial position
        float halfWidth = stage.getWidth() / 2;

        // moving label
        movingLabel = new Label("Moving side", engine.getStyles().getSecondaryLabelStyle());
        movingLabel.setWidth(halfWidth - 12f);
        movingLabel.setAlignment(Align.center);
        movingLabel.setPosition(getLabelPositionX(0), getLabelPositionY(0) + 80f);
        xPositions.put(movingLabel, movingLabel.getX());

        movingBg = new Image(skin.getDrawable("side_bg"));
        movingBg.setWidth(halfWidth - 12f);
        movingBg.setPosition(getLabelPositionX(0), getLabelPositionY(0));
        xPositions.put(movingBg, movingBg.getX());

        // jumping label
        jumpingLabel = new Label("Jumping side", engine.getStyles().getSecondaryLabelStyle());
        jumpingLabel.setWidth(halfWidth - 12f);
        jumpingLabel.setAlignment(Align.center);
        jumpingLabel.setPosition(getLabelPositionX(1), getLabelPositionY(1) + 80f);

        jumpingBg = new Image(skin.getDrawable("side_bg"));
        jumpingBg.setWidth(halfWidth - 12f);
        jumpingBg.setPosition(getLabelPositionX(1), getLabelPositionY(1));
        xPositions.put(jumpingBg, jumpingBg.getX());



        // stage
        group.addActor(movingBg);
        group.addActor(jumpingBg);
        group.addActor(movingLabel);
        group.addActor(jumpingLabel);
        group.addActor(menuCursor.getCursorImage());
        group.addActor(dragLabel);
        group.addActor(backButton);
        //group.addActor(resetButton);
        group.addActor(switchSidesButton);

        addSideBlackBars(stage);

        // button images
        for (String name : config.getTouchButtonNames()) {
            FadeImage buttonImage = new FadeImage(skin.getDrawable(name + "_button_down"));
            buttonImages.put(name, buttonImage);
            Vector2 buttonPosition = new Vector2(settings.getTouchPosition(name, this));
            buttonPositions.put(name, buttonPosition);
            buttonImage.addListener(new ButtonImageDragListener(buttonPosition));
            buttonImage.setUserObject(name);
            stage.addActor(buttonImage);
        }
        /*
        resetButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resetClicked();
            }
        });
        */

        switchSidesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switchSidesClicked();
            }
        });

        // cursor set up
        MenuCursorItem item;
        item = menuCursor.addItem(switchSidesButton);
        item.setNeighbour(MenuCursor.Neighbour.UP, backButton);
        item.setNeighbour(MenuCursor.Neighbour.RIGHT, backButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                switchSidesClicked();
            }
        });
        item = menuCursor.addItem(backButton);
        item.setNeighbour(MenuCursor.Neighbour.DOWN, switchSidesButton);
        item.setNeighbour(MenuCursor.Neighbour.RIGHT, switchSidesButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                backClicked();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        sidesSwitched = settings.isTouchSidesSwitched();
    }

    public float getLabelPositionX(int index) {
        float halfWidth = stage.getWidth() / 2;
        int[] order = getOrder();
        float[] positionsX = { -halfWidth + 8f, 4f };
        return positionsX[order[index]];
    }

    public float getLabelPositionY(int index) {
        float halfHeight = stage.getHeight() / 2;
        int[] order = getOrder();
        float[] positionsY = { -halfHeight + 8f, -halfHeight + 8f};
        return positionsY[order[index]];
    }

    public int[] getOrder() {
        int[] order = { 0, 1 };
        if (sidesSwitched) {
            order[0] = 1;
            order[1] = 0;
        }
        return order;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        adjustButtons();
    }

    private void adjustButtons() {
        for (String name : config.getTouchButtonNames()) {
            Vector2 position = buttonPositions.get(name);
            float x = position.x + (position.x < 0 ? stage.getWidth() : 0);
            Image button = buttonImages.get(name);
            button.setPosition(x - button.getWidth() / 2f, position.y - button.getHeight() / 2f); // why the fuck the Align.center doesn't work?
        }
    }

    @Override
    public void backClicked() {
        if (isAnimating()) { return; }
        save();
        moveOut(backAction);
    }
/*
    private void resetClicked() {
        dialogScreen.setHeight(200);
        dialogScreen.setText("Reset the positions?");
        dialogScreen.setButtonCount(2);
        dialogScreen.setButtonText(0, "Cancel");
        dialogScreen.setButtonText(1, "OK");
        savePositions(); // because of the resize
        engine.moveToScreen("dialog");
    }
*/
    private void switchSidesClicked() {
        sidesSwitched = !sidesSwitched;
        settings.setTouchSidesSwitched(sidesSwitched);
        movingLabel.addAction(Actions.moveTo(getLabelPositionX(0), getLabelPositionY(0) + 80f, 0.15f, Interpolation.sineOut));
        jumpingLabel.addAction(Actions.moveTo(getLabelPositionX(1), getLabelPositionY(1) + 80f, 0.15f, Interpolation.sineOut));
        for (String name : config.getTouchButtonNames()) {
            Vector2 position = buttonPositions.get(name);
            position.x = -position.x;
            Image button = buttonImages.get(name);
            float x = position.x + (position.x < 0 ? stage.getWidth() : 0) - button.getWidth() / 2f; // why the fuck the Align.center doesn't work?
            float y = position.y - button.getHeight() / 2f;  // why the fuck the Align.center doesn't work?
            buttonImages.get(name).addAction(Actions.moveTo(x, y, 0.15f, Interpolation.sineOut));
        }
    }
/*
    @Override
    public void dialogButtonClicked(int index) {
        if (index == 1) {
            resetPositions();
            savePositions();
        }
        dialogScreen.moveOut();
    }

    private void resetPositions() {
        for (String name : config.getTouchButtonNames()) {
            FadeImage buttonImage = buttonImages.get(name);
            Vector2 defaultPosition = config.getDefaultTouchPositions().get(name);
            float x = defaultPosition.x;
            float y = defaultPosition.y;
            if (x < 0) {
                x += stage.getWidth();
            }
            buttonImage.setPosition(x, y);
        }
    }
*/
    private void save() {
        for (String name : config.getTouchButtonNames()) {
            Vector2 position = buttonPositions.get(name);
            settings.setTouchPosition(name, position.x, position.y);
        }
        settings.save();
    }

    @Override
    public void moveIn() {
        moving = true;
        moveInActor(dragLabel, -stage.getWidth()/2 - 500f, 0.0f);
        //moveInActor(resetButton, -stage.getWidth()/2 - 500f, 0.10f);
        moveInActor(switchSidesButton, -stage.getWidth()/2 - 500f, 0.10f);
        for (FadeImage button : buttonImages.values()) {
            button.getColor().a = 0;
            button.addAction(Actions.fadeIn(0.25f, Interpolation.sineIn));
        }

        jumpingLabel.getColor().a = 0;
        jumpingLabel.addAction(Actions.fadeIn(0.25f, Interpolation.sineIn));
        jumpingBg.getColor().a = 0;
        jumpingBg.addAction(Actions.fadeIn(0.25f, Interpolation.sineIn));
        movingLabel.getColor().a = 0;
        movingLabel.addAction(Actions.fadeIn(0.25f, Interpolation.sineIn));
        movingBg.getColor().a = 0;
        movingBg.addAction(Actions.fadeIn(0.25f, Interpolation.sineIn));

        stage.addAction(Actions.sequence(
            Actions.delay(0.26f),
            movingFinishedAction
        ));
    }

    private void moveInActor(Actor actor, float x, float delay) {
        actor.setX(x);
        actor.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.moveTo(xPositions.get(actor), actor.getY(),0.15f, Interpolation.sineOut)
        ));
    }

    private void moveOut(Action endAction) {
        if (isAnimating()) { return; }
        moving = true;
        moveOutActor(dragLabel, stage.getWidth()/2 + 100f, 0.0f);
        //moveOutActor(resetButton, stage.getWidth()/2 + 100f, 0.10f);
        moveOutActor(switchSidesButton, stage.getWidth()/2 + 100f, 0.10f);
        for (FadeImage button : buttonImages.values()) {
            button.addAction(Actions.fadeOut(0.25f, Interpolation.sineOut));
        }
        jumpingLabel.addAction(Actions.fadeOut(0.25f, Interpolation.sineOut));
        jumpingBg.addAction(Actions.fadeOut(0.25f, Interpolation.sineOut));
        movingLabel.addAction(Actions.fadeOut(0.25f, Interpolation.sineOut));
        movingBg.addAction(Actions.fadeOut(0.25f, Interpolation.sineOut));

        stage.addAction(Actions.sequence(
            Actions.delay(0.26f),
            movingFinishedAction,
            endAction
        ));
    }

    private void moveOutActor(Actor actor, float x, float delay) {
        actor.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.moveTo(x, actor.getY(),0.15f, Interpolation.sineIn)
        ));
    }

}
