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

    public Entity create(BulletOptions o) {
        Entity bulletEntity = bulletPool.obtain();
        bulletEntity.setParent(null);

        BodyComponent body = bulletEntity.getComponent(BodyComponent.class);
        body.setAlign(Align.CENTER);
        body.setX(o.positionX != null ? o.positionX : o.ownerBody.getCenterX());
        body.setY(o.positionY != null ? o.positionY : o.ownerBody.getCenterY());
        body.setSize(o.sizeX != null ? o.sizeX : 16, o.sizeY != null ? o.sizeY : 16);

        GridCollisionComponent gridCollision = bulletEntity.getComponent(GridCollisionComponent.class);
        gridCollision.setActive(o.collideWithGrid);

        VelocityComponent velocity = bulletEntity.getComponent(VelocityComponent.class);
        velocity.setX(o.velocityX);
        velocity.setY(o.velocityY);

        OverlapAttackComponent overlapAttack = bulletEntity.getComponent(OverlapAttackComponent.class);
        overlapAttack.setActive(true);
        overlapAttack.setExceptBody(o.ownerBody);
        overlapAttack.setPower(o.power);
        overlapAttack.setEnemy(o.enemyFire);

        ViewComponent view = bulletEntity.getComponent(ViewComponent.class);
        view.setAnimationTime(0, 0);
        view.setAnimation(0, o.sprite != null ? o.sprite : "player_bullet1");
        view.flipX(o.flipX);
        view.flipY(o.flipY);
        view.setAlign(0, Align.CENTER);
        view.setRotation(0, o.rotation);
        view.setVisible(true);

        BulletComponent bullet = bulletEntity.getComponent(BulletComponent.class);
        bullet.setLifeTime(o.lifeTime);
        bullet.setHitSounds(o.hitSounds);
        bullet.setExplosive(o.explosive);
        bullet.setSparkEffect(o.sparkEffect);

        bulletEntity.setActive(true);
        entityManager.add(bulletEntity);

        return bulletEntity;
    }
}
