package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import net.dynart.neonsignal.core.controller.GameController;
import net.dynart.neonsignal.core.controller.GamepadListener;
import net.dynart.neonsignal.core.controller.KeyboardListener;
import net.dynart.neonsignal.core.controller.TouchListener;
import net.dynart.neonsignal.core.listeners.LoadingFinishedListener;
import net.dynart.neonsignal.core.script.ScriptLoader;
import net.dynart.neonsignal.core.ui.Styles;
import net.dynart.neonsignal.core.utils.JsonUtil;

import java.nio.file.Paths;
import java.util.HashMap;

public class Engine implements LoadingFinishedListener {

    public static final String LOG_TAG = "Engine";

    private static Engine instance;

    private final HashMap<String, Screen> screens = new HashMap<>();
    private final Array<InputProcessor> inputProcessors = new Array<>();
    private final Settings settings;
    private final EngineConfig config;
    private final boolean debug;

    private InputMultiplexer inputMultiplexer;
    private AssetManager assetManager;
    private TextureManager textureManager;
    private GameController gameController;
    private SoundManager soundManager;
    private FontManager fontManager;
    private SpriteAnimationManager spriteAnimationManager;
    private LevelManager levelManager;
    private GameScene gameScene;
    private GameSceneLoader gameSceneLoader;
    private ScriptLoader scriptLoader;
    private Screen screen;
    private Screen nextScreen;
    private boolean moveInNextScreen;
    private TouchListener touchListener;
    private GamepadListener gamepadListener;
    private Styles styles;
    private JsonValue resourcesJson;
    private User user;
    private float elapsedTime;
    private Fade fade;
    private Stencil stencil;
    private float deltaTime;
    private boolean inBackground;
    private FadeRenderer fadeRenderer;
    private float accTime;
    private boolean resetDeltaTime;

    public Engine(EngineConfig engineConfig, boolean debug) {
        instance = this;
        this.debug = debug;
        config = engineConfig;
        settings = new Settings(config);
    }

    public static Engine getInstance() {
        return instance;
    }

    public void resetDeltaTime() {
        resetDeltaTime = true;
    }

    public void create() {

        Gdx.app.log(LOG_TAG, "Starting NeonSignal Engine");
        Gdx.app.log(LOG_TAG, "Working directory: " + Paths.get(".").toAbsolutePath().normalize());

        // create load manager
        assetManager = new AssetManager();

        // create resource managers
        spriteAnimationManager = new SpriteAnimationManager();
        textureManager = new TextureManager(this);
        soundManager = new SoundManager(this);
        fontManager = new FontManager(this);
        levelManager = new LevelManager();

        // create user related objects
        gameController = new GameController(this);
        user = new User(this);

        // create fade related objects
        stencil = new Stencil();
        fade = new Fade(this);
        fadeRenderer = config.getDefaultFadeRenderer();
        fadeRenderer.init(this);

        // create script related objects
        scriptLoader = config.getScriptLoader();
        scriptLoader.init(this);

        // load the resources.json
        JsonReader jsonReader = new JsonReader();
        JsonValue internalJson = JsonUtil.tryToLoad(jsonReader, Gdx.files.internal("data/resources.json"));
        JsonValue externalJson = JsonUtil.tryToLoad(jsonReader, Gdx.files.local("data/resources-custom.json"));
        resourcesJson = JsonUtil.mergeJson(internalJson, externalJson);

        // add resources to the load queue
        textureManager.load(resourcesJson);
        soundManager.load(resourcesJson);
        fontManager.load(resourcesJson);
        levelManager.load(resourcesJson);
    }

    public void createStyles() {
        styles = new Styles(this);
    }

    public EngineConfig getConfig() { return config; }
    public GameController getGameController() { return gameController; }
    public SoundManager getSoundManager() { return soundManager; }
    public SpriteAnimationManager getSpriteAnimationManager() { return spriteAnimationManager; }
    public TextureManager getTextureManager() { return textureManager; }
    public FontManager getFontManager() { return fontManager; }
    public TouchListener getTouchListener() { return touchListener; }
    public GamepadListener getGamepadListener() { return gamepadListener; }
    public Styles getStyles() { return styles; }
    public AssetManager getAssetManager() { return assetManager; }
    public Settings getSettings() { return settings; }
    public GameScene getGameScene() { return gameScene; }
    public GameSceneLoader getGameSceneLoader() { return gameSceneLoader; }
    public LevelManager getLevelManager() { return levelManager; }
    public User getUser() { return user; }
    public Fade getFade() { return fade; }
    public Stencil getStencil() { return stencil; }
    public FadeRenderer getFadeRenderer() { return fadeRenderer; }
    public ScriptLoader getScriptLoader() { return scriptLoader; }
    public Screen getCurrentScreen() {
        return screen;
    }

    public void setFadeRenderer(FadeRenderer value) {
        fadeRenderer = value;
    }

    public boolean inDebugMode() {
        return debug;
    }

    public void pause() {
        inBackground = true;
        soundManager.pause();
    }

    public void resume() {
        inBackground = false;
        soundManager.resume();
    }

    public void addScreen(String name, Screen screen) {
        screens.put(name, screen);
    }

    public void setScreen(String name) {
        setScreen(screens.get(name));
    }

    public void setScreen(Screen screen) {
        if (nextScreen != screen) {
            nextScreen = screen;
            moveInNextScreen = false;
        }
    }

    public void moveToScreen(String name) {
        Screen screen = screens.get(name);
        if (nextScreen != screen) {
            nextScreen = screen;
            moveInNextScreen = true;
        }
    }

    public void resize(int width, int height) {
        screen.resize(width, height);
        stencil.resize(width, height);
        fade.resize(width, height);
    }

    @Override
    public void loadingFinished() {
        setUpGraphicResources();
        setUpGameController();
        setUpGameScene();
    }

    private void setUpGraphicResources() {
        textureManager.init(resourcesJson);
        spriteAnimationManager.init(textureManager, resourcesJson);
    }

    private void setUpGameController() {
        KeyboardListener keyboardListener = new KeyboardListener(gameController);
        gamepadListener = new GamepadListener(gameController);
        touchListener = new TouchListener(this);
        if (!config.getSection().equals("ios") && !config.getSection().equals("muos")) { // no Controllers support on IOS and MuOS for now
            Controllers.addListener(gamepadListener);
        }
        inputProcessors.add(new InputAdapter()); // empty for now, will be used by a "Stage"
        inputProcessors.add(keyboardListener);
        inputProcessors.add(touchListener);
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.setProcessors(inputProcessors);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void setUpGameScene() {
        gameScene = new GameScene(this);
        gameScene.init();
        gameSceneLoader = new GameSceneLoader(this);
    }

    public void initScreens() {
        for (Screen screen : screens.values()) {
            screen.init();
        }
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public void render() {
        update();
        draw();
        postUpdate();
    }

    private void update() {
        gameController.update();
        if (inBackground || resetDeltaTime) {
            deltaTime = 0;
            resetDeltaTime = false;
        } else {
            deltaTime = Gdx.graphics.getDeltaTime();
            if (deltaTime < 0.03f) {
                // just to be sure, maximize the update fps at about 200
                // because above that glitches can happen
                accTime += deltaTime;
                if (accTime < 0.005f) {
                    return;
                }
                deltaTime = accTime;
                accTime = 0;
            } else {
                // just to be sure, minimum 30 fps
                deltaTime = 0.03f;
            }
        }
        elapsedTime += deltaTime;
        soundManager.update();
        screen.update(deltaTime);
        fade.update(deltaTime);
    }

    private void draw() {
        if (fade.isActive()) {
            fade.draw(screen.getStage());
        } else {
            screen.draw();
        }
    }

    private void postUpdate() {
        gameController.postUpdate();
        changeToNextScreen();
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public void changeToNextScreen() {
        if (nextScreen == null) {
            return;
        }
        resetDeltaTime();
        screen = nextScreen;
        if (inputProcessors.size != 0) { // when loading, it will be empty
            setInputProcessor(screen.getStage());
        }
        screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screen.show();
        if (moveInNextScreen) {
            screen.moveIn();
        }
        nextScreen = null;
    }

    private void setInputProcessor(InputProcessor inputProcessor) {
        inputProcessors.set(0, inputProcessor);
        inputMultiplexer.setProcessors(inputProcessors);
    }

    public void dispose() {
        for (Screen screen : screens.values()) {
            screen.dispose();
        }
        stencil.dispose();
        fade.dispose();
        fadeRenderer.dispose();
        soundManager.dispose();
        assetManager.dispose();
    }

    public Screen getScreen(String name) {
        return screens.get(name);
    }

}
