package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;

public class ReviveComponent extends Component {

    public static final String TOUCHED = "revive_touched";

    private static Entity current;

    private BodyComponent body;

    public static Entity getCurrent() {
        return current;
    }

    public static void clearCurrent() {
        current = null;
    }

    @Override
    public void postConstruct(Entity entity) {
        super.postConstruct(entity);
        body = entity.getComponent(BodyComponent.class);
    }

    public void update(float delta) {
        if (current != entity
            && (body.overlapOther(PlayerComponent.class) != null)
        ) {
            current = entity;
            messageHandler.send(TOUCHED);
        }
    }
}
