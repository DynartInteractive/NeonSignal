package net.dynart.neonsignal.core;

import net.dynart.neonsignal.components.GridCollisionComponent;
import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.BulletComponent;
import net.dynart.neonsignal.components.OverlapAttackComponent;
import net.dynart.neonsignal.components.VelocityComponent;
import net.dynart.neonsignal.components.ViewComponent;
import net.dynart.neonsignal.core.utils.Align;

public class BulletFactory {

    protected SoundManager soundManager;
    protected BulletPool bulletPool;
    protected EntityManager entityManager;

    public BulletFactory(Engine engine) {
        bulletPool = engine.getGameScene().getBulletPool();
        entityManager = engine.getGameScene().getEntityManager();
        soundManager = engine.getSoundManager();
    }

    public void createFireball(BodyComponent onBody, boolean flipX) {
        create(onBody, flipX ? -240f : 240f, 0, flipX, false, true, -1, 0, false);
    }

    public Entity create(BodyComponent onBody, float velocityX, float velocityY, boolean flipX, boolean flipY, boolean collideWithGrid, float lifeTime, float rotation, boolean enemyFire) {
        Entity bulletEntity = bulletPool.obtain();
        bulletEntity.setParent(null);

        BodyComponent body = bulletEntity.getComponent(BodyComponent.class);
        body.setAlign(Align.CENTER);
        body.setX(onBody.getCenterX());
        body.setY(onBody.getCenterY());
        body.setSize(16, 16);

        GridCollisionComponent gridCollision = bulletEntity.getComponent(GridCollisionComponent.class);
        gridCollision.setActive(collideWithGrid);

        VelocityComponent velocity = bulletEntity.getComponent(VelocityComponent.class);
        velocity.setX(velocityX);
        velocity.setY(velocityY);

        OverlapAttackComponent overlapAttack = bulletEntity.getComponent(OverlapAttackComponent.class);
        overlapAttack.setActive(true);
        overlapAttack.setExceptBody(onBody);
        overlapAttack.setPower(1.0f);
        overlapAttack.setEnemy(enemyFire);

        ViewComponent view = bulletEntity.getComponent(ViewComponent.class);
        view.setAnimationTime(0, 0);
        view.setAnimation(0, "fireball");
        view.flipX(flipX);
        view.flipY(flipY);
        view.setAlign(0, Align.CENTER);
        view.setRotation(0, rotation);
        view.setVisible(true);

        BulletComponent bullet = bulletEntity.getComponent(BulletComponent.class);
        bullet.setLifeTime(lifeTime);
        bullet.setCollisionSound(null);
        bullet.setExplosive(true);

        bulletEntity.setActive(true);
        entityManager.add(bulletEntity);

        return bulletEntity;
    }
}
