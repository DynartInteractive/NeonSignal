package net.dynart.neonsignal.core;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import net.dynart.neonsignal.core.utils.Align;

public class GameSprite extends Sprite {

    private float offsetX;
    private float offsetY;
    private boolean visible = true;
    private Align align = Align.CENTER_BOTTOM;
    private boolean flipEnabled = true;
    private boolean rotate90; // clockwise

    public void setFlipEnabled(boolean value) {
        flipEnabled = value;
    }

    public void setAlign(Align value) {
        align = value;
    }

    public Align getAlign() {
        return align;
    }

    public void setOffsetX(float x) {
        offsetX = x;
    }

    public void setOffsetY(float y) {
        offsetY = y;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setRegion(TextureRegion region) {
        super.setRegion(region);
        setSize(region.getRegionWidth(), region.getRegionHeight());
        setOrigin(region.getRegionWidth() / 2f, region.getRegionHeight() / 2f);
        //setOffset(-region.getRegionWidth() / 2, -region.getRegionHeight() / 2);
    }

    public void rotate90() {
        rotate90 = true;
        super.rotate90(true);
    }

    public boolean isRotate90() {
        return rotate90;
    }

    public void draw(Batch batch, boolean viewFlipX, boolean viewFlipY) {
        if (!visible) {
            return;
        }
        float x = getX();
        float y = getY();
        float alignX = getWidth() / 2f * align.getLeftMultiplier();
        float alignY = getHeight() / 2f * align.getBottomMultiplier();
        boolean flipX = isFlipX();
        boolean flipY = isFlipY();
        if (flipEnabled) {
            setFlip(viewFlipX ^ flipX, viewFlipY ^ flipY);
        }
        setPosition(x + alignX + offsetX, y + alignY + offsetY);
        super.draw(batch);
        setPosition(x, y);
        setFlip(flipX, flipY);
    }

}
