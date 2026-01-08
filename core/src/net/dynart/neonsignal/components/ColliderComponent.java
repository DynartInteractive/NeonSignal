package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.listeners.CollisionListener;
import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.utils.Direction;

import java.util.ArrayList;
import java.util.List;

public class ColliderComponent extends Component {

    private final List<CollisionListener> listeners = new ArrayList<>();

    public void addListener(CollisionListener listener) {
        this.listeners.add(listener);
    }

    public void handleCollision(Entity entity, Direction direction) {
        for (CollisionListener listener : listeners) {
            listener.collide(entity, direction);
        }
    }

}
