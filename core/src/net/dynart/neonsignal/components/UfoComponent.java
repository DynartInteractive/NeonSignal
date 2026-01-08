package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class UfoComponent extends Component {

    private ViewComponent view;
    private float elapsed;

    public UfoComponent() {
    }

    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        view = entity.getComponent(ViewComponent.class);
    }

    @Override
    public void update(float delta) {
        elapsed += delta;
        float oy = (float)Math.sin(elapsed * 10f);
        view.setOffsetY(oy);
    }

}
