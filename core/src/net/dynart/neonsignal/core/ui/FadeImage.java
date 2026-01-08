package net.dynart.neonsignal.core.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

// this class is only because the LibGDX Image doesn't care about the Batch color
// and the fade effect makes interesting things

public class FadeImage extends Image {

    public FadeImage(Drawable drawable) {
        super(drawable);
    }

    public FadeImage(TextureRegion region) {
        super(region);
    }

    public FadeImage(Texture texture) {
        super(texture);
    }

    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(1, 1, 1, parentAlpha);
    }

}
