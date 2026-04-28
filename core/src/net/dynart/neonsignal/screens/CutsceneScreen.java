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
import com.badlogic.gdx.utils.Align;

import net.dynart.lisa.core.Engine;
import net.dynart.lisa.core.FontManager;
import net.dynart.lisa.core.SoundManager;
import net.dynart.lisa.core.ui.MenuButton;
import net.dynart.lisa.core.ui.MenuCursor;
import net.dynart.lisa.core.ui.MenuCursorItem;
import net.dynart.lisa.core.ui.TypewriterAction;
import net.dynart.neonsignal.core.script.NexusSaysCommand.NexusLine;

import java.util.ArrayList;
import java.util.List;

public class CutsceneScreen extends net.dynart.lisa.screens.CutsceneScreen {

    private final FontManager fontManager;

    private final Group nexusBox;
    private final Image nexusBoxBg;
    private final List<Label> nexusLabels = new ArrayList<>();
    private final Label.LabelStyle nexusLabelStyle;
    private boolean nexusSaysFinished = true;
    private final List<TypewriterAction> activeTypewriterActions = new ArrayList<>();

    protected final MenuButton nexusButton;
    private MenuCursorItem nexusButtonItem;

    private final Image nexusDimBg;

    public CutsceneScreen(Engine engine) {
        super(engine);

        fontManager = engine.getFontManager();

        nexusBoxBg = new Image(skin.getDrawable("dialog_bg"));
        nexusBoxBg.setWidth(800);
        nexusBoxBg.setHeight(200);

        nexusLabelStyle = new Label.LabelStyle();
        nexusLabelStyle.font = fontManager.get("secondary");

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

        nexusDimBg = new Image(textureManager.getTexture("black"));
        nexusDimBg.setVisible(false);
        stage.addActor(nexusDimBg);

        stage.addActor(nexusBox);

        nexusButton = new MenuButton(engine, "");
        nexusButton.setWidth(240);
        nexusButton.setHeight(80);
        nexusButton.setVisible(false);
        nexusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                nexusButtonClicked();
            }
        });
        stage.addActor(nexusButton);

        nexusButtonItem = menuCursor.addItem(nexusButton);
        nexusButtonItem.setListener(MenuCursor.Event.ENTER, i -> nexusButtonClicked());

        // keep skip button + cursor image above the dim overlay
        skipButton.toFront();
        menuCursor.getCursorImage().toFront();
    }

    @Override
    protected boolean isInteractingWithDialog() {
        return nexusButton.isVisible();
    }

    @Override
    protected void clearDialogState() {
        nexusSaysFinished = true;
        nexusBox.setVisible(false);
        nexusBox.clearActions();
        activeTypewriterActions.clear();
        for (Label l : nexusLabels) {
            l.clearActions();
            l.setVisible(false);
            l.setText("");
        }

        nexusButton.setVisible(false);
        nexusButton.clearActions();

        nexusDimBg.setVisible(false);
        nexusDimBg.clearActions();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        nexusDimBg.setSize(stage.getWidth(), stage.getHeight());
    }

    private void nexusButtonClicked() {
        nexusButton.clearActions();
        nexusButton.addAction(Actions.fadeOut(0.2f, Interpolation.pow2In));

        nexusDimBg.clearActions();
        nexusDimBg.addAction(Actions.fadeOut(0.2f, Interpolation.pow2In));

        nexusBox.clearActions();
        nexusBox.addAction(
            Actions.sequence(
                Actions.fadeOut(0.2f, Interpolation.pow2In),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        nexusBox.setVisible(false);
                        nexusButton.setVisible(false);
                        nexusDimBg.setVisible(false);
                        nexusSaysFinished = true;

                        canSkip = false;
                        menuCursor.setDisabled(true);
                        menuCursor.setGlobalAlpha(0);
                        menuCursor.setCurrentItem(skipButton);
                        return true;
                    }
                }
            )
        );
    }

    /**
     * Display multi-line text with typewriter animation in the nexus box.
     */
    public void nexusSays(List<NexusLine> lines, float charDelay, float lineDelay, float holdTime,
        String buttonLabel) {
        nexusSaysFinished = false;
        activeTypewriterActions.clear();

        for (Label label : nexusLabels) {
            label.clearActions();
            label.setVisible(false);
            label.setText("");
        }

        nexusButton.setVisible(false);

        float paddingX = 100;
        float paddingTop = 50;
        float paddingBottom = 70;

        float[] lineHeights = new float[lines.size()];
        float totalLinesHeight = 0;
        for (int i = 0; i < lines.size(); i++) {
            NexusLine line = lines.get(i);
            float lineHeight;
            if (line.font != null) {
                lineHeight = fontManager.get(line.font).getLineHeight();
            } else {
                lineHeight = nexusLabelStyle.font.getLineHeight();
            }
            lineHeights[i] = lineHeight;
            totalLinesHeight += lineHeight + line.marginBottom;
        }

        float boxHeight = paddingTop + totalLinesHeight + paddingBottom;
        nexusBoxBg.setHeight(boxHeight);

        nexusBox.setX((stage.getWidth() - nexusBoxBg.getWidth()) / 2);
        nexusBox.setY((stage.getHeight() - boxHeight) / 2);

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

        float currentY = boxHeight - paddingTop;
        SoundManager soundManager = engine.getSoundManager();
        for (int i = 0; i < lines.size() && i < nexusLabels.size(); i++) {
            NexusLine line = lines.get(i);
            Label label = nexusLabels.get(i);

            if (line.font != null) {
                Label.LabelStyle lineStyle = new Label.LabelStyle();
                lineStyle.font = fontManager.get(line.font);
                lineStyle.font.getData().markupEnabled = true;
                label.setStyle(lineStyle);
            } else {
                label.setStyle(nexusLabelStyle);
            }

            currentY -= lineHeights[i];
            label.setX(paddingX);
            label.setWidth(nexusBoxBg.getWidth() - paddingX * 2);
            label.setY(currentY);
            label.setVisible(true);
            label.setText("");
            currentY -= line.marginBottom;

            float startDelay = lineStartTimes[i];

            TypewriterAction typewriter = new TypewriterAction(
                label, line.text, charDelay, null, soundManager, "terminal"
            );
            activeTypewriterActions.add(typewriter);

            label.addAction(
                Actions.sequence(
                    Actions.delay(startDelay),
                    typewriter
                )
            );
        }

        Color dimColor = nexusDimBg.getColor();
        dimColor.a = 0;
        nexusDimBg.setColor(dimColor);
        nexusDimBg.setVisible(true);
        nexusDimBg.addAction(Actions.alpha(0.4f, 0.2f, Interpolation.pow2Out));

        Color c = nexusBox.getColor();
        c.a = 0;
        nexusBox.setColor(c);
        nexusBox.setVisible(true);

        if (buttonLabel != null) {
            nexusButton.setText(buttonLabel);
            nexusButton
                .setX(nexusBox.getX() + (nexusBoxBg.getWidth() - nexusButton.getWidth()) / 2);
            nexusButton.setY(nexusBox.getY() - nexusButton.getHeight() - 20);

            Color btnColor = nexusButton.getColor();
            btnColor.a = 0;
            nexusButton.setColor(btnColor);

            nexusBox.addAction(
                Actions.sequence(
                    Actions.fadeIn(0.2f, Interpolation.pow2Out),
                    Actions.delay(totalTypingTime),
                    new Action() {
                        @Override
                        public boolean act(float delta) {
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
                )
            );
        } else {
            nexusBox.addAction(
                Actions.sequence(
                    Actions.fadeIn(0.2f, Interpolation.pow2Out),
                    Actions.delay(totalTypingTime + holdTime),
                    Actions.fadeOut(0.2f, Interpolation.pow2In),
                    new Action() {
                        @Override
                        public boolean act(float delta) {
                            nexusBox.setVisible(false);
                            nexusDimBg.setVisible(false);
                            nexusSaysFinished = true;
                            return true;
                        }
                    }
                )
            );
            nexusDimBg.addAction(
                Actions.sequence(
                    Actions.delay(0.2f + totalTypingTime + holdTime),
                    Actions.fadeOut(0.2f, Interpolation.pow2In)
                )
            );
        }
    }

    public boolean isNexusSaysFinished() {
        return nexusSaysFinished || isAnimationFinished();
    }

    public void skipNexusSays() {
        for (TypewriterAction action : activeTypewriterActions) {
            action.skip();
        }

        nexusButton.clearActions();
        nexusButton.setVisible(false);

        nexusDimBg.clearActions();
        nexusDimBg.addAction(Actions.fadeOut(0.1f));

        nexusBox.clearActions();
        nexusBox.addAction(
            Actions.sequence(
                Actions.fadeOut(0.1f),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        nexusBox.setVisible(false);
                        nexusDimBg.setVisible(false);
                        nexusSaysFinished = true;
                        return true;
                    }
                }
            )
        );
    }

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
