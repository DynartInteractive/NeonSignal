package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Screen {

    protected EngineConfig config;
    protected Engine engine;
    protected Stage stage;
    protected Fade fade;
    protected Batch batch;
    protected Viewport viewport;
    protected Image[] sideBlackBar = { new Image(), new Image() };
    protected boolean clear = true;

    public Screen(Engine engine) {
        this.engine = engine;
        config = engine.getConfig();
        viewport = new ExtendViewport(
            config.getStageVirtualHeight(), config.getStageVirtualHeight()//,
            //config.getStageMaxVirtualWidth(), config.getStageVirtualHeight()
        );
        batch = new SpriteBatch();
        stage = createStage();
        fade = engine.getFade();
    }

    public Stage getStage() {
        return stage;
    }

    public Stage createStage() {
        return new Stage(viewport, batch);
    }

    public void init() {}

    public void show() {}

    public void update(float delta) {
        if (delta > 0.1) {
            // LibGDX super solution: https://github.com/libgdx/libgdx/issues/1133
            // On Android, after the phone woke up, for 4 frames the delta is about 0.3
            return;
        }
        stage.act(delta);
    }

    public void clear() {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f,1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
    }

    public void draw() {
        if (clear) {
            clear();
        }
        stage.draw();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        float w = getSideBlackBarWidth(false);
        for (int i = 0; i < 2; i++) {
            sideBlackBar[i].setWidth(w);
            sideBlackBar[i].setHeight(viewport.getWorldHeight());
            sideBlackBar[i].setVisible(w != 0);
        }
        sideBlackBar[1].setX(viewport.getWorldWidth() - w);
    }

    public void dispose() {
        stage.dispose();
    }

    public void moveIn() {}

    public void fadeIn() {
        fade.in();
    }

    public void fadeOut(Action action) {
        fade.setAction(Fade.Direction.OUT, action);
        fade.out();
    }

    public float getSideBlackBarWidth() {
        return getSideBlackBarWidth(false);
    }

    public float getSideBlackBarWidth(boolean convertToGameScene) {
        float maxR = config.getStageMaxVirtualWidth() / config.getStageVirtualHeight();
        float r = viewport.getWorldWidth() / config.getStageVirtualHeight();
        if (r > maxR) {
            float w = (viewport.getWorldWidth() - config.getStageMaxVirtualWidth()) / 2f;
            float r2 = config.getStageVirtualHeight() / config.getGameVirtualHeight();
            return convertToGameScene ? (w / r2 - 1) : w;
        }
        return 0;
    }

    protected void addSideBlackBars(Stage stage) {
        Skin skin = engine.getTextureManager().getSkin("ui");
        for (int i = 0; i < 2; i++) {
            sideBlackBar[i].setDrawable(skin.getDrawable("black"));
            stage.addActor(sideBlackBar[i]);
        }
    }

}
