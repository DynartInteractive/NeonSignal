package net.dynart.neonsignal.core.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import net.dynart.neonsignal.core.SoundManager;
import net.dynart.neonsignal.core.Engine;

public class MenuButton extends TextButton {

    private final SoundManager soundManager;
    private final MenuButtonStyle style;
    private final MenuButtonStyle pushedStyle;

    private boolean wasPressed;
    private Image icon;
    private boolean pushed;

    public MenuButton(Engine engine, String text) {
        this(engine, text,
            engine.getStyles().getDefaultButtonStyle(),
            engine.getStyles().getDefaultButtonPushedStyle()
        );
    }

    public MenuButton(Engine engine, String text, MenuButtonStyle style) {
        this(engine, text, style,
            engine.getStyles().getDefaultButtonPushedStyle()
        );
    }

    public MenuButton(Engine engine, String text, MenuButtonStyle style, MenuButtonStyle pushedStyle) {
        super(text, style);
        soundManager = engine.getSoundManager();
        this.style = style;
        this.pushedStyle = pushedStyle;
    }

    public void setIcon(Image icon) {
        if (this.icon != null) {
            this.icon.remove();
        }
        this.icon = icon;
        Cell<Label> cell = getLabelCell();
        Label label = cell.getActor();
        cell.padLeft(icon.getWidth());
        label.setAlignment(Align.left);
        Table table = cell.getTable();
        table.addActor(icon);
        icon.setY((int)((table.getHeight() - icon.getHeight()) / 2f + 4f));
        if (label.getText().toString().equals("")) {
            icon.setX((int)((table.getWidth() - icon.getWidth()) / 2f));
        } else {
            icon.setX(15f);
            cell.padLeft(icon.getWidth());
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (isDisabled()) {
            return;
        }
        if (style.soundName.equals("")) {
            return;
        }
        if (isPressed() && !wasPressed) {
            soundManager.play(style.soundName);
        }
        wasPressed = isPressed();
    }

    void push() {
        if (getStyle() != pushedStyle) {
            setStyle(pushedStyle);
        }
        if (!pushed) {
            soundManager.play(style.soundName);
        }
        pushed = true;
    }

    void release() {
        pushed = false;
        if (getStyle() != style) {
            setStyle(style);
        }
    }

}

