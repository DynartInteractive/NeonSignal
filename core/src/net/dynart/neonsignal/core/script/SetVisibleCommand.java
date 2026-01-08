package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.components.ViewComponent;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;

public class SetVisibleCommand implements Command {
    private final EntityManager entityManager;
    private final String entityName;
    private final boolean visible;

    public SetVisibleCommand(Engine engine, String entityName, boolean visible) {
        this.entityName = entityName;
        this.visible = visible;
        entityManager = engine.getGameScene().getEntityManager();
    }

    public boolean act(float delta) {
        Entity entity = entityManager.getByName(entityName);
        ViewComponent view = entity.getComponent(ViewComponent.class);
        view.setVisible(visible);
        return true;
    }
}
