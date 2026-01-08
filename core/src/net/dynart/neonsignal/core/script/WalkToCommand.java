package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.VelocityComponent;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.screens.CutsceneScreen;

public class WalkToCommand implements Command {

    private final String entityName;
    private final String targetName;
    private final boolean exact;
    private final EntityManager entityManager;
    private final EngineConfig config;
    private final CutsceneScreen cutsceneScreen;
    private Float signToTarget;

    public WalkToCommand(Engine engine, String entityName, String targetName, boolean exact) {
        this.targetName = targetName;
        this.entityName = entityName;
        this.exact = exact;
        entityManager = engine.getGameScene().getEntityManager();
        config = engine.getConfig();
        cutsceneScreen = (CutsceneScreen)engine.getScreen("cutscene");
    }

    public boolean act(float delta) {

        // get the entities
        Entity entity = entityManager.getByName(entityName);
        Entity target = entityManager.getByName(targetName);
        BodyComponent targetBody = target.getComponent(BodyComponent.class);
        BodyComponent entityBody = entity.getComponent(BodyComponent.class);

        // set velocity
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        float speed = config.getPlayerMaxRunningVelocity();
        float sign = Math.signum(targetBody.getCenterX() - entityBody.getCenterX());
        velocity.setX(speed * sign);
        velocity.setAcceleration(0);
        if (signToTarget == null) {
            signToTarget = sign;
        }

        // check for finish
        if (exact && signToTarget != sign) {
            return finishWalk(entity, target, sign);
        }
        if (!exact && entityBody.isOverlap(targetBody)) {
            return finishWalk(entity, target, sign);
        }
        if (cutsceneScreen.isAnimationFinished()) {
            return finishWalk(entity, target, sign);
        }

        return false;
    }

    private boolean finishWalk(Entity entity, Entity target, float sign) {
        BodyComponent targetBody = target.getComponent(BodyComponent.class);
        BodyComponent entityBody = entity.getComponent(BodyComponent.class);
        if (exact) {
            entityBody.setX(targetBody.getCenterX());
        } else if (sign > 0) { // goes left
            entityBody.setRight(targetBody.getLeft());
        } else { // goes right
            entityBody.setLeft(targetBody.getRight());
        }
        entityBody.setBottom(targetBody.getBottom());
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.setX(0);
        velocity.setY(0);
        return true;
    }
}
