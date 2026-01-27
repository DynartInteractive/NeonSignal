package net.dynart.neonsignal.screens;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.dynart.neonsignal.core.controller.Button;
import net.dynart.neonsignal.core.ui.FadeImage;
import net.dynart.neonsignal.core.ui.FadeToAction;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;
import net.dynart.neonsignal.core.Engine;

public class PauseScreen extends MenuScreen {

    private static final int BUTTON_WIDTH = 360;
    private static final int BUTTON_HEIGHT = 120;
    private static final int FIRST_COLUMN = -364;
    private static final int SECOND_COLUMN = 5;
    private static final int FIRST_ROW = -50;
    private static final int SECOND_ROW = -178;

    enum DialogType {
        REPLAY, EXIT
    }

    private final Table table;

    private GameScreen gameScreen;
    private FadeImage bgImage;
    private MenuButton continueButton;
    private MenuButton settingsButton;
    private MenuButton replayButton;
    private MenuButton exitButton;
    private DialogType dialogType;
    private Action continueAction;
    private Action settingsAction;
    private Action replayAction;
    private Action exitAction;
    private float tableY;
    private float tableX;

    public PauseScreen(final Engine engine) {
        super(engine);

        clear = false;

        table = new Table();
        table.setBackground(skin.getDrawable("header_bg"));
        table.setHeight(150);
        group.addActor(table);

        createActions();
        setUpButtons();
        setUpCursor();

        group.addActor(menuCursor.getCursorImage());
        group.addActor(continueButton);
        group.addActor(settingsButton);
        group.addActor(replayButton);
        group.addActor(exitButton);

        addSideBlackBars(stage);

        // if we hit "START" or "B" button, means: continue
        stage.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == engine.getGameController().getKeyCode(Button.MENU)
                        || keycode == engine.getGameController().getKeyCode(Button.B)) {
                    moveOut(continueAction);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void setUpButtons() {
        continueButton = new MenuButton(engine, "Continue");
        continueButton.setWidth(BUTTON_WIDTH);
        continueButton.setHeight(BUTTON_HEIGHT);
        continueButton.setPosition(-FIRST_COLUMN, FIRST_ROW);
        continueButton.setIcon(new Image(skin.getDrawable("icon_play")));
        continueButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                moveOut(continueAction);
            }
        });

        settingsButton = new MenuButton(engine, "Settings");
        settingsButton.setWidth(BUTTON_WIDTH);
        settingsButton.setHeight(BUTTON_HEIGHT);
        settingsButton.setPosition(SECOND_COLUMN, FIRST_ROW);
        settingsButton.setIcon(new Image(skin.getDrawable("icon_settings")));
        settingsButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                settingsClicked();
            }
        });

        replayButton = new MenuButton(engine, "Replay");
        replayButton.setWidth(BUTTON_WIDTH);
        replayButton.setHeight(BUTTON_HEIGHT);
        replayButton.setPosition(FIRST_COLUMN, SECOND_ROW);
        replayButton.setIcon(new Image(skin.getDrawable("icon_replay")));
        replayButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                replayClicked();
            }
        });

        exitButton = new MenuButton(engine, "Exit");
        exitButton.setWidth(BUTTON_WIDTH);
        exitButton.setHeight(BUTTON_HEIGHT);
        exitButton.setPosition(SECOND_COLUMN, SECOND_ROW);
        exitButton.setIcon(new Image(skin.getDrawable("icon_exit")));
        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                exitClicked();
            }
        });
    }

    private void setUpCursor() {
        MenuCursorItem item;
        item = menuCursor.addItem(continueButton);
        item.setNeighbour(MenuCursor.Neighbour.RIGHT, settingsButton);
        item.setNeighbour(MenuCursor.Neighbour.DOWN, replayButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                moveOut(continueAction);
            }
        });
        item = menuCursor.addItem(settingsButton);
        item.setNeighbour(MenuCursor.Neighbour.LEFT, continueButton);
        item.setNeighbour(MenuCursor.Neighbour.DOWN, exitButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                settingsClicked();
            }
        });
        item = menuCursor.addItem(replayButton);
        item.setNeighbour(MenuCursor.Neighbour.RIGHT, exitButton);
        item.setNeighbour(MenuCursor.Neighbour.UP, continueButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                replayClicked();
            }
        });
        item = menuCursor.addItem(exitButton);
        item.setNeighbour(MenuCursor.Neighbour.LEFT, replayButton);
        item.setNeighbour(MenuCursor.Neighbour.UP, settingsButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                exitClicked();
            }
        });
    }

    private void createActions() {
        exitAction = new FadeToAction(engine, "levels");
        settingsAction = new Action() {
            @Override
            public boolean act(float delta) {
                SettingsScreen settingsScreen = (SettingsScreen)engine.getScreen("settings");
                settingsScreen.setBackToMenu(false);
                settingsScreen.resetActorPositions();
                settingsScreen.fadeIn();
                engine.setScreen("settings");
                return true;
            }
        };
        replayAction = new Action() {
            @Override
            public boolean act(float delta) {
                gameScreen.loadLevel(gameScreen.getCurrentLevel());
                gameScreen.fadeIn();
                engine.setScreen("game");
                return true;
            }
        };
        continueAction = new Action() {
            @Override
            public boolean act(float delta) {
                engine.setScreen("game");
                return true;
            }
        };
    }

    @Override
    public void init() {
        super.init();
        gameScreen = (GameScreen)engine.getScreen("game");
    }

    protected void addBackground() {
        bgImage = new FadeImage(skin.getDrawable("dialog_fix_bg"));
        stage.addActor(bgImage);
        bgImage.toBack();
        // if we click the background image means: continue
        bgImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                moveOut(continueAction);
            }
        });
    }

    @Override
    public void draw() {
        gameScreen.updateCamera();
        gameScreen.draw();
        super.draw();
    }

    @Override
    public void resize(int width, int height) {
        gameScreen.resize(width, height);
        super.resize(width, height);
        bgImage.setSize(stage.getWidth(), stage.getHeight());
        table.setWidth(stage.getWidth());
        tableX = -stage.getWidth() / 2;
        table.setX(tableX);
        tableY = (-table.getHeight()) / 2f - 50;
        table.setY(tableY);
    }

    private void prepareDialog() {
        dialogScreen.setHeight(200);
        dialogScreen.setText("Are you sure?");
        dialogScreen.setButtonCount(2);
        dialogScreen.setButtonText(0, "Cancel");
        engine.moveToScreen("dialog");
    }

    private void replayClicked() {
        if (isAnimating()) { return; }
        prepareDialog();
        dialogType = DialogType.REPLAY;
        dialogScreen.setButtonText(1, "Replay");
    }

    private void exitClicked() {
        if (isAnimating()) { return; }
        prepareDialog();
        dialogType = DialogType.EXIT;
        dialogScreen.setButtonText(1, "Exit");
    }

    @Override
    public void dialogButtonClicked(int index) {
        if (index == 1 && dialogType == DialogType.REPLAY) {
            dialogScreen.fadeOut(replayAction);
        }
        else if (index == 1 && dialogType == DialogType.EXIT) {
            dialogScreen.fadeOut(exitAction);
        } else {
            dialogScreen.moveOut();
        }
    }

    private void settingsClicked() {
        if (isAnimating()) { return; }
        fadeOut(settingsAction);
    }

    @Override
    public void moveIn() {
        if (isAnimating()) { return; }
        moving = true;
        bgImage.addAction(Actions.fadeIn(0.15f));
        float left = -group.getX() - continueButton.getWidth() - 1f;
        float right = group.getX() + continueButton.getWidth() + 1f;
        moveInButton(continueButton, left, FIRST_COLUMN, 0.05f);
        moveInButton(settingsButton, right, SECOND_COLUMN, 0.05f);
        moveInButton(replayButton, left, FIRST_COLUMN, 0.1f);
        moveInButton(exitButton, right, SECOND_COLUMN, 0.1f);
        table.setPosition(tableX, -(stage.getHeight()/2 + table.getHeight()));
        table.addAction(Actions.moveTo(tableX, tableY, 0.15f));
        stage.addAction(Actions.sequence(Actions.delay(0.31f), movingFinishedAction));
    }

    public void resetMenuCursor() {
        menuCursor.setCurrentItem(continueButton);
    }

    private void moveOut(Action endAction) {
        if (isAnimating()) { return; }
        bgImage.addAction(Actions.fadeOut(0.15f));
        moving = true;
        float left = -group.getX() - continueButton.getWidth() - 1f;
        float right = group.getX() + continueButton.getWidth() + 1f;
        moveOutButton(continueButton, left, 0.05f);
        moveOutButton(settingsButton, right, 0.05f);
        moveOutButton(replayButton, left, 0.1f);
        moveOutButton(exitButton, right, 0.1f);
        table.addAction(Actions.moveTo(tableX, -(stage.getHeight()/2 + table.getHeight()), 0.15f));
        stage.addAction(Actions.sequence(Actions.delay(0.31f), movingFinishedAction, endAction));
    }

    private void moveInButton(MenuButton button, float startPos, float endPos, float delay) {
        button.setX(startPos);
        button.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.moveTo(endPos, button.getY(), 0.20f, Interpolation.sineOut)
        ));
    }

    private void moveOutButton(MenuButton button, float endPos, float delay) {
        button.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.moveTo(endPos, button.getY(), 0.20f, Interpolation.sineIn)
        ));
    }

}
