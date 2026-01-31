package net.dynart.neonsignal;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;

import net.dynart.neonsignal.core.LevelManager;
import net.dynart.neonsignal.core.listeners.LoadingFinishedListener;

import net.dynart.neonsignal.screens.CutsceneScreen;
import net.dynart.neonsignal.screens.CompletedScreen;
import net.dynart.neonsignal.screens.CustomizeGamepadScreen;
import net.dynart.neonsignal.screens.CustomizeKeyboardScreen;
import net.dynart.neonsignal.screens.CustomizeTouchScreen;
import net.dynart.neonsignal.screens.DialogScreen;
import net.dynart.neonsignal.screens.GameFadeInScreen;
import net.dynart.neonsignal.screens.GameOverScreen;
import net.dynart.neonsignal.screens.GameScreen;
import net.dynart.neonsignal.screens.LoadingScreen;
import net.dynart.neonsignal.screens.LogoScreen;
import net.dynart.neonsignal.screens.MainMenuScreen;
import net.dynart.neonsignal.screens.PauseScreen;
import net.dynart.neonsignal.screens.SettingsScreen;
import net.dynart.neonsignal.screens.LevelScreen;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.core.Level;

public class NeonSignal extends ApplicationAdapter implements LoadingFinishedListener {

    private final String configSection;
    private final String startWithLevel;
    private final boolean debug;

    private FPSLogger fpsLogger;
    private EngineConfig config;
    private Engine engine;

    public NeonSignal(String configSection, boolean debug, String startWithLevel) {
        this.configSection = configSection;
        this.startWithLevel = startWithLevel;
        this.debug = debug;
    }

    @Override
    public void create() {
        fpsLogger = new FPSLogger();
        //Gdx.input.setCursorCatched(true);

        config = new NeonSignalEngineConfig();
        config.load(configSection);
        config.setEntityFactory(new NeonSignalEntityFactory());
        config.setDefaultFadeRenderer(new NeonSignalFadeRenderer());
        config.setScriptLoader(new NeonSignalScriptLoader());

        engine = new Engine(config, debug);
        engine.create();

        addLoadingScreen();
    }

    public EngineConfig getConfig() {
        return config;
    }

    private void addLoadingScreen() {
        engine.createStyles(); // needs to be done before loading screen
        LoadingScreen loadingScreen = new LoadingScreen(engine);
        loadingScreen.setFinishedListener(this);
        loadingScreen.setNextScreen("logo");
        engine.addScreen("loading", loadingScreen);
        engine.setScreen("loading");
        engine.changeToNextScreen();
    }

    @Override
    public void loadingFinished() {
        GameScreen gameScreen = new GameScreen(engine);
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
                Thread.sleep(250);   // 1/4 second wait for Android ..
            } catch (InterruptedException ignored) {}
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
        GameScreen gameScreen = (GameScreen)engine.getScreen("game");
        gameScreen.loadLevel(level);
        LoadingScreen loadingScreen = (LoadingScreen)engine.getScreen("loading");
        loadingScreen.setNextScreen("game");
    }

    @Override
    public void resize(int width, int height) {
        engine.resize(width, height);
    }

    @Override
    public void render() {
        //fpsLogger.log();
        engine.render();
    }

    @Override
    public void pause () {
        engine.pause();
    }

    @Override
    public void resume () {
        engine.resume();
    }

    @Override
    public void dispose () {
        engine.dispose();
    }

}
