package net.dynart.neonsignal.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.dynart.neonsignal.core.SoundManager;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.core.ui.MenuCursor;
import net.dynart.neonsignal.core.ui.MenuCursorItem;
import net.dynart.neonsignal.core.Engine;

public class MainMenuScreen extends MenuScreen {

    public static final float LOGO_Y = 140;
    private static final float GLASSES_Y = 175;

    private final SoundManager soundManager;
    private final Image logoImage;
    private final Image glassesImage;

    private MenuButton playButton ;
    private MenuButton settingsButton;
    private Action quitAction;
    private Action playAction;
    private Action settingsAction;
    private MenuButton privacyPolicyButton;
    private MenuButton creditsButton;
    private MenuCursorItem wentToBackButtonFrom;

    public MainMenuScreen(final Engine engine) {
        super(engine);

        soundManager = engine.getSoundManager();

        // logo
        //Skin uiPixelSkin = engine.getTextureManager().getSkin("ui_pixel");
        logoImage = new Image(skin.getDrawable("logo"));
        logoImage.setOrigin(Align.center);
        //logoImage.setScale(4.5f);
        logoImage.setPosition(-logoImage.getWidth()/2f - 0.01f, LOGO_Y);
        glassesImage = new Image(skin.getDrawable("logo_glasses"));
        //glassesImage.setScale(4.5f);
        glassesImage.setPosition(-206, GLASSES_Y);

        createActions();
        setUpButtons();
        setUpCursor();

        group.addActor(menuCursor.getCursorImage());
        group.addActor(logoImage);
        group.addActor(glassesImage);
        group.addActor(playButton);
        group.addActor(settingsButton);
        group.addActor(backButton);
        group.addActor(creditsButton);
        group.addActor(privacyPolicyButton);

        addSideBlackBars(stage);

        menuCursor.setCurrentItem(playButton);
    }

    public void resize(int width, int height) {
        super.resize(width, height);
    }

    private void createActions() {
        playAction = new Action() {
            @Override
            public boolean act(float delta) {
                engine.moveToScreen("levels");
                return true;
            }
        };
        settingsAction = new Action() {
            @Override
            public boolean act(float delta) {
                SettingsScreen settingsScreen = (SettingsScreen)engine.getScreen("settings");
                settingsScreen.setBackToMenu(true);
                engine.moveToScreen("settings");
                return true;
            }
        };
        quitAction = new Action() {
            @Override
            public boolean act(float delta) {
                Gdx.app.exit();
                return true;
            }
        };

    }

    private void setUpButtons() {
        playButton = new MenuButton(engine, "Play!");
        playButton.setWidth(400);
        playButton.setHeight(120);
        playButton.setY(-30);
        playButton.setX(-200);
        playButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                moveOut(playAction);
            }
        });
        playButton.setIcon(new Image(skin.getDrawable("icon_play")));

        settingsButton = new MenuButton(engine, "Settings");
        settingsButton.setWidth(400);
        settingsButton.setHeight(120);
        settingsButton.setY(-170);
        settingsButton.setX(-200);
        settingsButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                moveOut(settingsAction);
            }
        });
        settingsButton.setIcon(new Image(skin.getDrawable("icon_settings")));

        backButton.setIcon(new Image(skin.getDrawable("icon_exit")));

        creditsButton = new MenuButton(engine, "      Credits", styles.getSecondaryButtonStyle(), styles.getSecondaryButtonStyle());
        creditsButton.setWidth(380);
        creditsButton.setHeight(60);
        creditsButton.setY(-250);
        creditsButton.setX(-190);
        creditsButton.setIcon(new Image(skin.getDrawable("icon_credits")));

        privacyPolicyButton = new MenuButton(engine, "      Privacy Policy", styles.getSecondaryButtonStyle(), styles.getSecondaryButtonStyle());
        privacyPolicyButton.setWidth(380);
        privacyPolicyButton.setHeight(60);
        privacyPolicyButton.setY(-310);
        privacyPolicyButton.setX(-190);
        privacyPolicyButton.setIcon(new Image(skin.getDrawable("icon_privacy")));
        privacyPolicyButton.setVisible(config.isMobile());

    }

    private void setUpCursor() {
        MenuCursor.Listener goToBackButton = new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                wentToBackButtonFrom = item;
                menuCursor.setCurrentItem(backButton);
            }
        };

        MenuCursor.Listener backFromBackButton = new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                menuCursor.setCurrentItem(wentToBackButtonFrom);
            }
        };

        MenuCursorItem item;
        item = menuCursor.addItem(backButton);
        item.setNeighbour(MenuCursor.Neighbour.LEFT, playButton);
        item.setListener(MenuCursor.Event.DOWN, backFromBackButton);
        item.setListener(MenuCursor.Event.ENTER,  new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                menuCursor.setCurrentItem(playButton);
                backClicked();
            }
        });

        item = menuCursor.addItem(playButton);
        item.setNeighbour(MenuCursor.Neighbour.DOWN, settingsButton);
        item.setNeighbour(MenuCursor.Neighbour.RIGHT, backButton);
        item.setListener(MenuCursor.Event.UP, goToBackButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                moveOut(playAction);
            }
        });
        wentToBackButtonFrom = item;

        item = menuCursor.addItem(settingsButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                moveOut(settingsAction);
            }
        });

        item.setNeighbour(MenuCursor.Neighbour.UP, playButton);
        item.setNeighbour(MenuCursor.Neighbour.DOWN, creditsButton);

        item = menuCursor.addItem(creditsButton);
        item.setNeighbour(MenuCursor.Neighbour.UP, settingsButton);
        item.setNeighbour(MenuCursor.Neighbour.DOWN, privacyPolicyButton);

        item = menuCursor.addItem(privacyPolicyButton);
        item.setNeighbour(MenuCursor.Neighbour.UP, creditsButton);
        item.setListener(MenuCursor.Event.ENTER, new MenuCursor.Listener() {
            @Override
            public void handle(MenuCursorItem item) {
                Gdx.net.openURI("https://dynart.net/privacy-policy?app=Cool%20Fox");
            }
        });

    }

    @Override
    public void backClicked() {
        if (isAnimating()) { return; }
        dialogScreen.setHeight(200);
        dialogScreen.setText("Quit the game?");
        dialogScreen.setButtonCount(2);
        dialogScreen.setButtonText(0, "Cancel");
        dialogScreen.setButtonText(1, "OK");
        engine.moveToScreen("dialog");
    }

    @Override
    public void dialogButtonClicked(int index) {
        if (index == 1) {
            moveOut(quitAction);
        }
        dialogScreen.moveOut();
    }

    @Override
    public void moveIn() {
        if (isAnimating()) { return; }
        soundManager.playMusic("main");
        moving = true;
        logoImage.setPosition(logoImage.getX(), stage.getHeight());
        logoImage.addAction(Actions.moveTo(logoImage.getX(), LOGO_Y, 0.2f, Interpolation.pow2Out));
        glassesImage.setPosition(glassesImage.getX(), stage.getHeight());
        glassesImage.addAction(Actions.sequence(
            Actions.delay(0.2f),
            Actions.moveTo(glassesImage.getX(), GLASSES_Y, 0.2f, Interpolation.pow2Out)
        ));
        float left = -group.getX() - playButton.getWidth() - 1f;
        float right = group.getX() + playButton.getWidth() + 1f;
        moveInButton(playButton, left, 0.05f);
        moveInButton(settingsButton, right, 0.1f);
        stage.addAction(Actions.sequence(Actions.delay(0.36f), movingFinishedAction));
    }

    private void moveOut(Action endAction) {
        if (isAnimating()) { return; }
        moving = true;
        logoImage.addAction(Actions.moveTo(logoImage.getX(), stage.getHeight(), 0.2f, Interpolation.pow2In));
        glassesImage.addAction(Actions.moveTo(glassesImage.getX(), stage.getHeight(), 0.2f, Interpolation.pow2In));
        float left = -group.getX() - playButton.getWidth() - 1f;
        float right = group.getX() + playButton.getWidth() + 1f;
        moveOutButton(playButton, right, 0.05f);
        moveOutButton(settingsButton, left, 0.1f);
        stage.addAction(Actions.sequence(Actions.delay(0.36f), movingFinishedAction, endAction));
    }

    private void moveInButton(MenuButton button, float startPos, float delay) {
        button.setX(startPos);
        button.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.moveTo(-button.getWidth() / 2f, button.getY(), 0.20f, Interpolation.sineOut)
        ));
    }

    private void moveOutButton(MenuButton button, float endPos, float delay) {
        button.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.moveTo(endPos, button.getY(), 0.20f, Interpolation.sineIn)
        ));
    }

}
