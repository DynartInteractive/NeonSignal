package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.components.SwitchableComponent;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;

public class TriggerCommand implements Command {

    private final EntityManager entityManager;
    private final String entityName;

    public TriggerCommand(Engine engine, String entityName) {
        this.entityName = entityName;
        entityManager = engine.getGameScene().getEntityManager();
    }

    @Override
    public boolean act(float delta) {
        Entity e = entityManager.getByName(entityName);
        SwitchableComponent sw = e.getComponent(SwitchableComponent.class);
        if (sw == null) {
            throw new RuntimeException("Entity is not switchable: " + entityName);
        }
        sw.switchOn();
        return true;
    }
}
