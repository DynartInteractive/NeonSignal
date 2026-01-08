package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.BulletFactory;
import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class BulletSpawnerComponent extends Component {

    private BulletFactory bulletFactory;
    private float elapsedTime;
    private BodyComponent body;
    private float rotation;

    public BulletSpawnerComponent() {
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        bulletFactory = engine.getGameScene().getBulletFactory();
        body = entity.getComponent(BodyComponent.class);
    }

    @Override
    public void update(float delta) {
        elapsedTime += delta;
        rotation += delta * 100f;
        if (elapsedTime > 0.1f) {
            elapsedTime = 0;
            double vx = Math.cos(rotation/180f*3.14f) * 140f;
            double vy = Math.sin(rotation/180f*3.14f) * 140f;
            Entity bullet = bulletFactory.create(body, (float)vx, (float)vy, false, false, true, 0.6f, rotation, true);
            BulletComponent bulletComp = bullet.getComponent(BulletComponent.class);
            bulletComp.setExplosive(true);
        }
    }
}
