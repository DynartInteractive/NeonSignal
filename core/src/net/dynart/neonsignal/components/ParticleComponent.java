package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.GameSprite;
import net.dynart.neonsignal.core.ParticlePool;
import net.dynart.neonsignal.core.utils.Align;

public class ParticleComponent extends Component {

    private ParticlePool particlePool;
    private VelocityComponent velocity;
    private BodyComponent body;
    private ViewComponent view;
    private float lifeTime;
    private float time;
    private float alphaChange;
    private float alpha;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        particlePool = gameScene.getParticlePool();
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);
    }

    public void init() {
        time = 0;
        lifeTime = 0;
        alpha = 1.0f;
        alphaChange = 0;
        velocity.setX(0);
        velocity.setY(0);
        velocity.setGravity(0);
        body.setAlign(Align.CENTER_BOTTOM);
        view = entity.getComponent(ViewComponent.class);
        view.setAnimationTime(0, 0);
        view.setOffsetX(0);
        view.setOffsetY(0);
        view.setVisible(true);
        view.setAnimation(0, null);
        view.setAlpha(0, alpha);
        view.setAlign(0, Align.CENTER_BOTTOM);
        view.setLayer(100);
        view.flipX(false);
        view.flipY(false);
        GameSprite sprite = view.getSprite(0);
        sprite.flip(false, false);
        entity.setActive(true);
    }

    public void setLifeTime(float lifeTime) {
        this.lifeTime = lifeTime;
    }

    public void setAlphaChange(float alphaChange) {
        this.alphaChange = alphaChange;
    }

    @Override
    public void update(float delta) {
        time += delta;
        if (alphaChange != 0) {
            alpha -= alphaChange * delta;
            if (alpha < 0) {
                alpha = 0;
            } else if (alpha > 1) {
                alpha = 1;
            }
            view.setAlpha(0, alpha);
        }
        if (body.getTop() < 0 || (lifeTime > 0 && time >= lifeTime)) {
            entity.remove();
            particlePool.free(entity);
        }
    }

}
