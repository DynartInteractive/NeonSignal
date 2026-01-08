package net.dynart.neonsignal.screens;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.dynart.neonsignal.core.FontManager;
import net.dynart.neonsignal.core.controller.Button;
import net.dynart.neonsignal.core.controller.GameController;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Settings;

public abstract class CustomizeButtonsScreen extends MenuScreen {

    Settings settings;
    GameController gameController;
    MenuButton menuButtonForSet;
    MenuButton menuButtonForWarning;
    Map<Button, MenuButton> menuButtonMap = new HashMap<Button, MenuButton>();
    MenuCursorItem backButtonItem;

    private Action backAction;
    private List<Actor> allActors = new ArrayList<Actor>();
    private Map<Actor, Float> xPositions = new HashMap<Actor, Float>();

    CustomizeButtonsScreen(final Engine engine) {
        super(engine);
        settings = engine.getSettings();
        gameController = engine.getGameController();
        group.addActor(menuCursor.getCursorImage());
        createBackButton(engine);
        createMenuButtons();

        addSideBlackBars(stage);

        setUpMenuCursor();
    }

    abstract void savePreferences();
    abstract void menuButtonClicked(MenuButton button);
    abstract String getControlName(Button button);

    void showDialog(String subject, MenuButton menuButton) {
        menuButtonForSet = menuButton;
        menuButtonForWarning = null;
        dialogScreen.setMenuCursorDisabled(true);
        dialogScreen.setHeight(200);
        dialogScreen.setText("Press a " + subject + " to assign!");
        dialogScreen.setButtonCount(1);
        dialogScreen.setButtonText(0, "Cancel");
        dialogScreen.setMenuCursorDisabled(true);
        engine.moveToScreen("dialog");
    }

    void showWarning(String subject) {
        dialogScreen.setMenuCursorDisabled(false);
        dialogScreen.setHeight(200);
        dialogScreen.setText("The " + subject + " is in use. Reassign?");
        dialogScreen.setButtonCount(2);
        dialogScreen.setButtonText(0, "Cancel");
        dialogScreen.setButtonText(1, "OK");
        dialogScreen.adjustLayout();
        dialogScreen.show();
    }

    @Override
    public void show() {
        super.show();
        dialogScreen.setMenuCursorDisabled(false);
    }

    private void createBackButton(final Engine engine) {
        backAction = new Action() {
            @Override
            public boolean act(float delta) {
                engine.moveToScreen("settings");
                return true;
            }
        };
        group.addActor(backButton);
    }

    @Override
    public void backClicked() {
        if (isAnimating()) { return; }
        savePreferences();
        settings.save();
        moveOut(backAction);
    }

    private void createMenuButtons() {
        float x = -280;
        float y = 140;
        for (int i = 0; i < config.getButtonOrder().length; i++) {
            createMenuButton(x, y, i);
            y -= 130;
            if (i == 3) {
                x += 480;
                y = 140;
            }
        }
    }

    private void setUpMenuCursor() {
        MenuCursor.Listener selectListener = new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                menuButtonClicked((MenuButton)item.getActor());
            }
        };

        backButtonItem = menuCursor.addItem(backButton);
        backButtonItem.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                Button[] buttonOrder = config.getButtonOrder();
                menuCursor.setCurrentItem(menuButtonMap.get(buttonOrder[0]));
                backClicked();
            }
        });

        Button[] buttonOrder = config.getButtonOrder();
        int halfLength = (int)Math.ceil((float)buttonOrder.length / 2f);
        boolean firstItem = true;
        for (int i = 0; i < buttonOrder.length; i++) {
            MenuCursorItem item = createMenuCursorItem(selectListener, halfLength, i);
            if (firstItem) {
                firstItem = false;
                menuCursor.setCurrentItem(item);
            }
        }
    }

    private MenuCursorItem createMenuCursorItem(MenuCursor.Listener selectListener, int halfLength, int i) {
        Button[] buttonOrder = config.getButtonOrder();
        Button button = buttonOrder[i];
        MenuButton menuButton = menuButtonMap.get(button);
        MenuCursorItem item = menuCursor.addItem(menuButton);
        if (i == halfLength) {
            backButtonItem.setNeighbour(MenuCursor.Neighbour.DOWN, menuButton);
            item.setNeighbour(MenuCursor.Neighbour.UP, backButton);
            backButtonItem.setNeighbour(MenuCursor.Neighbour.LEFT, menuButton);
            item.setNeighbour(MenuCursor.Neighbour.RIGHT, backButton);
        }
        else if (i == 0) {
            item.setNeighbour(MenuCursor.Neighbour.UP, backButton);
        }
        if (i < halfLength && i + halfLength < buttonOrder.length) {
            Button neighbourButton = buttonOrder[i + halfLength];
            item.setNeighbour(MenuCursor.Neighbour.RIGHT, menuButtonMap.get(neighbourButton));
        }
        if (i >= halfLength) {
            Button neighbourButton = buttonOrder[i - halfLength];
            item.setNeighbour(MenuCursor.Neighbour.LEFT, menuButtonMap.get(neighbourButton));
        }
        if (i > 0 && i != halfLength) {
            Button neighbourButton = buttonOrder[i - 1];
            item.setNeighbour(MenuCursor.Neighbour.UP, menuButtonMap.get(neighbourButton));
        }
        if (i < buttonOrder.length - 1 && i != halfLength -1) {
            Button neighbourButton = buttonOrder[i + 1];
            item.setNeighbour(MenuCursor.Neighbour.DOWN, menuButtonMap.get(neighbourButton));
        }
        item.setListener(MenuCursor.Event.ENTER, selectListener);
        return item;
    }

    private void createMenuButton(float x, float y, int index) {
        ClickListener buttonClickListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                menuButtonClicked((MenuButton)event.getListenerActor());
            }
        };
        Button[] buttonOrder = config.getButtonOrder();
        String[] buttonLabels = config.getButtonLabels();
        String name = getControlName(buttonOrder[index]);

        // button
        MenuButton menuButton = new MenuButton(engine, name, styles.getDefaultButtonStyle());
        menuButton.setUserObject(buttonOrder[index]);
        menuButton.setPosition(x, y);
        menuButton.setSize(250, 120);
        menuButton.setWidth(250);
        menuButton.setHeight(120);
        menuButton.addListener(buttonClickListener);
        menuButtonMap.put(buttonOrder[index], menuButton);

        // label
        FontManager fontManager = engine.getFontManager();
        BitmapFont font = fontManager.get("secondary");
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = font;
        Label label = new Label(buttonLabels[index], ls);
        label.setPosition(x - 170, y + 25);
        label.setSize(150, 95);
        label.setAlignment(Align.right);

        // group
        group.addActor(label);
        group.addActor(menuButton);
        addAnimatedActor(label);
        addAnimatedActor(menuButton);
    }

    private void addAnimatedActor(Actor actor) {
        allActors.add(actor);
        xPositions.put(actor, actor.getX());
    }

    @Override
    public void moveIn() {
        moving = true;
        float delay = moveInButtons();
        stage.addAction(Actions.sequence(
            Actions.sequence(Actions.delay(delay + 0.16f)),
                movingFinishedAction
        ));
    }

    private void moveOut(Action endAction) {
        moving = true;
        float delay = moveOutButtons();
        stage.addAction(Actions.sequence(
            Actions.sequence(Actions.delay(delay + 0.16f)),
                movingFinishedAction,
            endAction
        ));
    }

    private float moveInButtons() {
        float delay = 0f;
        for (int i = 0; i < allActors.size(); i++) {
            Actor actor = allActors.get(i);
            float x = xPositions.get(actor);
            if (i < 8) {
                actor.setX(x - stage.getWidth() / 2f);
            } else {
                actor.setX(x + stage.getWidth() / 2f);
            }
            if (i == 8) {
                delay = 0f;
            }
            actor.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.moveTo(x, actor.getY(), 0.15f, Interpolation.sineOut)
            ));
            if (actor instanceof Label) {
                delay += 0.025f;
            }
        }
        return delay;
    }

    private float moveOutButtons() {
        float delay = 0.0f;
        for (int i = 0; i < allActors.size(); i++) {
            Actor actor = allActors.get(i);
            float x = xPositions.get(actor);
            if (i < 8) {
                x -= stage.getWidth() / 2f;
            } else {
                x += stage.getWidth() / 2f;
            }
            if (i == 8) {
                delay = 0f;
            }
            actor.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.moveTo(x, actor.getY(), 0.15f, Interpolation.sineIn)
            ));
            if (actor instanceof Label) {
                delay += 0.025f;
            }
        }
        return delay;
    }

}
