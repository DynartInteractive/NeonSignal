package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;

public class SecretComponent extends Component {

    public static final String FOUND = "secret_found";
    public static final String SHOW = "secret_show";
    public static final String HIDE = "secret_hide";

    private BodyComponent body;
    private BodyComponent mustLeaveBody;
    private EntityManager entityManager;
    private boolean found;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);
    }

    @Override
    public void update(float delta) {
        for (Entity otherEntity : entityManager.getAllByClass(PlayerComponent.class)) {
            BodyComponent otherBody = otherEntity.getComponent(BodyComponent.class);
            boolean overlap = body.isOverlap(otherBody);
            if (overlap && otherBody != mustLeaveBody) {
                if (!found) {
                    found = true;
                    messageHandler.send(FOUND);
                }
                messageHandler.send(SHOW);
                mustLeaveBody = otherBody;
            }
            if (!overlap && otherBody == mustLeaveBody) {
                messageHandler.send(HIDE);
                mustLeaveBody = null;
            }
        }
    }
}
