package net.dynart.neonsignal.core.listeners;

import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.utils.Direction;

public interface CollisionListener {
    void collide(Entity entity, Direction direction);
}
