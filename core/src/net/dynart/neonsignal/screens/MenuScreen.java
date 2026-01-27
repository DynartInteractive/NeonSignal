package net.dynart.neonsignal.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.dynart.neonsignal.VersionUtil;
import net.dynart.neonsignal.core.listeners.DialogListener;
import net.dynart.neonsignal.core.Screen;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.Styles;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.ui.MenuBackground;

abstract class MenuScreen extends Screen implements DialogListener {

    private Label versionLabel;

    protected MenuButton backButton;

    boolean moving;
    Action movingFinishedAction;
    Styles styles;
    DialogScreen dialogScreen;
    Group group;
    Skin skin;
    MenuCursor menuCursor;

    MenuScreen(Engine engine) {
        super(engine);
        styles = engine.getStyles();
        skin = engine.getTextureManager().getSkin("ui");
        movingFinishedAction = new Action() {
            @Override
            public boolean act(float delta) {
            moving = false;
            return true;
            }
        };
        menuCursor = new MenuCursor(engine);
        group = new Group();
        stage.addActor(group);

        backButton = new MenuButton(engine, "");
        backButton.setWidth(110);
        backButton.setHeight(110);
        backButton.setY(220);
        backButton.setIcon(new Image(skin.getDrawable("icon_back")));
        backButton.addListener(new ClickListener () {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                backClicked();
            }
        });

        createVersionLabel();
    }

    protected boolean isAnimating() {
        return moving || fade.isActive();
    }

    private void createVersionLabel() {
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = new BitmapFont();
        ls.font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        versionLabel = new Label(config.getName() + " " + VersionUtil.getVersion(), ls);
        stage.addActor(versionLabel);
    }

    @Override
    public void init() {
        addBackground();
        dialogScreen = (DialogScreen)engine.getScreen("dialog");
    }

    protected void addBackground() {
        MenuBackground background = new MenuBackground(engine);
        stage.addActor(background);
        background.toBack();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        versionLabel.setX(5);
        versionLabel.setY(stage.getHeight() - versionLabel.getHeight() - 5f);

        float sideBlackBarWidth = getSideBlackBarWidth();
        backButton.setX(stage.getWidth() / 2f - 130 - sideBlackBarWidth);

        group.setX(stage.getWidth() / 2f);
        group.setY(stage.getHeight() / 2f);
        Gdx.input.setCursorCatched(false);
    }

    public void backClicked() {
    }

    @Override
    public void show() {
        dialogScreen.setBackScreen(this);
        dialogScreen.setListener(this);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        menuCursor.update();
        if (engine.getGameController().isMenuPressed() || engine.getGameController().isBPressed()) {
            backClicked();
        }
    }

    @Override
    public void dialogButtonClicked(int index) {}
}
