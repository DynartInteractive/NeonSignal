package net.dynart.neonsignal.core;

import com.badlogic.gdx.utils.Pool;

import net.dynart.neonsignal.components.GridCollisionComponent;
import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.BulletComponent;
import net.dynart.neonsignal.components.OverlapAttackComponent;
import net.dynart.neonsignal.components.VelocityComponent;
import net.dynart.neonsignal.components.ViewComponent;

public class BulletPool extends Pool<Entity> {

    private final Engine engine;

    public BulletPool(Engine engine) {
        this.engine = engine;
    }

    public Entity newObject() {
        Entity result = new Entity(engine);
        ViewComponent viewComponent = new ViewComponent(engine);
        viewComponent.addSprite(new GameSprite(),"fireball");
        BodyComponent body = new BodyComponent(16f, 16f);
        result.addComponents(
            body,
            new VelocityComponent(),
            new BulletComponent(),
            new GridCollisionComponent(),
            new OverlapAttackComponent(1.0f, false),
            viewComponent
        );
        result.postConstruct();
        return result;
    }

}
