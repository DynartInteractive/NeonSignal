package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.CameraHandler;
import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;

public class CameraLimitTriggerComponent extends Component {

    private static String lastGroup = "";
    private static Entity last;

    private final String[] targets = { "", "", "", "" };
    private final boolean fade;
    private final boolean instant;


    public CameraLimitTriggerComponent(String leftTargetName, String rightTargetName, String topTargetName, String bottomTargetName, boolean fade, boolean instant) {
        targets[CameraHandler.LEFT] = leftTargetName;
        targets[CameraHandler.RIGHT] = rightTargetName;
        targets[CameraHandler.TOP] = topTargetName;
        targets[CameraHandler.BOTTOM] = bottomTargetName;
        this.fade = fade;
        this.instant = instant;
    }

    @Override
    public void update(float delta) {
        if (last == entity) {
            return;
        }
        BodyComponent body = entity.getComponent(BodyComponent.class);
        Entity player = engine.getGameScene().getPlayer();
        BodyComponent playerBody = player.getComponent(BodyComponent.class);
        if (!playerBody.isInAir() && body.isOverlap(playerBody) && !lastGroup.equals(entity.getGroup())) {
            lastGroup = entity.getGroup() == null ? "" : entity.getGroup();
            setLimits();
        }
    }

    public void setLimits() {
        last = entity;
        CameraHandler camHandler = engine.getGameScene().getCameraHandler();
        EntityManager entityManager = engine.getGameScene().getEntityManager();
        for (int i = 0; i < 4; i++) {
            if (targets[i].isEmpty()) {
                camHandler.setNewLimit(i, -1);
                continue;
            }
            Entity target = entityManager.getByName(targets[i]);
            if (target == null) {
                throw new RuntimeException("Target doesn't exist: " + targets[i]);
            }
            BodyComponent targetBody = target.getComponent(BodyComponent.class);
            switch (i) {
                case CameraHandler.LEFT:
                    camHandler.setNewLimit(i, targetBody.getRight());
                    break;
                case CameraHandler.RIGHT:
                    camHandler.setNewLimit(i, targetBody.getLeft());
                    break;
                case CameraHandler.TOP:
                    camHandler.setNewLimit(i, targetBody.getBottom());
                    break;
                case CameraHandler.BOTTOM:
                    camHandler.setNewLimit(i, targetBody.getTop());
                    break;
            }
        }
        camHandler.changeLimit(fade, instant);
    }


}
