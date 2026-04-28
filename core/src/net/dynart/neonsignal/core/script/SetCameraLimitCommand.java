package net.dynart.neonsignal.core.script;

import net.dynart.lisa.components.CameraLimitTriggerComponent;
import net.dynart.lisa.core.Engine;
import net.dynart.lisa.core.Entity;
import net.dynart.lisa.core.EntityManager;
import net.dynart.lisa.core.script.Command;

public class SetCameraLimitCommand implements Command {

    private final EntityManager entityManager;
    private final String entityName;

    public SetCameraLimitCommand(Engine engine, String entityName) {
        entityManager = engine.getGameScene().getEntityManager();
        this.entityName = entityName;
    }

    @Override
    public boolean act(float delta) {
        Entity e = entityManager.getByName(entityName);
        CameraLimitTriggerComponent cl = e.getComponent(CameraLimitTriggerComponent.class);
        cl.setLimits();
        return true;
    }
}
