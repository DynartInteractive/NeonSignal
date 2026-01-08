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

public class NeonSignalGlassesFadeRenderer implements FadeRenderer {

    private Engine engine;
    private Sprite sprite;
    private Batch batch;

    public void init(Engine engine) {
        this.engine = engine;
        Texture texture = new Texture("data/textures/glasses_stencil.png");
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sprite = new Sprite(texture);
        sprite.setOriginCenter();
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
        sprite.setScale(value * value * 27);
        sprite.setX((w - sprite.getWidth()) / 2);
        sprite.setY((h - sprite.getHeight()) / 2);
        sprite.rotate(engine.getDeltaTime() * 380);
        sprite.draw(batch);
        stencil.endStencil();
        stencil.draw(frameBuffer, stage);
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }

}
