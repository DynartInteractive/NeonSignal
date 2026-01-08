package net.dynart.neonsignal.core;

import com.badlogic.gdx.utils.Pool;

import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.ParticleComponent;
import net.dynart.neonsignal.components.VelocityComponent;
import net.dynart.neonsignal.components.ViewComponent;

public class ParticlePool extends Pool<Entity> {

    private final Engine engine;

    public ParticlePool(Engine engine) {
        this.engine = engine;
    }

    public Entity newObject() {
        Entity result = new Entity(engine);
        ViewComponent viewComponent = new ViewComponent(engine);
        viewComponent.addSprite(new GameSprite(), "dust");
        viewComponent.setLayer(EntityManager.TOP_LAYER);
        result.addComponents(
            new BodyComponent(0, 0),
            new VelocityComponent(),
            new ParticleComponent(),
            viewComponent
        );
        result.postConstruct();
        return result;
    }

    @Override
    public Entity obtain() {
        Entity result = super.obtain();
        ParticleComponent particle = result.getComponent(ParticleComponent.class);
        particle.init();
        return result;
    }

}
