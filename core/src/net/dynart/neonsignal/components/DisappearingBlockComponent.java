package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;

public class DisappearingBlockComponent extends Component {

    private final float time;
    private final String animPrefix;

    private EntityManager entityManager;
    private OverlapAttackableComponent overlapAttackable;
    private ColliderComponent collider;
    private ViewComponent view;
    private BodyComponent body;
    private float currentTime;
    private boolean visible;

    public DisappearingBlockComponent(float time, boolean visible, String animPrefix) {
        this.time = time;
        this.visible = visible;
        this.animPrefix = animPrefix;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);
        overlapAttackable = entity.getComponent(OverlapAttackableComponent.class);
        collider = entity.getComponent(ColliderComponent.class);
        view = entity.getComponent(ViewComponent.class);
    }

    @Override
    public void preUpdate(float delta) {
        BodyComponent mustLeaveBody = null;
        for (Entity otherEntity : entityManager.getAllByClass(PlayerComponent.class)) {
            BodyComponent otherBody = otherEntity.getComponent(BodyComponent.class);
            if (otherBody.isOverlap(body)) {
                mustLeaveBody = otherBody;
                break;
            }
        }
        currentTime += delta;
        if (currentTime >= time) {
            currentTime = 0;
            visible = !visible;
            if (visible) {
                view.setAnimation(0, animPrefix + "_in");
                view.setAnimationTime(0, 0);
            } else {
                view.setAnimation(0, animPrefix + "_out");
                view.setAnimationTime(0, 0);
                overlapAttackable.setActive(false);
                collider.setActive(false);
            }
        }
        if (visible && mustLeaveBody == null) {
            overlapAttackable.setActive(true);
            collider.setActive(true);
        }
    }
}
