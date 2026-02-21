package net.dynart.neonsignal.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;

import net.dynart.neonsignal.core.Level;
import net.dynart.neonsignal.core.controller.GameController;
import net.dynart.neonsignal.core.controller.TouchListener;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.Screen;
import net.dynart.neonsignal.core.script.ScriptLoader;
import net.dynart.neonsignal.core.script.SequenceCommand;

import net.dynart.neonsignal.GameStage;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.GameSceneLoader;
import net.dynart.neonsignal.core.ui.FadeToAction;

public class GameScreen extends Screen {

    private final GameSceneLoader gameSceneLoader;
    private final GameScene gameScene;
    private final GameStage gameStage;
    private final GameController gameController;
    private final TouchListener touchListener;
    private final Vector2 screenCoordinates = new Vector2();
    private final ScriptLoader scriptLoader;
    private final SequenceCommand sequenceCommand;
    private final Action fadeToGameOverAction;

    private Level currentLevel;
    private float gameOverCountDown;

    public GameScreen(final Engine engine) {
        super(engine);
        clear = false;
        gameStage = (GameStage)stage;
        gameController = engine.getGameController();
        addSideBlackBars(gameStage); // before TouchListener!
        touchListener = engine.getTouchListener();
        touchListener.addActorsToStage(gameStage);
        gameScene = engine.getGameScene();
        gameSceneLoader = engine.getGameSceneLoader();
        scriptLoader = engine.getScriptLoader();
        sequenceCommand = new SequenceCommand();
        fadeToGameOverAction = new FadeToAction(engine, "game_over");
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    @Override
    public Stage createStage() {
        return new GameStage(viewport, engine, this);
    }

    public void loadLevel(Level level) {
        currentLevel = level;
        gameSceneLoader.loadLevel(currentLevel.getPath());
        gameStage.setPlayer(gameScene.getPlayer());
        engine.resetDeltaTime();
        if (engine.getAnalyticsManager() != null) {
            engine.getAnalyticsManager().trackLevelStart(currentLevel);
        }
    }

    @Override
    public void update(float delta) {
        if (delta > 0.1) {
            // LibGDX super solution: https://github.com/libgdx/libgdx/issues/1133
            // On Android, after the phone woke up, for 4 frames the delta is about 0.3
            return;
        }
        sequenceCommand.act(delta);
        gameScene.update(delta);
        if (gameController.isMenuPressed()) {
            pause();
        }
        if (gameOverCountDown > 0) {
            gameOverCountDown -= delta;
            if (gameOverCountDown < 0) {
                gameOverCountDown = 0;
                fadeOut(fadeToGameOverAction);
            }
        }
        super.update(delta);
    }

    void updateCamera() {
        gameScene.updateCamera(engine.getDeltaTime());
    }

    @Override
    public void draw() {
        clear();
        gameScene.draw();
        super.draw();
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(engine.getSettings().mustCatchMouse());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gameStage.resize();
        touchListener.resize();
        gameScene.resize(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        gameScene.dispose();
        gameSceneLoader.dispose();
    }

    public void pause() {
        PauseScreen pauseScreen = (PauseScreen)engine.getScreen("pause");
        pauseScreen.resetMenuCursor();
        engine.moveToScreen("pause");
    }

    public Vector2 getStagePositionFromScene(float x, float y) {
        Camera camera = gameScene.getCamera(); //
        float r = viewport.getScreenHeight() / config.getGameVirtualHeight();
        float h = (float)viewport.getScreenHeight() / 2f;
        float w = (float)viewport.getScreenWidth() / 2f;
        screenCoordinates.set((x - camera.position.x) * r + w, (y - camera.position.y) * r + h);
        Vector2 result = gameStage.screenToStageCoordinates(screenCoordinates);
        result.y = config.getStageVirtualHeight() - result.y;
        return result;
    }

    public GameScene getScene() {
        return gameScene;
    }

    public void runScript(String path) {
        sequenceCommand.init(scriptLoader.load(path));
    }

    public void prepareForGameOver() {
        if (engine.getAnalyticsManager() != null && currentLevel != null) {
            net.dynart.neonsignal.components.BodyComponent body =
                gameScene.getPlayer().getComponent(net.dynart.neonsignal.components.BodyComponent.class);
            engine.getAnalyticsManager().trackDeath(currentLevel, body.getCenterX(), body.getBottom());
        }
        gameOverCountDown = 0.5f;
        gameStage.startGameOver();
    }

    public void revive() {
        gameStage.revive();
        gameScene.revive();
    }

}
