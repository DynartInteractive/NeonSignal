package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.components.CameraLimitTriggerComponent;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;

public class SetCameraLimitCommand implements Command  {

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
