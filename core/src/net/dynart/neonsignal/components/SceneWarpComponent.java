package net.dynart.neonsignal.components;

import com.badlogic.gdx.Gdx;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.utils.Direction;

public class SceneWarpComponent extends Component {

    private BodyComponent body;
    private EntityManager entityManager;
    private final String targetName;
    private BodyComponent targetBody;
    private Entity entityForMove;
    private float delay;
    private boolean fade;

    public SceneWarpComponent(String targetName, boolean fade) {
        this.targetName = targetName;
        this.fade = fade;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);

    }

    @Override
    public void update(float delta) {

        // TODO: can not be this in a postInit()?
        // post init
        if (targetBody == null) {
            Entity targetEntity = entityManager.getByName(targetName);
            targetBody = targetEntity.getComponent(BodyComponent.class);
            if (targetBody == null) {
                throw new RuntimeException("Target doesn't exist: " + targetName);
            }
        }

        // update
        if (delay > 0) {
            delay -= delta;
            return;
        }

        if (entityForMove != null) {
            BodyComponent entityBody = entityForMove.getComponent(BodyComponent.class);
            VelocityComponent velocity = entityForMove.getComponent(VelocityComponent.class);
            ViewComponent view = entityForMove.getComponent(ViewComponent.class);
            view.setPaused(false);
            entityBody.setBottom(targetBody.getBottom());
            entityBody.setX(body.getCenterX() - entityBody.getCenterX() > 0
                ? targetBody.getRight() + 10 + entityBody.getHalfWidth()
                : targetBody.getLeft() - 10 - entityBody.getHalfWidth());
            velocity.setInitialX();
            velocity.setInitialY();
            entityForMove = null;
            delay = 0;
        } else {
            for (Entity otherEntity : entityManager.getAllByClass(PlayerComponent.class)) {
                BodyComponent otherBody = otherEntity.getComponent(BodyComponent.class);
                if (body.isOverlap(otherBody)) {
                    entityForMove = otherEntity; // on next update
                    entityForMove.setParent(null);
                    PlayerComponent player = entityForMove.getComponent(PlayerComponent.class);
                    player.cancelDash();
                    ViewComponent view = entityForMove.getComponent(ViewComponent.class);
                    view.setPaused(true);
                    if (fade) delay = 0.5f; // wait for fade out to finish
                }
            }
        }
    }
}
