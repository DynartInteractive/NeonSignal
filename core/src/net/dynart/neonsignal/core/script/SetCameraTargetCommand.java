package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.CameraHandler;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.Engine;

public class SetCameraTargetCommand implements Command {

    private final String target;
    private final CameraHandler cameraHandler;
    private final EntityManager entityManager;
    private final boolean smooth;

    public SetCameraTargetCommand(Engine engine, String target, boolean smooth) {
        cameraHandler = engine.getGameScene().getCameraHandler();
        entityManager = engine.getGameScene().getEntityManager();
        this.target = target;
        this.smooth = smooth;
    }

    public boolean act(float delta) {
        Entity entity = entityManager.getByName(target);
        cameraHandler.setTarget(entity);
        if (smooth) {
            cameraHandler.startToChangeLimit();
        }
        return true;
    }
}
