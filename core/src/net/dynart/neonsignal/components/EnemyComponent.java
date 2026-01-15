package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.GameSprite;
import net.dynart.neonsignal.core.listeners.MessageListener;
import net.dynart.neonsignal.core.ParticlePool;
import net.dynart.neonsignal.core.SoundManager;

public class EnemyComponent extends Component {

    public static final String DAMAGED = "enemy_damaged";

    private EntityManager entityManager;
    private ParticlePool particlePool;
    private final String spritePrefix;
    private BodyComponent body;
    private VelocityComponent velocity;
    private MiniBarComponent miniBar;
    private HealthComponent health;

    private float damageTime;




    public EnemyComponent(String spritePrefix) {
        this.spritePrefix = spritePrefix;
    }

    public String getSpritePrefix() {
        return spritePrefix;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        particlePool = gameScene.getParticlePool();
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);
        health = entity.getComponent(HealthComponent.class);
        messageHandler.subscribe(HealthComponent.DECREASED, (sender, message) -> damage());
        messageHandler.subscribe(HealthComponent.ZERO, (sender, message) -> kill());

        ViewComponent view = entity.getComponent(ViewComponent.class);

        GameSprite sprite = new GameSprite();
        view.addSprite(sprite);
        view.setSprite(1, spritePrefix + "_dead");
        view.setAlign(1, view.getAlign(0)); // copy the align
        sprite.setVisible(false);

        miniBar = entity.getComponent(MiniBarComponent.class);
        if (miniBar != null)
            miniBar.add(entity);

        EntityCollisionComponent entityCollision = entity.getComponent(EntityCollisionComponent.class);
        if (entityCollision != null) {
            entityCollision.setExcludeComponentClass(PlayerComponent.class);
        }
    }

    private void damage() {
        messageHandler.send(DAMAGED);
        damageTime = 0.2f;
        if (miniBar != null)
            miniBar.setVisibleTime(1.0f);
    }

    public float getDamageTime() {
        return damageTime;
    }

    @Override
    public void postUpdate(float delta) {
        if (miniBar != null)
            miniBar.adjustDisplay(health.getValue(), delta);
        if (damageTime > 0) {
            ViewComponent view = entity.getComponent(ViewComponent.class);
            damageTime -= delta;
            if (damageTime < 0) {
                view.getSprite(0).setVisible(true);
                view.getSprite(1).setVisible(false);
            } else {
                view.getSprite(0).setVisible(false);
                view.getSprite(1).setVisible(true);
            }
        }
    }

    private void kill() {
        PlayerComponent player = engine.getGameScene().getPlayer().getComponent(PlayerComponent.class);
        player.incKnockoutCount();

        entity.remove();
        Entity particle = particlePool.obtain();
        BodyComponent particleBody = particle.getComponent(BodyComponent.class);
        particleBody.copySize(body);
        particleBody.setLeft(body.getLeft());
        particleBody.setBottom(body.getBottom());
        VelocityComponent particleVelocity = particle.getComponent(VelocityComponent.class);
        particleVelocity.setX(velocity.getX() / 2f);
        particleVelocity.setY(100f);
        particleVelocity.setGravity(config.getDefaultGravity() * 1.5f);
        ViewComponent particleView = particle.getComponent(ViewComponent.class);
        particleView.setSprite(0, spritePrefix + "_dead");
        ViewComponent entityView = entity.getComponent(ViewComponent.class);
        particleView.flipX(entityView.isFlipX());
        entityManager.add(particle);
    }

}
