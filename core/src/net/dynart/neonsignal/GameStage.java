package net.dynart.neonsignal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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

    // health
    private final Group hpGroup;
    private static final float MAX_HP_LINE_WIDTH = 256;
    private static final Color DANGER_COLOR = new Color(1, 0, 0.45f, 1);
    private static final Color WARNING_COLOR = new Color(1, 0.88f, 0, 1);
    private static final Color GOOD_COLOR = new Color(0.5f, 0.88f, 0, 1);
    private final MessageListener healthChangedListener;
    private final Image hpLineImage;
    private final TextureRegion originalHpLineImageRegion;
    private final Color hpLineColor = new Color(GOOD_COLOR);
    private float health = 1;
    private float targetHealthSign = 0;
    private float targetHealth = 1;

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
        pauseButton.setWidth(110);
        pauseButton.setHeight(110);
        pauseButton.setIcon(new Image(skin.getDrawable("icon_pause")));

        addActor(pauseButton);


        // health bar
        Group healthBarGroup = new Group();
        healthBarGroup.setY(0);
        healthBarGroup.setX(0);

        hpLineImage = new Image(uiPixelSkin.getDrawable("hud_hp_line"));
        hpLineImage.setColor(GOOD_COLOR);

        originalHpLineImageRegion = new TextureRegion(((TextureRegionDrawable)hpLineImage.getDrawable()).getRegion());

        Image healthBarImage = new Image(uiPixelSkin.getDrawable("hud_hp_bar"));

        healthBarGroup.addActor(healthBarImage);
        healthBarGroup.addActor(hpLineImage);

        // health
        hpGroup = new Group();
        hpGroup.setY(config.getStageVirtualHeight() - 74);
        hpGroup.setX(screen.getSideBlackBarWidth() + 96);

        hpGroup.addActor(healthBarGroup);

        addActor(hpGroup);

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
        hpGroup.setX(35 + sideBlackBarWidth);
        scoreLabel.setX(getWidth() - scorePadding - scoreLabel.getWidth());
        scoreLabel.setY(getHeight() - scoreLabel.getHeight() - 30);
    }

    private void updateScore() {
        scoreLabel.setText(player.getScore());
    }

    private void updateHealth() {
        targetHealth = healthComponent.getValue() / healthComponent.getMaxValue();
        targetHealthSign = Math.signum(targetHealth - health);
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
            hpLineColor.set(DANGER_COLOR);
            hpLineColor.lerp(WARNING_COLOR, health * 2f);
        } else {
            hpLineColor.set(WARNING_COLOR);
            hpLineColor.lerp(GOOD_COLOR, (health - 0.5f) * 2f);
        }

        float h = health * 0.78f + 0.11f; // crop 0.11 from both sides and shift

        TextureRegion region = ((TextureRegionDrawable)hpLineImage.getDrawable()).getRegion();
        region.setRegion(originalHpLineImageRegion);
        region.setRegion(region.getRegionX(), region.getRegionY(), (int)((float)region.getRegionWidth() * h), region.getRegionHeight());
        ((TextureRegionDrawable)hpLineImage.getDrawable()).setRegion(region);
        hpLineImage.setColor(hpLineColor);
        hpLineImage.setWidth(MAX_HP_LINE_WIDTH * h);
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
