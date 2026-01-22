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
import net.dynart.neonsignal.core.ui.TypewriterAction;
import net.dynart.neonsignal.core.script.NexusSaysCommand.NexusLine;

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

    // nexus says box
    private final Group nexusBox;
    private final Image nexusBoxBg;
    private final List<Label> nexusLabels = new ArrayList<>();
    private final Label.LabelStyle nexusLabelStyle;
    private boolean nexusSaysFinished = true;
    private final List<TypewriterAction> activeTypewriterActions = new ArrayList<>();
    private Command endCommand;
    private boolean requestEnd;
    private float fadeTime = 0;
    private float fadeOutWaitTime = 0;
    private boolean fadeOut = false;
    private boolean canSkip = false;

    private final Image topBar;
    private final Image bottomBar;

    protected final MenuButton skipButton;
    protected final MenuButton nexusButton;
    private MenuCursorItem nexusButtonItem;

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

        // nexus says box
        nexusBoxBg = new Image(skin.getDrawable("dialog_bg"));
        nexusBoxBg.setWidth(800);
        nexusBoxBg.setHeight(200);

        nexusLabelStyle = new Label.LabelStyle();
        nexusLabelStyle.font = fontManager.get("text_bubble");

        nexusBox = new Group();
        nexusBox.addActor(nexusBoxBg);

        float nexusLineHeight = nexusLabelStyle.font.getLineHeight();
        for (int i = 0; i < 10; i++) {
            Label nexusLabel = new Label("", nexusLabelStyle);
            nexusLabel.getStyle().font.getData().markupEnabled = true;
            nexusLabel.setColor(0.9f, 0.9f, 0.9f, 1);
            nexusLabel.setAlignment(Align.left);
            nexusLabel.setWidth(nexusBoxBg.getWidth() - 40);
            nexusLabel.setHeight(nexusLineHeight);
            nexusLabel.setX(20);
            nexusLabel.setVisible(false);
            nexusLabels.add(nexusLabel);
            nexusBox.addActor(nexusLabel);
        }

        nexusBox.setVisible(false);

        // black bars
        topBar = new Image(textureManager.getTexture("black"));
        topBar.setHeight(BAR_HEIGHT);
        bottomBar = new Image(textureManager.getTexture("black"));
        bottomBar.setHeight(BAR_HEIGHT);

        stage.addActor(topBar);
        stage.addActor(bottomBar);

        addSideBlackBars(stage);

        stage.addActor(textBubble);
        stage.addActor(nexusBox);

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

        nexusButton = new MenuButton(engine, "");
        nexusButton.setWidth(240);
        nexusButton.setHeight(80);
        nexusButton.setVisible(false);
        nexusButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                nexusButtonClicked();
            }
        });

        stage.addActor(menuCursor.getCursorImage());
        stage.addActor(skipButton);
        stage.addActor(nexusButton);

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

        // Reset nexus box state
        nexusSaysFinished = true;
        nexusBox.setVisible(false);
        nexusBox.clearActions();
        activeTypewriterActions.clear();
        for (Label l : nexusLabels) {
            l.clearActions();
            l.setVisible(false);
            l.setText("");
        }

        // Reset nexus button state
        nexusButton.setVisible(false);
        nexusButton.clearActions();

        adjustSkipButton();
    }

    private void setUpCursor() {
        MenuCursorItem item;
        item = menuCursor.addItem(skipButton);
        item.setListener(MenuCursor.Event.ENTER, item1 -> skipClicked());

        nexusButtonItem = menuCursor.addItem(nexusButton);
        nexusButtonItem.setListener(MenuCursor.Event.ENTER, item1 -> nexusButtonClicked());
    }

    private void nexusButtonClicked() {
        nexusButton.clearActions();
        nexusButton.addAction(Actions.fadeOut(0.2f, Interpolation.pow2In));

        nexusBox.clearActions();
        nexusBox.addAction(Actions.sequence(
            Actions.fadeOut(0.2f, Interpolation.pow2In),
            new Action() {
                @Override
                public boolean act(float delta) {
                    nexusBox.setVisible(false);
                    nexusButton.setVisible(false);
                    nexusSaysFinished = true;

                    // Re-enable skip button for next command
                    canSkip = false;
                    menuCursor.setDisabled(true);
                    menuCursor.setGlobalAlpha(0);
                    menuCursor.setCurrentItem(skipButton);
                    return true;
                }
            }
        ));
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

        } else if (gameController.isAnyKeyPressed() && !canSkip && !nexusButton.isVisible()) {

            skipButton.setVisible(true);
            menuCursor.setGlobalAlpha(0);
            Color c = skipButton.getColor(); // flash can happen, reset the alpha!
            c.a = 0;
            skipButton.setColor(c);

            fadeTime = FADE_MAX_TIME;
            fadeOutWaitTime = FADE_OUT_WAIT_MAX_TIME;
            fadeOut = false;

            menuCursor.setCurrentItem(skipButton);
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

    /**
     * Display multi-line text with typewriter animation in the nexus box.
     *
     * @param lines List of NexusLine objects containing text and per-line delays
     * @param charDelay Seconds between characters during typewriter effect
     * @param lineDelay Default delay before each line starts (if line.delay is 0)
     * @param holdTime Seconds to display after typing completes
     * @param buttonLabel Label for a button to show after animation (null for auto-finish)
     */
    public void nexusSays(List<NexusLine> lines, float charDelay, float lineDelay, float holdTime, String buttonLabel) {
        nexusSaysFinished = false;
        activeTypewriterActions.clear();

        // Clear previous labels
        for (Label label : nexusLabels) {
            label.clearActions();
            label.setVisible(false);
            label.setText("");
        }

        // Hide nexus button initially
        nexusButton.setVisible(false);

        // Calculate box height based on number of lines (add space for button if needed)
        float nexusLineHeight = nexusLabelStyle.font.getLineHeight();
        float padding = 40;
        float buttonSpace = (buttonLabel != null) ? nexusButton.getHeight() + 20 : 0;
        float boxHeight = padding + lines.size() * nexusLineHeight + buttonSpace + padding;
        nexusBoxBg.setHeight(boxHeight);

        // Position box centered horizontally, lower third of screen
        nexusBox.setX((stage.getWidth() - nexusBoxBg.getWidth()) / 2);
        nexusBox.setY(150);

        // Calculate total animation time for scheduling fadeout
        float totalTypingTime = 0;
        float[] lineStartTimes = new float[lines.size()];

        for (int i = 0; i < lines.size(); i++) {
            NexusLine line = lines.get(i);
            float lineDelayTime = (line.delay > 0) ? line.delay : (i > 0 ? lineDelay : 0);
            lineStartTimes[i] = totalTypingTime + lineDelayTime;

            int visibleChars = countVisibleChars(line.text);
            float typingDuration = visibleChars * charDelay;
            totalTypingTime = lineStartTimes[i] + typingDuration;
        }

        // Setup labels and typewriter actions
        float topY = boxHeight - padding - nexusLineHeight;
        for (int i = 0; i < lines.size() && i < nexusLabels.size(); i++) {
            NexusLine line = lines.get(i);
            Label label = nexusLabels.get(i);
            label.setY(topY - i * nexusLineHeight);
            label.setVisible(true);
            label.setText("");

            float startDelay = lineStartTimes[i];

            TypewriterAction typewriter = new TypewriterAction(label, line.text, charDelay, null);
            activeTypewriterActions.add(typewriter);

            label.addAction(Actions.sequence(
                Actions.delay(startDelay),
                typewriter
            ));
        }

        // Fade in the box
        Color c = nexusBox.getColor();
        c.a = 0;
        nexusBox.setColor(c);
        nexusBox.setVisible(true);

        if (buttonLabel != null) {
            // Setup button to appear after typing completes
            nexusButton.setText(buttonLabel);
            nexusButton.setX(nexusBox.getX() + (nexusBoxBg.getWidth() - nexusButton.getWidth()) / 2);
            nexusButton.setY(nexusBox.getY() + padding);

            Color btnColor = nexusButton.getColor();
            btnColor.a = 0;
            nexusButton.setColor(btnColor);

            // Schedule button appearance and set cursor to it
            nexusBox.addAction(Actions.sequence(
                Actions.fadeIn(0.2f, Interpolation.pow2Out),
                Actions.delay(totalTypingTime),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        // Hide skip button while nexus button is active
                        skipButton.setVisible(false);
                        canSkip = false;

                        nexusButton.setVisible(true);
                        nexusButton.addAction(Actions.fadeIn(0.2f, Interpolation.pow2Out));
                        menuCursor.setDisabled(false);
                        menuCursor.setGlobalAlpha(1f);
                        menuCursor.setCurrentItem(nexusButtonItem);
                        return true;
                    }
                }
            ));
        } else {
            // Schedule fade out and completion (original behavior)
            nexusBox.addAction(Actions.sequence(
                Actions.fadeIn(0.2f, Interpolation.pow2Out),
                Actions.delay(totalTypingTime + holdTime),
                Actions.fadeOut(0.2f, Interpolation.pow2In),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        nexusBox.setVisible(false);
                        nexusSaysFinished = true;
                        return true;
                    }
                }
            ));
        }
    }

    /**
     * Check if the nexusSays animation has finished.
     */
    public boolean isNexusSaysFinished() {
        return nexusSaysFinished || isAnimationFinished();
    }

    /**
     * Skip the nexusSays animation and immediately hide the box.
     */
    public void skipNexusSays() {
        // Skip all typewriter animations
        for (TypewriterAction action : activeTypewriterActions) {
            action.skip();
        }

        // Hide the nexus button
        nexusButton.clearActions();
        nexusButton.setVisible(false);

        // Clear existing actions and fade out quickly
        nexusBox.clearActions();
        nexusBox.addAction(Actions.sequence(
            Actions.fadeOut(0.1f),
            new Action() {
                @Override
                public boolean act(float delta) {
                    nexusBox.setVisible(false);
                    nexusSaysFinished = true;
                    return true;
                }
            }
        ));
    }

    /**
     * Count visible characters in text, excluding markup tags like [color] and [/].
     */
    private int countVisibleChars(String text) {
        int count = 0;
        boolean inTag = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '[') {
                    count++;
                    i++;
                } else {
                    inTag = true;
                }
            } else if (c == ']' && inTag) {
                inTag = false;
            } else if (!inTag) {
                count++;
            }
        }
        return count;
    }

}
