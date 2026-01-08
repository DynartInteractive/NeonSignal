package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.components.ViewComponent;
import net.dynart.neonsignal.core.Engine;

public class SetAnimationCommand implements Command {

    private final EntityManager entityManager;
    private final String entityName;
    private final String animation;

    private int layer = 0;
    private Boolean flipX = null;
    private Boolean flipY = null;
    private Float time = null;

    public SetAnimationCommand(Engine engine, String entityName, String animation) {
        this.entityName = entityName;
        this.animation = animation;
        entityManager = engine.getGameScene().getEntityManager();
    }

    public void setLayer(int value) {
        layer = value;
    }

    public void setFlipX(boolean value) {
        flipX = value;
    }

    public void setFlipY(boolean value) {
        flipY = value;
    }

    public void setTime(float value) {
        time = value;
    }

    public boolean act(float delta) {
        Entity entity = entityManager.getByName(entityName);
        ViewComponent view = entity.getComponent(ViewComponent.class);
        view.setAnimation(layer, animation);
        if (flipX != null) {
            view.flipX(flipX);
        }
        if (flipY != null) {
            view.flipY(flipY);
        }
        if (time != null) {
            view.setAnimationTime(layer, time);
        }
        return true;
    }

}
