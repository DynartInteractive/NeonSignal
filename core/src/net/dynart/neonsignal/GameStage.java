package net.dynart.neonsignal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.HealthComponent;
import net.dynart.neonsignal.components.PlayerComponent;
import net.dynart.neonsignal.components.ViewComponent;
import net.dynart.neonsignal.core.controller.ControllerType;
import net.dynart.neonsignal.core.TextureManager;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.FontManager;
import net.dynart.neonsignal.core.MessageHandler;
import net.dynart.neonsignal.core.listeners.MessageListener;
import net.dynart.neonsignal.core.ui.FadeImage;
import net.dynart.neonsignal.core.ui.MenuButton;
import net.dynart.neonsignal.screens.GameScreen;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.core.Settings;

public class GameStage extends Stage {

    private final Engine engine;
    private final EngineConfig config;
    private final Settings settings;
    private final GameScreen screen;

    private HealthComponent healthComponent;
    private PlayerComponent player;
    private BodyComponent body;
    private ViewComponent view;

    private final MenuButton pauseButton;
    private final Label[] labelPool;
    private final Label[] labelShadowPool;
    private final Vector2[] labelPosition;
    private int currentLabel;

    // score
    private final MessageListener playerScoreChangedListener;
    private final Label scoreLabel;

    // floppy
    private final MessageListener playerFloppyChangedListener;
    private final Label floppyLabel;

    // health
    private final Group healthGroup;
    private static final float MAX_HEARTH_LINE_WIDTH = 118;
    private static final Color DANGER_COLOR = new Color(1, 0, 0.45f, 1);
    private static final Color WARNING_COLOR = new Color(1, 0.88f, 0, 1);
    private static final Color GOOD_COLOR = new Color(0.5f, 0.88f, 0, 1);
    private final MessageListener healthChangedListener;
    private final Image healthIconImage;
    private final Image healthLineImage;
    private final Color healthLineColor = new Color(GOOD_COLOR);
    private float health = 1;
    private float targetHealthSign = 0;
    private float targetHealth = 1;

    // floppy
    private final Group floppyGroup;
    private final Image floppyIconImage;

    // game over
    private final FadeImage whiteImage;
    private final Image playerImage;

    public GameStage(Viewport viewport, Engine engine, final GameScreen screen) {
        super(viewport);
        this.engine = engine;

        config = engine.getConfig();
        settings = engine.getSettings();

        this.screen = screen;

        FontManager fontManager = engine.getFontManager();
        BitmapFont font = fontManager.get("default");
        BitmapFont font2 = fontManager.get("secondary");

        TextureManager textureManager = engine.getTextureManager();
        Skin skin = textureManager.getSkin("ui");

        TextureAtlas spriteAtlas = textureManager.getAtlas("sprites");
        Skin spritesSkin = textureManager.getSkin("sprites");

        TextureAtlas uiPixelAtlas = textureManager.getAtlas("ui_pixel");
        Skin uiPixelSkin = textureManager.getSkin("ui_pixel");

        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = font;
        ls.font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Label.LabelStyle ls2 = new Label.LabelStyle();
        ls2.font = font2;
        ls2.font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // labels for scores
        labelPool = new Label[8];
        labelShadowPool = new Label[8];
        labelPosition = new Vector2[8];
        for (int i = 0; i < labelPool.length; i++) {
            labelPool[i] = new Label("+10", ls2);
            labelPool[i].setVisible(false);
            labelPool[i].setWidth(100);
            labelPool[i].setAlignment(Align.center);

            labelShadowPool[i] = new Label("+10", ls2);
            labelShadowPool[i].setVisible(false);
            labelShadowPool[i].getColor().r = 0.1f;
            labelShadowPool[i].getColor().g = 0.1f;
            labelShadowPool[i].getColor().b = 0.1f;
            labelShadowPool[i].setWidth(100);
            labelShadowPool[i].setAlignment(Align.center);

            labelPosition[i] = new Vector2();

            addActor(labelShadowPool[i]);
            addActor(labelPool[i]);
        }

        scoreLabel = new Label("0", ls);
        scoreLabel.setWidth(300);
        scoreLabel.setAlignment(Align.right | Align.bottom);
        addActor(scoreLabel);

        // game over
        whiteImage = new FadeImage(skin.getDrawable("white"));
        whiteImage.setVisible(false);
        addActor(whiteImage);


        playerImage = new Image(spriteAtlas.findRegion("player_pain"));
        playerImage.setVisible(false);
        playerImage.setAlign(Align.center | Align.bottom);
        playerImage.setOrigin(Align.center | Align.bottom);
        addActor(playerImage);

        // pause button
        pauseButton = new MenuButton(engine, "");
        pauseButton.setWidth(90);
        pauseButton.setHeight(110);
        pauseButton.setIcon(new Image(skin.getDrawable("icon_pause")));

        addActor(pauseButton);


        // health icon
        healthIconImage = new Image(uiPixelSkin.getDrawable("hud_hearth"));
        healthIconImage.setOrigin(Align.center);
        healthIconImage.setAlign(Align.center);

        // health bar
        Group healthBarGroup = new Group();
        healthBarGroup.setY(16);
        healthBarGroup.setX(56);

        healthLineImage = new Image(uiPixelSkin.getDrawable("hud_hearth_line"));
        healthLineImage.setX(4);
        healthLineImage.setY(8);
        healthLineImage.setWidth(MAX_HEARTH_LINE_WIDTH);
        healthLineImage.setColor(GOOD_COLOR);

        Image healthBarImage = new Image(uiPixelSkin.getDrawable("hud_hearth_bar"));
        Image healthBarBgImage = new Image(uiPixelSkin.getDrawable("hud_hearth_bar_bg"));

        healthBarGroup.addActor(healthBarBgImage);
        healthBarGroup.addActor(healthLineImage);
        healthBarGroup.addActor(healthBarImage);

        // health
        healthGroup = new Group();
        healthGroup.setY(config.getStageVirtualHeight() - 105f);
        healthGroup.setX(35 + screen.getSideBlackBarWidth());

        healthGroup.addActor(healthBarGroup);
        healthGroup.addActor(healthIconImage);

        addActor(healthGroup);

        // floppy icon
        floppyIconImage = new Image(uiPixelSkin.getDrawable("hud_floppy"));
        floppyIconImage.setOrigin(Align.center);
        floppyIconImage.setAlign(Align.center);

        // floppy label
        floppyLabel = new Label("", ls);
        floppyLabel.setAlignment(Align.bottomLeft);
        floppyLabel.setX(90);
        floppyLabel.setY(0);

        // floppy
        floppyGroup = new Group();
        floppyGroup.setY(config.getStageVirtualHeight() - 105f);
        floppyGroup.setX(270 + screen.getSideBlackBarWidth());

        floppyGroup.addActor(floppyIconImage);
        floppyGroup.addActor(floppyLabel);

        addActor(floppyGroup);


        // events

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screen.pause();
            }
        });

        healthChangedListener = new MessageListener() {
            @Override
            public void receive(Entity sender, String message) {
                updateHealth();
            }
        };

        playerScoreChangedListener = new MessageListener() {
            @Override
            public void receive(Entity sender, String message) {
                updateScore();
            }
        };

        playerFloppyChangedListener = new MessageListener() {
            @Override
            public void receive(Entity sender, String message) {
                updateFloppy();
            }
        };


    }

    public void resize() {
        float sideBlackBarWidth = screen.getSideBlackBarWidth();
        boolean showPause = settings.getControllerType() == ControllerType.TOUCH || config.isMobile();
        float scorePadding = showPause ? 142f : 38f;
        pauseButton.setVisible(showPause);
        pauseButton.setY(getHeight() - pauseButton.getHeight() - 20);
        pauseButton.setX(getWidth() - pauseButton.getWidth() - 20 - sideBlackBarWidth);
        whiteImage.setWidth(getWidth());
        whiteImage.setHeight(getHeight());
        healthGroup.setX(35 + sideBlackBarWidth);
        floppyGroup.setX(270 + sideBlackBarWidth);
        floppyLabel.setText(player.getFloppy());
        scoreLabel.setX(getWidth() - scorePadding - scoreLabel.getWidth());
        scoreLabel.setY(getHeight() - scoreLabel.getHeight() - 30);
    }

    private void updateScore() {
        scoreLabel.setText(player.getScore());
    }

    private void updateFloppy() {
        floppyLabel.setText(player.getFloppy());
        floppyIconImage.setScale(1.5f);
        floppyIconImage.addAction(Actions.scaleTo(1, 1, 0.3f));
    }


    private void updateHealth() {
        targetHealth = healthComponent.getValue() / healthComponent.getMaxValue();
        targetHealthSign = Math.signum(targetHealth - health);
        healthIconImage.setScale(1.5f);
        healthIconImage.addAction(Actions.scaleTo(1, 1, 0.3f));
    }

    public void showItemScore(float x, float y, String text, float r, float g, float b) {

        currentLabel++;
        if (currentLabel == labelPool.length) {
            currentLabel = 0;
        }

        int c = currentLabel;

        labelPosition[c].x = x;
        labelPosition[c].y = y;

        labelPool[c].clearActions();
        labelPool[c].setVisible(true);
        labelPool[c].getColor().a = 1f;
        labelPool[c].getColor().r = r;
        labelPool[c].getColor().g = g;
        labelPool[c].getColor().b = b;
        labelPool[c].setText(text);
        labelPool[c].addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeOut(0.2f)));

        labelShadowPool[c].clearActions();
        labelShadowPool[c].getColor().a = 1f;
        labelShadowPool[c].setVisible(true);
        labelShadowPool[c].setText(text);
        labelShadowPool[c].addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeOut(0.2f)));
    }

    private void updateHealthLine() {
        if (targetHealth == health) {
            return;
        }
        if (Math.signum(targetHealth - health) != -targetHealthSign) {
            health += engine.getDeltaTime() * targetHealthSign; // / 2f;
        } else {
            health = targetHealth;
        }

        if (health < 0.5) {
            healthLineColor.set(DANGER_COLOR);
            healthLineColor.lerp(WARNING_COLOR, health * 2f);
        } else {
            healthLineColor.set(WARNING_COLOR);
            healthLineColor.lerp(GOOD_COLOR, (health - 0.5f) * 2f);
        }
        healthLineImage.setColor(healthLineColor);
        healthLineImage.setWidth(MAX_HEARTH_LINE_WIDTH * health);
    }

    @Override
    public void act(float delta) {
        GameScreen gameScreen = (GameScreen)engine.getScreen("game");
        for (int i = 0; i < labelPool.length; i++) {
            Vector2 v = gameScreen.getStagePositionFromScene(labelPosition[i].x, labelPosition[i].y);
            //v.y += labelPool[0].getHeight();
            labelPool[i].setPosition(v.x - 25, v.y);
            labelShadowPool[i].setPosition(v.x - 25, v.y - 2);
        }
        super.act(delta);
        updateHealthLine();
    }

    public void setPlayer(Entity player) {
        playerImage.setVisible(false);
        this.player = player.getComponent(PlayerComponent.class);
        healthComponent = player.getComponent(HealthComponent.class);
        body = player.getComponent(BodyComponent.class);
        view = player.getComponent(ViewComponent.class);
        updateScore();
        updateHealth();
        MessageHandler messageHandler = player.getMessageHandler();
        messageHandler.subscribe(PlayerComponent.FLOPPY_CHANGED, playerFloppyChangedListener);
        messageHandler.subscribe(PlayerComponent.SCORE_CHANGED, playerScoreChangedListener);
        messageHandler.subscribe(HealthComponent.DECREASED, healthChangedListener);
        messageHandler.subscribe(HealthComponent.INCREASED, healthChangedListener);
    }

    public void startGameOver() {
        whiteImage.getColor().a = 0;
        whiteImage.setVisible(true);
        whiteImage.addAction(Actions.sequence(
            Actions.alpha(1, 0.1f, Interpolation.exp5In),
            Actions.alpha(0, 0.4f, Interpolation.pow2In)
        ));
        playerImage.setVisible(true);
        GameScreen gameScreen = (GameScreen)engine.getScreen("game");
        Vector2 pos = gameScreen.getStagePositionFromScene(body.getX(), body.getY());
        playerImage.setPosition(pos.x - 10, pos.y);
        playerImage.setScale(4f * (view.isFlipX() ? -1 : 1), 4f);
    }

    public void revive() {
        playerImage.setVisible(false);
    }

}
