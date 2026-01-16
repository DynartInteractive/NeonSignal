package net.dynart.neonsignal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.dynart.neonsignal.core.Fade;
import net.dynart.neonsignal.core.FadeRenderer;
import net.dynart.neonsignal.core.Screen;
import net.dynart.neonsignal.core.Stencil;
import net.dynart.neonsignal.core.Engine;

public class NeonSignalFadeRenderer implements FadeRenderer {

    private static final int TILE_SIZE = 48;

    private Engine engine;
    private Sprite sprite;
    private Batch batch;

    public void init(Engine engine) {
        this.engine = engine;
        Texture texture = new Texture("data/textures/white_square.png");
        sprite = new Sprite(texture);
        sprite.setSize(TILE_SIZE, TILE_SIZE);
        sprite.setOrigin(TILE_SIZE / 2f, TILE_SIZE / 2f);
        batch = new SpriteBatch();
    }

    public void draw(Stage stage, float value) {
        Viewport viewport = stage.getViewport();
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        Stencil stencil = engine.getStencil();
        Screen screen = engine.getCurrentScreen();
        Fade fade = engine.getFade();

        FrameBuffer frameBuffer = fade.getFrameBuffer();
        frameBuffer.begin();
        screen.draw();
        frameBuffer.end();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        stencil.beginStencil(stage.getCamera(), batch);

        int cols = (int) Math.ceil(w / TILE_SIZE) + 1;
        int rows = (int) Math.ceil(h / TILE_SIZE) + 1;
        int maxDiagonal = (cols - 1) + (rows - 1);
        float waveSpread = 0.5f; // portion of time spent on wave delay (0 = no wave, 1 = max spread)

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                float diagonal = col + row;
                float normalizedDelay = (diagonal / maxDiagonal) * waveSpread;
                float tileValue = (value - normalizedDelay) / (1 - waveSpread);
                tileValue = Math.max(0, Math.min(1, tileValue));

                float x = col * TILE_SIZE;
                float y = row * TILE_SIZE;
                sprite.setScale(tileValue);
                sprite.setPosition(x, y);
                sprite.draw(batch);
            }
        }

        stencil.endStencil();
        stencil.draw(frameBuffer, stage);
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }

}
