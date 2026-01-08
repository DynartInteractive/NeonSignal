package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.CameraHandler;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.Screen;
import net.dynart.neonsignal.core.Engine;

public class MoveCameraToCommand implements Command {

    private final CameraHandler cameraHandler;
    private final EntityManager entityManager;
    private final String target;
    private final Screen cutsceneScreen;
    private final Engine engine;
    private final float speed;

    public MoveCameraToCommand(Engine engine, String target, float speed) {
        this.engine = engine;
        this.target = target;
        this.speed = speed;
        cameraHandler = engine.getGameScene().getCameraHandler();
        entityManager = engine.getGameScene().getEntityManager();
        cutsceneScreen = engine.getScreen("cutscene");
    }

    public boolean act(float delta) {
        Entity entity = entityManager.getByName(target);
        if (engine.getCurrentScreen() == cutsceneScreen && !entityManager.isInAnimation()) {
            cameraHandler.setTarget(entity);
            return true;
        }
        cameraHandler.moveTo(entity, speed);
        return true;
    }
}
