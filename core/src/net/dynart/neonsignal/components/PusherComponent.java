package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.MessageHandler;
import net.dynart.neonsignal.core.listeners.MessageListener;
import net.dynart.neonsignal.core.utils.Direction;
import net.dynart.neonsignal.core.Grid;

public class PusherComponent extends Component {

    private EntityManager entityManager;
    private BodyComponent body;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        entityManager = engine.getGameScene().getEntityManager();
        body = entity.getComponent(BodyComponent.class);
        MessageHandler messageHandler = entity.getRoot().getMessageHandler(); // ???
        messageHandler.subscribe(VelocityComponent.Y_ADDED, (sender, message) -> push(false));
        messageHandler.subscribe(VelocityComponent.X_ADDED, (sender, message) -> push(true));
    }

    public void push(boolean horizontal) {
        for (Entity otherEntity : entityManager.getAllByClassAndArea(ColliderComponent.class, entity)) {
            if (otherEntity.hasComponent(PusherComponent.class)) { // do not push other pushers
                continue;
            }
            BodyComponent otherBody = otherEntity.getComponent(BodyComponent.class);
            if (otherBody == null || !body.isOverlap(otherBody)) {
                continue;
            }
            Direction direction = body.getDirection(horizontal, otherBody);
            if (direction == null) {
                continue;
            }
            switch (direction) {
                case LEFT:
                    otherBody.setRight(body.getLeft() - GridCollisionComponent.CORRIGATION);
                    break;
                case RIGHT:
                    otherBody.setLeft(body.getRight() + GridCollisionComponent.CORRIGATION);
                    break;
                case DOWN:
                    otherBody.setTop(body.getBottom() - GridCollisionComponent.CORRIGATION);
                    break;
                case UP:
                    otherBody.setBottom(body.getTop() + GridCollisionComponent.CORRIGATION);
                    break;
            }
            if ((direction == Direction.DOWN || direction == Direction.UP) && otherEntity.hasComponent(MountableComponent.class)) {
                MountableComponent mountable = otherEntity.getComponent(MountableComponent.class);
                mountable.dismount(null);
            }

            Grid grid = engine.getGameScene().getGrid();

            boolean inBlockAndCollider = grid.bodyInBlock(otherBody) || otherBody.overlapOther(ColliderComponent.class, body) != null;
            if (inBlockAndCollider) {
                if (otherEntity.hasComponent(HealthComponent.class)) {
                    HealthComponent health = otherEntity.getComponent(HealthComponent.class);
                    health.kill();
                } else if (otherEntity.hasComponent(OverlapAttackableComponent.class)) {
                    OverlapAttackableComponent attackable = otherEntity.getComponent(OverlapAttackableComponent.class);
                    attackable.attacked(entity);
                }
            }

            // sticky problem
            if (otherEntity.hasComponent(PlayerComponent.class)) {
                PlayerComponent player = otherEntity.getComponent(PlayerComponent.class);
                player.setPushedBy(entity);
            }
        }
    }

}
