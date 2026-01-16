package net.dynart.neonsignal.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.core.SpriteAnimationManager;
import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.GameSprite;
import net.dynart.neonsignal.core.TextureManager;
import net.dynart.neonsignal.core.utils.Align;

import java.util.ArrayList;
import java.util.List;

import net.dynart.neonsignal.core.Engine;

public class ViewComponent extends Component {

    public static final String APPEARS_ON_SCREEN = "view_appears_on_screen";

    private final List<GameSprite> sprites = new ArrayList<>();
    private final List<String> spriteNames = new ArrayList<>();
    private final List<Animation> animations = new ArrayList<>();
    private final List<Float> animationTimes = new ArrayList<>();
    private final SpriteAnimationManager spriteAnimationManager;
    private final TextureAtlas spriteAtlas;
    private final Camera camera;
    private final Viewport viewport;
    private final float tileWidth;
    private final float tileHeight;

    private BodyComponent body;
    private float offsetY;
    private float offsetX;
    private boolean flipX;
    private boolean flipY;
    private boolean onScreen;
    private boolean wasOnScreen;
    private int layer = 100;
    private int repeatX = 1;
    private int repeatY = 1;

    public ViewComponent(Engine engine) {
        GameScene gameScene = engine.getGameScene();
        TextureManager textureManager = engine.getTextureManager();
        EngineConfig config = engine.getConfig();
        spriteAnimationManager = engine.getSpriteAnimationManager();
        spriteAtlas = textureManager.getAtlas("sprites");
        camera = gameScene.getCamera();
        viewport = gameScene.getViewport();
        tileWidth = config.getTileWidth();
        tileHeight = config.getTileHeight();
    }

    public void setRepeatX(int value) {
        repeatX = value;
    }

    public void setRepeatY(int value) {
        repeatY = value;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        body = entity.getComponent(BodyComponent.class);
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int value) {
        layer = value;
    }

    public void addSprite(GameSprite sprite) {
        sprites.add(sprite);
        spriteNames.add(null);
        animations.add(null);
        animationTimes.add(0f);
    }

    public void addSprite(GameSprite sprite, String animationName) {
        sprites.add(sprite);
        spriteNames.add(null);
        animations.add(spriteAnimationManager.get(animationName));
        animationTimes.add(0f);
    }

    public GameSprite getSprite(int index) {
        return sprites.get(index);
    }

    public int getSpriteCount() {
        return sprites.size();
    }

    public void setSprite(int index, String regionName) {
        GameSprite sprite = sprites.get(index);
        sprite.setRegion(spriteAtlas.findRegion(regionName));
        spriteNames.set(index, regionName);
    }

    public String getSpriteName(int index) {
        return spriteNames.get(index);
    }

    public float getSpriteWidth(int index) { return sprites.get(index).getWidth(); }
    public float getSpriteHeight(int index) { return sprites.get(index).getHeight(); }

    public float getRotation(int index) { return sprites.get(index).getRotation(); }
    public void setRotation(int index, float value) { sprites.get(index).setRotation(value); }

    public void setAnimation(int index, String animationName) {
        if (animationName == null) {
            animations.set(index, null);
        } else {
            Animation animation = spriteAnimationManager.get(animationName);
            if (animation == null) {
                throw new RuntimeException("Animation doesn't exist: " + animationName);
            }
            animations.set(index, animation);
            GameSprite sprite = sprites.get(index);
            TextureRegion region;
            try {
                region = (TextureRegion) animation.getKeyFrame(animationTimes.get(index));
            } catch (ArrayIndexOutOfBoundsException | ArithmeticException e) {
                throw new RuntimeException("Animation doesn't exist: " + animationName);
            }
            sprite.setRegion(region);
        }
    }

    public void setAnimationTime(int index, float time) {
        animationTimes.set(index, time);
    }

    public float getAnimationTime(int index) {
        return animationTimes.get(index);
    }

    public void setAlpha(int index, float alpha) {
        GameSprite sprite = sprites.get(index);
        sprite.setAlpha(alpha);
    }

    public void setColor(int index, float r, float g, float b) {
        GameSprite sprite = sprites.get(index);
        sprite.setColor(r, g, b, sprite.getColor().a);
    }

    @Override
    public void preUpdate(float deltaTime) {
        for (int i = 0; i < animations.size(); i++) {
            animationTimes.set(i, animationTimes.get(i) + deltaTime);
        }
    }

    public void setVisible(boolean visible) {
        for (GameSprite sprite : sprites) {
            sprite.setVisible(visible);
        }
    }

    public void setAlign(int index, Align align) {
        sprites.get(index).setAlign(align);
    }

    public Align getAlign(int index) {
        return sprites.get(index).getAlign();
    }

    public void flipX(boolean flip) {
        flipX = flip;
    }

    public int getLayerCount() {
        return sprites.size();
    }

    public boolean isFlipX() {
        return flipX;
    }

    public void flipY(boolean flip) {
        flipY = flip;
    }

    public boolean isFlipY() {
        return flipY;
    }

    private void processAppearingEvent() {
        onScreen = isAlmostOnScreen();
        if (onScreen && !wasOnScreen) { // send only once
            messageHandler.send(APPEARS_ON_SCREEN);
        }
        wasOnScreen = onScreen;
    }

    public void draw(Batch batch) {
        processAppearingEvent();
        if (!onScreen) {
            return;
        }
        if (repeatX > 1) {
            for (int j = 0 ; j < repeatX; j++) {
                drawSprites(batch, tileWidth * j + getOffsetX(), getOffsetY());
            }
        } else if (repeatY > 1) {
            for (int j = 0 ; j < repeatY; j++) {
                drawSprites(batch, getOffsetX(), tileHeight * j + getOffsetY());
            }
        } else {
            drawSprites(batch, getOffsetX(), getOffsetY());
        }
    }

    private void drawSprites(Batch batch, float plusX, float plusY) {
        for (int i = 0; i < sprites.size(); i++) {
            GameSprite sprite = sprites.get(i);
            Animation animation = animations.get(i);
            if (animation != null) {
                sprite.setRegion((TextureRegion) animation.getKeyFrame(animationTimes.get(i)));
                if (sprite.isRotate90()) {
                    sprite.rotate90();
                }
            }

            // set position to the "exact" virtual pixels
            /*
            float ratioX = (float)Gdx.graphics.getWidth() / (float)viewport.getWorldWidth();
            float x = ((int)((body.getGlobalX()) * ratioX)) / ratioX;
            float ratioY = (float)Gdx.graphics.getHeight() / (float)viewport.getWorldHeight();
            float y = ((int)((body.getGlobalY()) * ratioY)) / ratioY;
            */
            sprite.setX(body.getGlobalX() + plusX);
            sprite.setY(body.getGlobalY() + plusY);
            sprite.draw(batch, flipX, flipY);
        }
    }

    public float getOffsetY() {
        if (entity.getParent() != null) {
            Entity parent = entity.getParent();
            ViewComponent parentView = parent.getComponent(ViewComponent.class);
            return parentView != null ? offsetY + parentView.getOffsetY() : offsetY;
        }
        return offsetY;
    }

    public float getOffsetX() {
        if (entity.getParent() != null) {
            Entity parent = entity.getParent();
            ViewComponent parentView = parent.getComponent(ViewComponent.class);
            return parentView != null ? offsetX + parentView.getOffsetX() : offsetX;
        }
        return offsetX;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }



    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public boolean isOnScreen() {
        return isOnVirtualScreen(0, 0);
    }

    public boolean isAlmostOnScreen() {
        return isOnVirtualScreen(50, 50);
    }

    public boolean isOnVirtualScreen(float borderH, float borderV) {
        return body.getRight() > camera.position.x - config.getGameMaxVirtualWidth() / 2f - borderH
            && body.getLeft() < camera.position.x + config.getGameMaxVirtualWidth() / 2f + borderH
            && body.getTop() > camera.position.y  - config.getGameVirtualHeight() / 2f - borderV
            && body.getBottom() < camera.position.y + config.getGameVirtualHeight() / 2f + borderV;
    }

}
