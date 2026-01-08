package net.dynart.neonsignal.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import net.dynart.neonsignal.core.controller.GameController;
import net.dynart.neonsignal.core.script.SkippableCommand;
import net.dynart.neonsignal.core.script.ScriptLoader;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.FontManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.TextureManager;
import net.dynart.neonsignal.core.script.Command;
import net.dynart.neonsignal.core.script.SequenceCommand;

import java.util.ArrayList;
import java.util.List;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.ui.FadeImage;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;

public class CutsceneScreen extends MenuScreen {

    public static final float FADE_MAX_TIME = 0.3f;
    public static final float FADE_OUT_WAIT_MAX_TIME = 2f;
    public static final float BAR_HEIGHT = 100;

    private final GameController gameController;
    private final ScriptLoader scriptLoader;

    private GameScreen gameScreen;
    private GameScene gameScene;
    private EntityManager entityManager;
    private Action moveOutEndAction;
    private Action fadeOutEndAction;
    private Action sayEndAction;
    private boolean sayFinished;

    // text bubble
    private final Group textBubble;
    private final Image textBubbleBg;
    private final List<Label> textBubbleLabels = new ArrayList<>();
    private final Label.LabelStyle textBubbleLs;
    private final SequenceCommand commands = new SequenceCommand();
    private Command endCommand;
    private boolean requestEnd;
    private float fadeTime = 0;
    private float fadeOutWaitTime = 0;
    private boolean fadeOut = false;
    private boolean canSkip = false;

    private final Image topBar;
    private final Image bottomBar;

    protected final MenuButton skipButton;

    private final FadeImage characterImage;

    private final TextureManager textureManager;

    public CutsceneScreen(Engine engine) {
        super(engine);

        clear = false;

        scriptLoader = engine.getScriptLoader();
        gameController = engine.getGameController();

        textureManager = engine.getTextureManager();
        FontManager fontManager = engine.getFontManager();

        // text bubble
        textBubbleBg = new Image(skin.getDrawable("text_bubble"));
        textBubbleBg.setWidth(510);
        textBubbleBg.setHeight(153);

        textBubbleLs = new Label.LabelStyle();
        textBubbleLs.font = fontManager.get("text_bubble");

        textBubble = new Group();
        textBubble.addActor(textBubbleBg);
        textBubble.setY(720 - 315);

        float lineHeight = textBubbleLs.font.getLineHeight();
        for (int i = 0; i < 3; i++) {
            Label tbLabel = new Label("", textBubbleLs);
            tbLabel.setColor(0.05f, 0.05f, 0.05f, 1);
            tbLabel.setAlignment(Align.bottom);
            tbLabel.setWidth(textBubbleBg.getWidth());
            tbLabel.setHeight(lineHeight);
            textBubbleLabels.add(tbLabel);
            textBubble.addActor(tbLabel);
        }

        // black bars
        topBar = new Image(textureManager.getTexture("black"));
        topBar.setHeight(BAR_HEIGHT);
        bottomBar = new Image(textureManager.getTexture("black"));
        bottomBar.setHeight(BAR_HEIGHT);

        stage.addActor(topBar);
        stage.addActor(bottomBar);

        addSideBlackBars(stage);

        stage.addActor(textBubble);

        characterImage = new FadeImage(textureManager.getTexture("coolfox"));
        characterImage.setY(-40);
        stage.addActor(characterImage);

        skipButton = new MenuButton(engine, "Skip");
        skipButton.setWidth(240);
        skipButton.setHeight(120);
        skipButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                skipClicked();
            }
        });

        stage.addActor(menuCursor.getCursorImage());
        stage.addActor(skipButton);

        setUpCursor();
    }

    public boolean isAnimationFinished() {
        return engine.getCurrentScreen() == this && !entityManager.isInAnimation();
    }

    private void finishCommands() {
        entityManager.setInAnimation(false);

        if (requestEnd) { // it was skipped
            for (SkippableCommand skippableCommand : scriptLoader.getSkippableCommandList()) {
                skippableCommand.skip();
            }
            while (!commands.act(1)) {
                // finish the sequence
            }
        }

    }

    @Override
    public void init() { // do not call the parent init!
        gameScreen = (GameScreen)engine.getScreen("game");
        gameScene = gameScreen.getScene();
        entityManager = gameScene.getEntityManager();

        endCommand = delta -> {
            if (!requestEnd) {
                moveOut();
            }
            return true;
        };

        moveOutEndAction = new Action() {
            @Override
            public boolean act(float delta) {
                finishCommands();
                engine.moveToScreen("game");
                return true;
            }
        };

        fadeOutEndAction = new Action() {
            @Override
            public boolean act(float delta) {
                finishCommands();
                engine.setScreen(gameScreen);
                gameScreen.fadeIn();
                return true;
            }
        };

        sayEndAction = new Action() {
            @Override
            public boolean act(float delta) {
                sayFinished = true;
                return true;
            }
        };

    }

    public boolean isSayFinished() {
        return sayFinished || isAnimationFinished();
    }

    @Override
    public void show() {
        // do not call the parent show!
        requestEnd = false;

        fadeTime = 0;
        fadeOutWaitTime = 0;

        skipButton.setVisible(false);
        menuCursor.setDisabled(true);
        menuCursor.setGlobalAlpha(0);
        Color c = skipButton.getColor(); // flash can happen, reset the alpha!
        c.a = 0;
        skipButton.setColor(c);

        canSkip = false;

        setCharacterVisible(false);
        setTextBubbleVisible(false);
        characterImage.clearActions();
        textBubble.clearActions();
        for (Label l : textBubbleLabels) {
            l.clearActions();
        }
        adjustSkipButton();
    }

    private void setUpCursor() {
        MenuCursorItem item;
        item = menuCursor.addItem(skipButton);
        item.setListener(MenuCursor.Event.ENTER, item1 -> skipClicked());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        bottomBar.setWidth(stage.getWidth());
        topBar.setWidth(stage.getWidth());
        adjustSkipButton();
    }

    public void skipClicked() {
        requestEnd = true;
        fadeOut(fadeOutEndAction);
    }

    private void adjustSkipButton() {
        skipButton.setX(stage.getWidth() - skipButton.getWidth() - getSideBlackBarWidth() - 20);
        skipButton.setY(stage.getHeight() - skipButton.getHeight() - 20);
    }

    private float getCharacterImageX(boolean left) {
        return left
            ? getSideBlackBarWidth() - 50 + characterImage.getWidth()
            : stage.getWidth() - getSideBlackBarWidth() - characterImage.getWidth() + 50;
    }

    private float getTextBubbleX(boolean left) {
        return left
            ? getSideBlackBarWidth() + 100 + textBubbleBg.getWidth()
            : stage.getWidth() - getSideBlackBarWidth() - textBubbleBg.getWidth() - 100;
    }

    @Override
    public void draw() {
        clear();
        gameScene.draw();
        super.draw();
    }

    @Override
    public void update(float delta) {

        if (fadeOutWaitTime > 0) {
            fadeOutWaitTime -= delta;
            if (fadeOutWaitTime < 0) {
                fadeTime = FADE_MAX_TIME;
                fadeOut = true;
            }
        }

        if (fadeTime > 0) {

            fadeTime -= delta;
            if (fadeTime < 0) {
                fadeTime = 0;
                canSkip = !fadeOut;
                menuCursor.setDisabled(fadeOut);
                if (fadeOut) {
                    skipButton.setVisible(false);
                    menuCursor.setDisabled(true);
                }
            }
            Color c = skipButton.getColor();
            float r = fadeTime / FADE_MAX_TIME;
            c.a = fadeOut ? r : 1f - r;
            menuCursor.setGlobalAlpha(c.a);
            skipButton.setColor(c);

        } else if (gameController.isAnyKeyPressed() && !canSkip) {

            skipButton.setVisible(true);
            menuCursor.setGlobalAlpha(0);
            Color c = skipButton.getColor(); // flash can happen, reset the alpha!
            c.a = 0;
            skipButton.setColor(c);

            fadeTime = FADE_MAX_TIME;
            fadeOutWaitTime = FADE_OUT_WAIT_MAX_TIME;
            fadeOut = false;
        }

        commands.act(delta);
        gameScene.update(delta);
        super.update(delta);
    }


    public void say(String text, String name, boolean start, boolean finish, boolean left) {

        String[] lines = text.split("\n");
        if (lines.length > 3) {
            throw new RuntimeException("Say text more than 3 lines.");
        }
        float textBubbleDelay = 1.0f + (float)lines.length * 0.4f;
        sayFinished = false;

        for (Label tbLabel : textBubbleLabels) {
            tbLabel.setText(""); // clear texts
        }

        characterImage.setDrawable(new TextureRegionDrawable(textureManager.getTexture(name)));
        characterImage.setScaleX(left ? -1 : 1);
        textBubbleBg.setScaleX(left ? -1 : 1);

        // text bubble label in/out animation
        float lineHeight = textBubbleLs.font.getLineHeight();
        float textBubbleTargetHeight = (lineHeight * (lines.length + 1) + 80f);
        textBubbleBg.setHeight(textBubbleTargetHeight);
        float y = 100f + lineHeight * (lines.length-1);
        for (int i = 0; i < lines.length; i++) {
            Label tbLabel = textBubbleLabels.get(i);
            tbLabel.setY(y);
            y -= lineHeight;
            tbLabel.setText(lines[i]);
            Color c = tbLabel.getColor();
            c.a = 0;
            tbLabel.setColor(c);
            tbLabel.setX(left ? -textBubbleBg.getWidth() - 10f : 10f);
            tbLabel.clearActions();
            tbLabel.addAction(Actions.sequence(
                Actions.delay(0.2f + (float)i * 0.2f),
                Actions.parallel(
                    Actions.moveTo(left ? -textBubbleBg.getWidth() : 0, tbLabel.getY(), 0.1f),
                    Actions.fadeIn(0.1f)
                )
            ));
        }

        // text bubble move in/out animation
        textBubble.setX(getTextBubbleX(left) + (left ? -10f : 10f));
        textBubble.clearActions();
        textBubble.addAction(
            Actions.sequence(
                Actions.delay(0.1f),
                Actions.parallel(
                    Actions.moveTo(getTextBubbleX(left), textBubble.getY(),0.2f, Interpolation.pow2Out),
                    Actions.fadeIn(0.2f, Interpolation.pow2InInverse)
                ),
                Actions.delay(textBubbleDelay),
                Actions.parallel(
                    Actions.moveTo(getTextBubbleX(left) + (left ? -10f : 10f), textBubble.getY(),0.1f, Interpolation.pow2OutInverse),
                    Actions.fadeOut(0.1f, Interpolation.pow2In)
                )
            )
        );

        // character move in/out animation
        Action startAction = Actions.parallel(
            Actions.moveTo(getCharacterImageX(left), characterImage.getY(),0.2f, Interpolation.pow2Out),
            Actions.fadeIn(0.2f, Interpolation.pow2InInverse)
        );

        Action finishAction = Actions.parallel(
            Actions.moveTo(getCharacterImageX(left) + (left ? -10f : 10f), characterImage.getY(),0.1f, Interpolation.pow2OutInverse),
            Actions.fadeOut(0.1f, Interpolation.pow2In)
        );

        characterImage.clearActions();

        if (start && !finish) {
            characterImage.setX(getCharacterImageX(left) + (left ? -10f : 10f));
            characterImage.addAction(
                Actions.sequence(startAction, Actions.delay(textBubbleDelay + 0.2f), sayEndAction)
            );
        }

        if (finish && !start) {
            characterImage.addAction(
                Actions.sequence(
                    Actions.delay(textBubbleDelay + 0.5f), finishAction, sayEndAction
                )
            );
        }

        if (start && finish) {
            characterImage.setX(getCharacterImageX(left) + (left ? -10f : 10f));
            characterImage.addAction(
                Actions.sequence(startAction, Actions.delay(textBubbleDelay + 0.2f), finishAction, sayEndAction)
            );
        }

        if (!start && !finish) {
            characterImage.addAction(
                Actions.sequence(Actions.delay(textBubbleDelay + 0.5f), sayEndAction)
            );
        }

    }

    public void setCharacterVisible(boolean value) {
        Color c = characterImage.getColor();
        c.a = value ? 1 : 0;
        characterImage.setColor(c);
    }

    public void setTextBubbleVisible(boolean value) {
        Color c = textBubble.getColor();
        c.a = value ? 1 : 0;
        textBubble.setColor(c);
    }

    public void load(String path) {
        requestEnd = false;
        List<Command> commandList = scriptLoader.load(path);
        commandList.add(endCommand);
        commands.init(commandList);
        entityManager.setInAnimation(true);
    }

    @Override
    public void moveIn() {
        if (moving) { return; }
        moving = true;
        topBar.setY(stage.getHeight());
        bottomBar.setY(-BAR_HEIGHT);
        topBar.addAction(Actions.moveTo(0, stage.getHeight() - BAR_HEIGHT, 0.28f, Interpolation.pow2InInverse));
        bottomBar.addAction(Actions.moveTo(0, 0, 0.28f, Interpolation.pow2InInverse));
        stage.addAction(Actions.sequence(Actions.delay(0.3f), movingFinishedAction));
    }

    private void moveOut() {
        moving = true;
        topBar.addAction(Actions.moveTo(0, stage.getHeight(), 0.28f, Interpolation.pow2In));
        bottomBar.addAction(Actions.moveTo(0, -BAR_HEIGHT, 0.28f, Interpolation.pow2In));
        stage.addAction(Actions.sequence(Actions.delay(0.3f), movingFinishedAction, moveOutEndAction));
    }

}
