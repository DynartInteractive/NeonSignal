package net.dynart.neonsignal.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.dynart.neonsignal.core.TextureManager;
import net.dynart.neonsignal.core.Engine;

public class MenuBackground extends Actor {

    private final int gridSize;
    private final Group group;
    private final Engine engine;

    private Image[] images;
    private static float offset;

    public MenuBackground(Engine engine) {
        super();
        this.engine = engine;
        gridSize = 1; // because of ultra wide monitors (3 would be enough for 16:9)
        group = new Group();
        createImages();
    }

    private void createImages() {
        TextureManager textureManager = engine.getTextureManager();
        Skin skin = textureManager.getSkin("ui");
        images = new Image[gridSize * gridSize];
        for (int i = 0; i < images.length; i++) {
            images[i] = new Image(skin.getDrawable("menu_background"));
            //images[i].setScale(5);
            float imageWidth = images[i].getWidth() * images[i].getScaleX() - 0.1f;
            float imageHeight = images[i].getHeight() * images[i].getScaleY() - 0.1f;
            float x = (i % gridSize) * imageWidth;
            float y = (i / gridSize) * imageHeight;
            images[i].setPosition(x, y);
            group.addActor(images[i]);
        }
    }

    private void move() {
        /*
        float delta = engine.getDeltaTime();
        float move = delta * 60f;
        offset -= move;
        float imageWidth = images[0].getWidth() * images[0].getScaleX();
        if (offset < -imageWidth) {
            offset += imageWidth;
        }
         */
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        move();
        group.draw(batch, parentAlpha);
    }
}

