package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;

public class SceneWarpComponent extends Component {

    // Warp just before fade completes (while screen is black), before CameraHandler switches screens
    private static final float FADE_DELAY = 0.45f;

    private BodyComponent body;
    private EntityManager entityManager;
    private final String targetName;
    private BodyComponent targetBody;
    private Entity entityForMove;
    private float fadeTimer;
    private final boolean fade;

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

        // Wait for fade delay before warping (fade is triggered by CameraHandler)
        if (fadeTimer > 0) {
            fadeTimer -= delta;
            if (fadeTimer > 0) {
                return;
            }
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
                    if (fade) {
                        fadeTimer = FADE_DELAY;
                    }
                }
            }
        }
    }
}
