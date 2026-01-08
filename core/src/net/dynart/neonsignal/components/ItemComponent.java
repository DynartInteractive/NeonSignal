package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

import java.util.Random;

public class ItemComponent extends Component {

    private final String name;

    private ViewComponent view;
    private float counter;

    public ItemComponent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        Random random = new Random();
        counter = random.nextFloat() * 10f;
        view = entity.getComponent(ViewComponent.class);
    }

    @Override
    public void update(float delta) {
        counter += delta;
        view.setOffsetY((float)Math.sin(counter * 10f)*2f + 1f);
    }

}
