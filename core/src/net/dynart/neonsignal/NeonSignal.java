package net.dynart.neonsignal;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.utils.Align;

import net.dynart.lisa.components.BodyComponent;
import net.dynart.lisa.components.EnemyComponent;
import net.dynart.lisa.components.HealthComponent;
import net.dynart.lisa.components.VelocityComponent;
import net.dynart.lisa.core.ActionHandler;
import net.dynart.lisa.core.DamageHandler;
import net.dynart.lisa.core.Engine;
import net.dynart.lisa.core.Entity;
import net.dynart.lisa.core.EntityManager;
import net.dynart.lisa.core.ExitHandler;
import net.dynart.lisa.core.Level;
import net.dynart.lisa.core.LevelManager;
import net.dynart.lisa.core.PlayerEventHandler;
import net.dynart.lisa.core.PlayerInitializer;
import net.dynart.lisa.core.ScreenFadeOutHandler;
import net.dynart.lisa.core.TouchAbilityProvider;
import net.dynart.lisa.core.listeners.LoadingFinishedListener;
import net.dynart.lisa.core.analytics.AnalyticsManager;
import net.dynart.lisa.core.ui.MenuButtonStyle;
import net.dynart.neonsignal.components.PlayerComponent;
import net.dynart.neonsignal.core.PlayerAbility;
import net.dynart.neonsignal.core.analytics.AnalyticsManagerFactory;
import net.dynart.neonsignal.core.analytics.MeasurementProtocolAnalyticsManager;
import net.dynart.neonsignal.screens.CompletedScreen;
import net.dynart.neonsignal.screens.CutsceneScreen;
import net.dynart.lisa.screens.GameFadeInScreen;
import net.dynart.lisa.screens.GameOverScreen;
import net.dynart.neonsignal.screens.GameScreen;
import net.dynart.neonsignal.screens.LevelScreen;
import net.dynart.neonsignal.screens.MainMenuScreen;
import net.dynart.lisa.screens.PauseScreen;
import net.dynart.lisa.screens.SettingsScreen;
import net.dynart.neonsignal.ui.MenuBackground;
import net.dynart.lisa.screens.CustomizeGamepadScreen;
import net.dynart.lisa.screens.CustomizeKeyboardScreen;
import net.dynart.lisa.screens.CustomizeTouchScreen;
import net.dynart.lisa.screens.DialogScreen;
import net.dynart.lisa.screens.LoadingScreen;
import net.dynart.lisa.screens.LogoScreen;

public class NeonSignal extends ApplicationAdapter implements LoadingFinishedListener {

    private final String configSection;
    private final String startWithLevel;
    private final boolean debug;
    private final boolean gaDebug;
    private final AnalyticsManagerFactory analyticsManagerFactory;

    private FPSLogger fpsLogger;
    private NeonSignalEngineConfig config;
    private Engine engine;

    public NeonSignal(String configSection, boolean debug, String startWithLevel, boolean gaDebug) {
        this(
            configSection, debug, startWithLevel, gaDebug, MeasurementProtocolAnalyticsManager::new
        );
    }

    public NeonSignal(
        String configSection, boolean debug, String startWithLevel, boolean gaDebug,
        AnalyticsManagerFactory analyticsManagerFactory) {
        this.configSection = configSection;
        this.startWithLevel = startWithLevel;
        this.debug = debug;
        this.gaDebug = gaDebug;
        this.analyticsManagerFactory = analyticsManagerFactory;
    }

    @Override
    public void create() {
        fpsLogger = new FPSLogger();
        // Gdx.input.setCursorCatched(true);

        config = new NeonSignalEngineConfig();
        config.load(configSection);
        config.setGaDebug(gaDebug);
        config.setEntityFactory(new NeonSignalEntityFactory());
        config.setDefaultFadeRenderer(new NeonSignalFadeRenderer());
        config.setScriptLoader(new NeonSignalScriptLoader());

        engine = new Engine(config, debug);
        engine.create();
        if (analyticsManagerFactory != null) {
            AnalyticsManager am = analyticsManagerFactory
                .create(config, engine.getUser(), engine.getSettings());
            engine.setAnalyticsManager(am);
        }

        addLoadingScreen();
    }

    public NeonSignalEngineConfig getConfig() {
        return config;
    }

    private void addLoadingScreen() {
        engine.createStyles(); // needs to be done before loading screen
        for (MenuButtonStyle s : new MenuButtonStyle[] {
                engine.getStyles().getDefaultButtonStyle(),
                engine.getStyles().getDefaultButtonPushedStyle(),
                engine.getStyles().getSecondaryButtonStyle()}) {
            s.iconOffsetY = 0f;
            s.iconGap = 6f;
            s.paddingLeft = 15f;
            s.paddingTop = 0f;

            // disable the press animation
            s.unpressedOffsetY = 0;
            s.pressedOffsetY = 0;
            s.checkedOffsetY = 0;
            s.unpressedOffsetX = 0;
            s.pressedOffsetX = 0;
            s.checkedOffsetX = 0;
        }

        LoadingScreen loadingScreen = new LoadingScreen(engine);
        loadingScreen.setFinishedListener(this);
        loadingScreen.setNextScreen("logo");
        engine.addScreen("loading", loadingScreen);
        engine.setScreen("loading");
        engine.changeToNextScreen();
    }

    @Override
    public void loadingFinished() {
        engine.setMenuBackgroundFactory(() -> new MenuBackground(engine));
        EntityManager entityManager = engine.getGameScene().getEntityManager();
        entityManager.setOnAnimationStarted(() -> {
            for (Entity e : entityManager.getAllByClass(PlayerComponent.class)) {
                VelocityComponent velocity = e.getComponent(VelocityComponent.class);
                velocity.setX(0);
                velocity.setY(0);
                velocity.setAcceleration(0);
            }
        });
        engine.setTouchAbilityProvider(new TouchAbilityProvider() {
            @Override
            public boolean hasButtonAbility(String buttonName) {
                PlayerComponent player = getPlayer();
                if (player == null)
                    return false;
                switch (buttonName) {
                    case "joy_bottom":
                    case "joy_top":
                        return player.hasAbility(PlayerAbility.MOVE);
                    case "a":
                        return player.hasAbility(PlayerAbility.JUMPING);
                    case "b":
                        return player.hasAbility(PlayerAbility.DASHING);
                    case "x":
                        return player.hasAbility(PlayerAbility.FIRING);
                    default:
                        return false;
                }
            }

            @Override
            public boolean hasMoveAbility() {
                PlayerComponent player = getPlayer();
                return player == null || player.hasAbility(PlayerAbility.MOVE);
            }

            private PlayerComponent getPlayer() {
                Entity entity = engine.getGameScene().getPlayer();
                return entity != null ? entity.getComponent(PlayerComponent.class) : null;
            }
        });

        GameScreen gameScreen = new GameScreen(engine);

        // register handlers
        engine.setActionHandler((path, cutscene) -> {
            if (cutscene) {
                CutsceneScreen cs = (CutsceneScreen) engine.getScreen("cutscene");
                cs.load(path);
                engine.moveToScreen("cutscene");
            } else {
                GameScreen gs = (GameScreen) engine.getScreen("game");
                gs.runScript(path);
            }
        });
        engine.setExitHandler(left -> engine.moveToScreen("completed"));
        engine.setScreenFadeOutHandler(action -> {
            GameScreen gs = (GameScreen) engine.getScreen("game");
            gs.fadeOut(action);
        });
        engine.setDamageHandler(new DamageHandler() {
            @Override
            public void onDamage(Entity victim, Entity attacker, float amount) {
                Entity playerEntity = engine.getGameScene().getPlayer();
                GameScreen gs = (GameScreen) engine.getScreen("game");
                GameStage gameStage = (GameStage) gs.getStage();
                BodyComponent body = victim.getComponent(BodyComponent.class);
                if (body == null)
                    return;
                if (victim == playerEntity || victim.hasComponent(EnemyComponent.class)) {
                    String text = "-" + (int) (amount * 100);
                    gameStage
                        .showItemScore(body.getCenterX(), body.getTop() + 8, text, 1, .44f, .44f);
                }
            }

            @Override
            public void onKill(Entity victim, Entity attacker) {
                if (!victim.hasComponent(EnemyComponent.class))
                    return;
                Entity playerEntity = engine.getGameScene().getPlayer();
                if (playerEntity == null)
                    return;
                PlayerComponent playerComp = playerEntity.getComponent(PlayerComponent.class);
                int point = 100;
                playerComp.addPoint(point);
                playerComp.incKnockoutCount();
                BodyComponent body = victim.getComponent(BodyComponent.class);
                if (body != null) {
                    GameScreen gs = (GameScreen) engine.getScreen("game");
                    GameStage gameStage = (GameStage) gs.getStage();
                    gameStage.showItemScore(
                        body.getCenterX(), body.getTop() + 16, "+" + point, 1, 0.8f, 0.1f
                    );
                }
            }
        });
        engine.setPlayerEventHandler(new PlayerEventHandler() {
            @Override
            public void onSecretFound(Entity player) {
                player.getComponent(PlayerComponent.class).incSecretCount();
            }

            @Override
            public void onEnemyKilled(Entity player, Entity enemy) {
                player.getComponent(PlayerComponent.class).incKnockoutCount();
            }

            @Override
            public void onOxygenUsed(Entity player) {
                player.getComponent(PlayerComponent.class).incOxygen();
            }

            @Override
            public void onPushed(Entity player, Entity pusher) {
                player.getComponent(PlayerComponent.class).setPushedBy(pusher);
            }

            @Override
            public void onSwitchOverlap(Entity player, Entity switchEntity) {
                player.getComponent(PlayerComponent.class).setOnSwitch(switchEntity);
            }

            @Override
            public void onCheckpoint(Entity player, float x, float y) {
                GameScreen gs = (GameScreen) engine.getScreen("game");
                Level level = gs.getCurrentLevel();
                if (level == null) {
                    return;
                }
                java.util.Map<String, Object> params = new java.util.HashMap<>();
                params.put("level_name", level.getName());
                params.put("checkpoint_x", (int) x);
                params.put("checkpoint_y", (int) y);
                engine.getAnalyticsManager().track("checkpoint_reached", params);
            }
        });
        engine.setPlayerInitializer((player, props) -> {
            PlayerComponent pc = player.getComponent(PlayerComponent.class);
            if (props.containsKey("player_abilities")) {
                for (String abilityString : props.get("player_abilities").toString().split(",")) {
                    pc.addAbility(PlayerAbility.valueOf(abilityString.trim().toUpperCase()));
                }
            } else {
                for (PlayerAbility ability : PlayerAbility.values()) {
                    pc.addAbility(ability);
                }
            }
        });

        engine.addScreen("logo", new LogoScreen(engine));
        engine.addScreen("dialog", new DialogScreen(engine));
        engine.addScreen("game_fade_in", new GameFadeInScreen(engine));
        engine.addScreen("game", gameScreen);
        engine.addScreen("pause", new PauseScreen(engine));
        engine.addScreen("menu", new MainMenuScreen(engine));
        engine.addScreen("settings", new SettingsScreen(engine));
        engine.addScreen("customize_touch", new CustomizeTouchScreen(engine));
        engine.addScreen("customize_keyboard", new CustomizeKeyboardScreen(engine));
        engine.addScreen("customize_gamepad", new CustomizeGamepadScreen(engine));
        engine.addScreen("levels", new LevelScreen(engine)); // after GameScreen added!
        engine.addScreen("game_over", new GameOverScreen(engine));
        engine.addScreen("completed", new CompletedScreen(engine));
        engine.addScreen("cutscene", new CutsceneScreen(engine)); // after GameScreen added!
        engine.initScreens();
        sleepAtTheBeginning();
        startWithLevel();
    }

    private void sleepAtTheBeginning() {
        if (config.isMobile()) {
            try {
                Thread.sleep(250); // 1/4 second wait for Android ..
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void startWithLevel() { // TODO: put this to the engine
        if (startWithLevel == null) {
            return;
        }
        LevelManager levelManager = engine.getLevelManager();
        Level level = levelManager.get("first");
        if (!levelManager.has(startWithLevel)) {
            FileHandle fileHandle = Gdx.files.absolute(startWithLevel);
            if (!fileHandle.exists()) {
                throw new RuntimeException("Level not found: " + startWithLevel);
            }
            level.setPath(startWithLevel);
        } else {
            level = levelManager.get(startWithLevel);
        }
        GameScreen gameScreen = (GameScreen) engine.getScreen("game");
        gameScreen.loadLevel(level);
        LoadingScreen loadingScreen = (LoadingScreen) engine.getScreen("loading");
        loadingScreen.setNextScreen("game");
    }

    @Override
    public void resize(int width, int height) {
        engine.resize(width, height);
    }

    @Override
    public void render() {
        // fpsLogger.log();
        engine.render();
    }

    @Override
    public void pause() {
        engine.pause();
    }

    @Override
    public void resume() {
        engine.resume();
    }

    @Override
    public void dispose() {
        engine.dispose();
    }

}
