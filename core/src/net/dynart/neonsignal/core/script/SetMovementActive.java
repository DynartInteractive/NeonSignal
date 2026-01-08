package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.components.MovableComponent;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;

public class SetMovementActive implements Command, SkippableCommand {
    private final boolean active;
    private final boolean finishOnSkip;
    private final MovableComponent movable;
    private final Entity entity;

    public SetMovementActive(Engine engine, String entityName, boolean active, boolean finishOnSkip, int finishIndex) {
        this.active = active;
        this.finishOnSkip = finishOnSkip;
        EntityManager entityManager = engine.getGameScene().getEntityManager();
        entity = entityManager.getByName(entityName);
        movable = entity.getComponent(MovableComponent.class);
        movable.setFinishIndex(finishIndex);
    }

    public boolean act(float delta) {
        entity.setActive(true);
        movable.setActive(active);
        return true;
    }

    public void skip() {
        if (finishOnSkip) {
            movable.finish();
        }
    }
}
