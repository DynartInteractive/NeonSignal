package net.dynart.neonsignal.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class HudBar extends Group {

    private static final float MAX_LINE_WIDTH = 256;

    private final Image lineImage;
    private final TextureRegion originalLineRegion;
    private final Color barColor = new Color(Color.WHITE);
    private float fill = 1;

    public HudBar(Skin uiPixelSkin, Color color, float scale) {
        barColor.set(color);

        Group inner = new Group();
        inner.setScale(scale);

        Image barImage = new Image(uiPixelSkin.getDrawable("hud_hp_bar"));

        TextureRegion sourceRegion = ((TextureRegionDrawable) uiPixelSkin
            .getDrawable("hud_hp_line")).getRegion();
        originalLineRegion = new TextureRegion(sourceRegion);
        TextureRegion ownRegion = new TextureRegion(sourceRegion);
        lineImage = new Image(new TextureRegionDrawable(ownRegion));
        lineImage.setColor(barColor);

        inner.addActor(barImage);
        inner.addActor(lineImage);
        addActor(inner);
    }

    public void setFill(float value) {
        fill = Math.max(0, Math.min(value, 1));
        float h = fill * 0.78f + 0.11f;

        TextureRegion region = ((TextureRegionDrawable) lineImage.getDrawable()).getRegion();
        region.setRegion(originalLineRegion);
        region.setRegion(
            region.getRegionX(), region.getRegionY(),
            (int) ((float) region.getRegionWidth() * h),
            region.getRegionHeight()
        );
        lineImage.setColor(barColor);
        lineImage.setWidth(MAX_LINE_WIDTH * h);
    }

    public float getFill() {
        return fill;
    }

    public void setBarColor(Color color) {
        barColor.set(color);
        lineImage.setColor(barColor);
    }

    public Color getBarColor() {
        return barColor;
    }
}
