package net.dynart.neonsignal.components;

import java.util.Set;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.listeners.MessageListener;
import net.dynart.neonsignal.core.utils.Direction;
import net.dynart.neonsignal.core.Grid;

public class EntityCollisionComponent extends Component {

    public static final String LEFT_COLLISION = "entity_left_collision";
    public static final String RIGHT_COLLISION = "entity_right_collision";
    public static final String BOTTOM_COLLISION = "entity_bottom_collision";
    public static final String TOP_COLLISION = "entity_top_collision";

    private EntityManager entityManager;
    private BodyComponent body;
    private VelocityComponent velocity;
    private Entity collidedWith;

    private Class excludeComponentClass = null;
    private boolean gridCollision;

    public EntityCollisionComponent() {
        super();
    }

    public EntityCollisionComponent(Class excludeClass) {
        excludeComponentClass = excludeClass;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);

        messageHandler.subscribe(VelocityComponent.X_ADDED, (sender, message) -> collide(true));
        messageHandler.subscribe(VelocityComponent.Y_ADDED, (sender, message) -> collide(false));

        MessageListener gridCollisionListener = (sender, message) -> gridCollision = true;

        messageHandler.subscribe(GridCollisionComponent.LEFT_COLLISION, gridCollisionListener);
        messageHandler.subscribe(GridCollisionComponent.TOP_COLLISION, gridCollisionListener);
        messageHandler.subscribe(GridCollisionComponent.BOTTOM_COLLISION, gridCollisionListener);
        messageHandler.subscribe(GridCollisionComponent.RIGHT_COLLISION, gridCollisionListener);

    }

    public Entity getCollidedWith() {
        return collidedWith;
    }

    public void setExcludeComponentClass(Class cls) {
        excludeComponentClass = cls;
    }

    @Override
    public void preUpdate(float delta) {
        gridCollision = false;
    }

    private void collide(boolean horizontal) {
        if (!active) {
            return;
        }

        Set<Entity> otherEntities = entityManager.getAllByClassAndArea(ColliderComponent.class, entity);
        for (Entity otherEntity : otherEntities) {

            if (otherEntity.hasComponent(excludeComponentClass)) {
                continue;
            }
            this.collidedWith = otherEntity;
            BodyComponent otherBody = otherEntity.getComponent(BodyComponent.class);
            ColliderComponent otherCollider = otherEntity.getComponent(ColliderComponent.class);
            if (skipCheck(otherEntity, otherBody, otherCollider)) {
                continue;
            }
            Direction direction = body.getDirection(horizontal, otherBody);
            if (direction == null) { // possible bug?
                continue;
            }
            otherCollider.handleCollision(entity, direction.inverse());
            handleCollision(direction, otherBody);
            if (hasToKillBy(otherEntity)) {
                kill();
            }
        }
        // reset the grid collision after the horizontal check
        if (horizontal) {
            gridCollision = false;
        }
    }

    private boolean skipCheck(Entity otherEntity, BodyComponent otherBody, ColliderComponent otherCollider) {
        return entity == otherEntity
            || !otherEntity.isActive()
            || !otherCollider.isActive()
            || !body.isOverlap(otherBody);
    }


    private void handleCollision(Direction direction, BodyComponent otherBody) {
        switch (direction) {
            case LEFT:
                body.setLeft(otherBody.getRight() + GridCollisionComponent.CORRIGATION);
                body.setLeftCollision(true);
                velocity.setX(0);
                messageHandler.send(LEFT_COLLISION);
                break;
            case RIGHT:
                body.setRight(otherBody.getLeft() - GridCollisionComponent.CORRIGATION);
                body.setRightCollision(true);
                velocity.setX(0);
                messageHandler.send(RIGHT_COLLISION);
                break;
            case DOWN:
                body.setBottom(otherBody.getTop() + GridCollisionComponent.CORRIGATION);
                body.setInAir(false);
                body.setBottomCollision(true);
                velocity.setY(0);
                messageHandler.send(BOTTOM_COLLISION);
                break;
            case UP:
                body.setTop(otherBody.getBottom() - GridCollisionComponent.CORRIGATION);
                body.setTopCollision(true);
                velocity.setY(0);
                messageHandler.send(TOP_COLLISION);
                break;
        }
    }

    private boolean hasToKillBy(Entity otherEntity) {
        Grid grid = engine.getGameScene().getGrid();
        return gridCollision
            && otherEntity.hasComponent(PusherComponent.class)
            && entity.hasComponent(HealthComponent.class)
            && grid.bodyInBlock(body);
    }

    private void kill() {
        HealthComponent health = entity.getComponent(HealthComponent.class);
        health.kill();
    }

}
