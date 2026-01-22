package net.dynart.neonsignal.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.Styles;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Level;

public class LevelButton extends Group {

    private final Image[] starImages = new Image[3];
    private final Image lockedImage;
    private final MenuButton levelButton;
    private final Skin skin;

    public LevelButton(final Engine engine, Level level, int i) {
        Styles styles = engine.getStyles();
        skin = engine.getTextureManager().getSkin("ui");

        setWidth(127);
        setHeight(139);

        levelButton = new MenuButton(engine, String.valueOf(i), styles.getDefaultButtonStyle());
        //levelButton.setY(0);
        levelButton.setWidth(126);
        levelButton.setHeight(139);
        levelButton.setUserObject(level);
        levelButton.setVisible(false);
        addActor(levelButton);

        lockedImage = new Image(skin.getDrawable("button_locked"));
        addActor(lockedImage);

        for (int j = 0; j < 3; j++) {
            starImages[j] = new Image(skin.getDrawable("star_empty"));
            starImages[j].setX(j * 35 + 9);
            starImages[j].setY(-14);
            starImages[j].setVisible(false);
            //addActor(starImages[j]);
        }
    }

    public void addClickListener(ClickListener listener) {
        levelButton.addListener(listener);
    }

    public void setUnlocked(boolean unlocked) {
        levelButton.setVisible(unlocked);
        for (Image starImage : starImages) {
            starImage.setVisible(unlocked);
        }
        lockedImage.setVisible(!unlocked);
    }

    public void setStars(int num) {
        for (int i = 0; i < 3; i++) {
            String name = i < num ? "star_full" : "star_empty";
            starImages[i].setDrawable(skin.getDrawable(name));
        }
    }

    public void setDisabled(boolean disabled) {
        levelButton.setDisabled(disabled);
        levelButton.setTouchable(disabled ?  Touchable.disabled : Touchable.enabled);
    }

}
