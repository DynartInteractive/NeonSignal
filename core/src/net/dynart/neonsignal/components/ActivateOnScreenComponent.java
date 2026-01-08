package net.dynart.neonsignal.components;

import com.badlogic.gdx.Gdx;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class ActivateOnScreenComponent extends Component {

    public ActivateOnScreenComponent() {
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        messageHandler.subscribe(ViewComponent.APPEARS_ON_SCREEN, (sender, message) -> {
            if (entity.isActive()) {
                return;
            }
            if (entity.getName() != null && !entity.getName().isEmpty()) {
                Gdx.app.log("ActivateOnScreenComponent", "Activating entity: " + entity.getName());
                BodyComponent body = entity.getComponent(BodyComponent.class);
                Gdx.app.log("ActivateOnScreenComponent", "Entity position: " + body.getCenterX() + ", " + body.getCenterY());
            }
            if (entity.getGroup() == null || entity.getGroup().isEmpty()) {
                entity.setActive(true);
            } else {
                GameScene gameScene = engine.getGameScene();
                EntityManager entityManager = gameScene.getEntityManager();
                entityManager.enableGroup(entity.getGroup());
            }
        });
    }

}
