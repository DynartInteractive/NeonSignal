package net.dynart.neonsignal.components;

import com.badlogic.gdx.Gdx;

import net.dynart.neonsignal.core.BulletPool;
import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.listeners.MessageListener;
import net.dynart.neonsignal.core.ParticleEffectManager;
import net.dynart.neonsignal.core.ParticlePool;
import net.dynart.neonsignal.core.SoundManager;
import net.dynart.neonsignal.core.utils.Align;

public class BulletComponent extends Component {

    private BodyComponent body;
    private VelocityComponent velocity;
    private EntityManager entityManager;
    private SoundManager soundManager;
    private BulletPool bulletPool;
    private ParticlePool particlePool;
    private ParticleEffectManager particleEffectManager;
    private float lifeTime = -1;
    private float elapsedTime;
    private boolean explosive;
    private String[] hitSounds;
    private String sparkEffect;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        soundManager = engine.getSoundManager();
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        bulletPool = gameScene.getBulletPool();
        particlePool = gameScene.getParticlePool();
        particleEffectManager = gameScene.getParticleEffectManager();
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);

        MessageListener collisionListener = new MessageListener() {
            @Override
            public void receive(Entity sender, String message) {
                collided();
            }
        };

        messageHandler.subscribe(GridCollisionComponent.LEFT_COLLISION, collisionListener);
        messageHandler.subscribe(GridCollisionComponent.RIGHT_COLLISION, collisionListener);
        messageHandler.subscribe(GridCollisionComponent.TOP_COLLISION, collisionListener);
        messageHandler.subscribe(GridCollisionComponent.BOTTOM_COLLISION, collisionListener);
        messageHandler.subscribe(OverlapAttackComponent.ATTACK, collisionListener);
    }

    public void setLifeTime(float value) {
        lifeTime = value;
    }

    public void setExplosive(boolean value) {
        explosive = value;
    }

    public void setHitSounds(String[] value) {
        hitSounds = value;
    }

    public void setSparkEffect(String value) {
        sparkEffect = value;
    }

    @Override
    public void update(float delta) {
        elapsedTime += delta;
        if (lifeTime != -1 && elapsedTime > lifeTime) {
            remove();
        }
    }

    private void remove() {
        elapsedTime = 0;
        entity.remove();
        bulletPool.free(entity);
    }

    private void collided() {
        elapsedTime = 0;

        OverlapAttackComponent overlapAttack = entity.getComponent(OverlapAttackComponent.class);
        overlapAttack.setActive(false);

        if (hitSounds.length > 0) {
            Gdx.app.log("Bullet", "Hit volume: " + entity.getVolumeRelatedToPlayer());

            soundManager.playRandom(hitSounds, entity.getVolumeRelatedToPlayer());
        }

        // Spawn spark effect rotated based on bullet velocity
        if (sparkEffect != null && velocity != null) {
            particleEffectManager.spawnWithVelocity(
                sparkEffect,
                body.getCenterX(), body.getCenterY(),
                velocity.getLastX(), velocity.getLastY(),
                false
            );
        }

        remove();
        if (explosive) {
            Entity particle = particlePool.obtain();
            BodyComponent explosionBody = particle.getComponent(BodyComponent.class);
            ParticleComponent explosionParticle = particle.getComponent(ParticleComponent.class);
            explosionParticle.setLifeTime(0.20f);
            ViewComponent explosionView = particle.getComponent(ViewComponent.class);
            explosionView.setAnimation(0, "fireball_explosion");
            explosionBody.setSize(explosionView.getSpriteWidth(0), explosionView.getSpriteHeight(0));
            explosionBody.setAlign(Align.CENTER);
            explosionBody.setX(body.getCenterX());
            explosionBody.setY(body.getCenterY());
            explosionView.setAlign(0, Align.CENTER);
            entityManager.add(particle);
            if (explosionView.isOnScreen()) {
                soundManager.play("fireball_explosion", entity.getVolumeRelatedToPlayer());
            }
        }
    }

}
