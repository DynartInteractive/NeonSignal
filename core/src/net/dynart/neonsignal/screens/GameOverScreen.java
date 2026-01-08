package net.dynart.neonsignal.screens;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.dynart.neonsignal.core.ui.FadeImage;
import net.dynart.neonsignal.core.ui.FadeToAction;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;
import net.dynart.neonsignal.core.Engine;

public class GameOverScreen extends MenuScreen {

    private static final int LOGO_Y = 200;

    enum DialogType {
        REPLAY, EXIT
    }

    private final Table table;

    private GameScreen gameScreen;
    private FadeImage bgImage;
    //private Image titleImage;
    private MenuButton reviveButton;
    private MenuButton replayButton;
    private MenuButton exitButton;
    private Action replayAction;
    private Action reviveAction;
    private Action exitAction;
    private float tableY;
    private float tableX;
    private DialogType dialogType;

    public GameOverScreen(final Engine engine) {
        super(engine);

//        clear = false;

        //Skin sprites = engine.getTextureManager().getSkin("sprites");
//        titleImage = new Image(sprites.getDrawable("pause_title"));
//        titleImage.setOrigin(Align.center);
//        titleImage.setScale(4.5f);
//        titleImage.setPosition(-titleImage.getWidth()/2f - 0.01f, LOGO_Y);
        //group.addActor(titleImage);

        table = new Table();
        table.setBackground(skin.getDrawable("dialog_bg"));
        table.setHeight(150);
        group.addActor(table);

        createActions();
        setUpButtons();
        setUpCursor();

        group.addActor(menuCursor.getCursorImage());
        group.addActor(reviveButton);
        group.addActor(replayButton);
        group.addActor(exitButton);

        group.addActor(backButton);

        addSideBlackBars(stage);

    }

    private void setUpButtons() {
        reviveButton = createButton("Revive");
        reviveButton.setPosition(-344, 58);
        reviveButton.setHeight(120);
        reviveButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                reviveClicked();
            }
        });        
        replayButton = createButton("Replay");
        replayButton.setPosition(-344, -178);
        replayButton.setHeight(120);
        replayButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                replayClicked();
            }
        });
        replayButton.setIcon(new Image(skin.getDrawable("icon_replay")));
        exitButton = createButton("Exit");
        exitButton.setPosition(5, -178);
        exitButton.setHeight(120);
        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                exitClicked();
            }
        });
        exitButton.setIcon(new Image(skin.getDrawable("icon_exit")));
    }

    private void setUpCursor() {
        MenuCursorItem item;
        item = menuCursor.addItem(replayButton);
        item.setNeighbour(MenuCursor.Neighbour.RIGHT, exitButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                replayClicked();
            }
        });
        item = menuCursor.addItem(exitButton);
        item.setNeighbour(MenuCursor.Neighbour.LEFT, replayButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                exitClicked();
            }
        });
    }

    private MenuButton createButton(String name) {
        MenuButton result = new MenuButton(engine, name);
        result.setWidth(340);
        return result;
    }

    private void createActions() {
        exitAction = new FadeToAction(engine, "levels");
        replayAction = new Action() {
            @Override
            public boolean act(float delta) {
                gameScreen.loadLevel(gameScreen.getCurrentLevel());
                gameScreen.fadeIn();
                engine.setScreen("game");
                return true;
            }
        };
        reviveAction = new Action() {
            @Override
            public boolean act(float delta) {
                gameScreen.revive();
                gameScreen.fadeIn();
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


/*
    @Override
    public void draw() {
        gameScreen.updateCamera();
        gameScreen.draw();
        super.draw();
    }
*/
    @Override
    public void resize(int width, int height) {
        table.setWidth(stage.getWidth());
        tableX = -stage.getWidth() / 2;
        table.setX(tableX);
        tableY = (-table.getHeight()) / 2f - 50;
        table.setY(tableY);
        super.resize(width, height);
    }

    private void replayClicked() {
        if (isAnimating()) { return; }
        fadeOut(replayAction);
    }

    private void reviveClicked() {
        if (isAnimating()) { return; }
        fadeOut(reviveAction);
    }

    private void exitClicked() {
        if (isAnimating()) { return; }
        engine.moveToScreen("levels");
    }

    @Override
    public void moveIn() {
        if (isAnimating()) { return; }
        moving = true;
//        titleImage.setPosition(titleImage.getX(), stage.getHeight());
//        titleImage.addAction(Actions.moveTo(titleImage.getX(), LOGO_Y, 0.2f, Interpolation.pow2Out));
        float left = -group.getX() - replayButton.getWidth() - 1f;
        float right = group.getX() + replayButton.getWidth() + 1f;
        moveInButton(reviveButton, left, -344, 0.1f);
        moveInButton(replayButton, left, -344, 0.1f);
        moveInButton(exitButton, right, 5, 0.1f);
        table.setPosition(tableX, -(stage.getHeight()/2 + table.getHeight()));
        table.addAction(Actions.moveTo(tableX, tableY, 0.15f));
        stage.addAction(Actions.sequence(Actions.delay(0.31f), movingFinishedAction));
    }

    private void moveOut(Action endAction) {
        if (isAnimating()) { return; }
        moving = true;
//        titleImage.addAction(Actions.moveTo(titleImage.getX(), stage.getHeight(), 0.2f, Interpolation.pow2In));
        float left = -group.getX() - replayButton.getWidth() - 1f;
        float right = group.getX() + replayButton.getWidth() + 1f;
        moveOutButton(reviveButton, left, 0.1f);
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
