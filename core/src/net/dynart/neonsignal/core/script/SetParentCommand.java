package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.EntityManager;

public class SetParentCommand implements Command {

    private final EntityManager entityManager;
    private final String entity;
    private final String parent;

    public SetParentCommand(Engine engine, String entity, String parent) {
        entityManager = engine.getGameScene().getEntityManager();
        this.entity = entity;
        this.parent = parent;
    }

    public boolean act(float delta) {
        entityManager.getByName(entity).setParent(entityManager.getByName(parent));
        return true;
    }
}
