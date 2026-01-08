package net.dynart.neonsignal.core.ui;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

import net.dynart.neonsignal.core.FontManager;
import net.dynart.neonsignal.core.TextureManager;
import net.dynart.neonsignal.core.Engine;

public class Styles {

    private final MenuButtonStyle defaultButtonStyle;
    private final MenuButtonStyle defaultButtonPushedStyle;
    private final MenuButtonStyle secondaryButtonStyle;
    private final Label.LabelStyle defaultLabelStyle;
    private final Label.LabelStyle secondaryLabelStyle;
    private final Slider.SliderStyle defaultSliderStyle;
    private final CheckBox.CheckBoxStyle defaultCheckboxStyle;

    public Styles(Engine engine) {
        FontManager fontManager = engine.getFontManager();
        TextureManager textureManager = engine.getTextureManager();
        Skin skin = textureManager.getSkin("ui");

        defaultButtonStyle = new MenuButtonStyle();
        defaultButtonStyle.font = fontManager.get("default");
        defaultButtonStyle.up = skin.getDrawable("button_up");
        defaultButtonStyle.down = skin.getDrawable("button_down");
        defaultButtonStyle.soundName = "button_click";
        defaultButtonStyle.unpressedOffsetY = 5;
        defaultButtonStyle.pressedOffsetY = -2;
        defaultButtonStyle.checkedOffsetY = 5;

        defaultButtonPushedStyle = new MenuButtonStyle();
        defaultButtonPushedStyle.font = fontManager.get("default");
        defaultButtonPushedStyle.up = skin.getDrawable("button_down");
        defaultButtonPushedStyle.down = skin.getDrawable("button_down");
        defaultButtonPushedStyle.soundName = "button_click";
        defaultButtonPushedStyle.unpressedOffsetY = -2;
        defaultButtonPushedStyle.pressedOffsetY = -2;
        defaultButtonPushedStyle.checkedOffsetY = -2;

        secondaryButtonStyle = new MenuButtonStyle();
        secondaryButtonStyle.font = fontManager.get("secondary");
        secondaryButtonStyle.up = skin.getDrawable("empty");
        secondaryButtonStyle.down = skin.getDrawable("empty");
        secondaryButtonStyle.soundName = "button_click";

        defaultLabelStyle = new Label.LabelStyle();
        defaultLabelStyle.font = fontManager.get("default");

        secondaryLabelStyle = new Label.LabelStyle();
        secondaryLabelStyle.font = fontManager.get("secondary");

        defaultSliderStyle = new Slider.SliderStyle();
        defaultSliderStyle.knob = skin.getDrawable("slider_knob");
        defaultSliderStyle.background = skin.getDrawable("slider_bg");

        defaultCheckboxStyle = new CheckBox.CheckBoxStyle();

    }

    public MenuButtonStyle getDefaultButtonStyle() {
        return defaultButtonStyle;
    }

    public MenuButtonStyle getDefaultButtonPushedStyle() {
        return defaultButtonPushedStyle;
    }

    public MenuButtonStyle getSecondaryButtonStyle() {
        return secondaryButtonStyle;
    }

    public Slider.SliderStyle getDefaultSliderStyle() {
        return defaultSliderStyle;
    }

    public Label.LabelStyle getDefaultLabelStyle() {
        return defaultLabelStyle;
    }

    public Label.LabelStyle getSecondaryLabelStyle() {
        return secondaryLabelStyle;
    }

    public CheckBox.CheckBoxStyle getDefaultCheckboxStyle() {
        return defaultCheckboxStyle;
    }
}
