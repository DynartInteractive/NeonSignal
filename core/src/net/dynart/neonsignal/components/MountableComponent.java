package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class MountableComponent extends Component {

    public static final String MOUNTED = "mountable_mounted";

    private BodyComponent body;
    private Entity platformEntity;
    private BodyComponent platformBody;
    private VelocityComponent velocity;
    private BodyComponent mustLeaveBody;
    private boolean canDrop;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);

        messageHandler.subscribe(GridCollisionComponent.BOTTOM_COLLISION, (sender, message) -> dismount(null));
        messageHandler.subscribe(GridCollisionComponent.TOP_COLLISION, (sender, message) -> dismount(platformBody));
        messageHandler.subscribe(EntityCollisionComponent.BOTTOM_COLLISION, (sender, message) -> {
            EntityCollisionComponent collision = entity.getComponent(EntityCollisionComponent.class);
            Entity otherEntity = collision.getCollidedWith();
            if (otherEntity != platformEntity) {
                dismount(null);
            }
        });
        messageHandler.subscribe(EntityCollisionComponent.TOP_COLLISION, (sender, message) -> {
            EntityCollisionComponent collision = entity.getComponent(EntityCollisionComponent.class);
            Entity otherEntity = collision.getCollidedWith();
            if (otherEntity != platformEntity) {
                dismount(platformBody);
            }
        });
        messageHandler.subscribe(PlayerComponent.WANTS_TO_DROP, (sender, message) -> {
            if (platformBody != null && canDrop) {
                dismount(platformBody);
            }
        });
    }

    public void mount(Entity toEntity) {
        dismount(platformBody);
        platformEntity = toEntity;
        entity.setParent(toEntity);
        platformBody = toEntity.getComponent(BodyComponent.class);
        canDrop = !toEntity.hasComponent(ColliderComponent.class);
        body.setBottom(platformBody.getTop() + GridCollisionComponent.CORRIGATION); // before setY!
        body.setInAir(false);
        velocity.setY(0);
        velocity.setGravity(0);
        messageHandler.send(MOUNTED);
    }

    public void dismount(BodyComponent mustLeaveBody) {
        if (platformBody == null) {
            return;
        }
        PlatformComponent platform = platformEntity.getComponent(PlatformComponent.class);
        platform.dismount(entity);
        platformEntity = null;
        platformBody = null;
        entity.setParent(null);
        velocity.setInitialY();
        float gravity = config.getDefaultGravity();

        if (entity.hasComponent(WaterCollisionComponent.class)) {
            WaterCollisionComponent waterCollision = entity.getComponent(WaterCollisionComponent.class);
            if (waterCollision.isInWater()) {
                gravity = config.getDefaultGravity() / 5f; // TODO: don't repeat this, PlayerComponent has a similar code
            }
        }

        if (entity.hasComponent(PlayerComponent.class)) {
            PlayerComponent player =  entity.getComponent(PlayerComponent.class);
            if (!player.isActive()) {
                gravity = 0;
            }
        }
        velocity.setGravity(gravity);
        this.mustLeaveBody = mustLeaveBody;
    }

    public BodyComponent getMustLeaveBody() {
        return mustLeaveBody;
    }

    public void setBodyLeaved() {
        mustLeaveBody = null;
    }

    @Override
    public void preUpdate(float delta) {
        if (platformBody != null) {
            body.setInAir(false);
        }
    }

    @Override
    public void postUpdate(float delta) {
        if (platformBody != null && (platformBody.getRight() < body.getLeft() || platformBody.getLeft() > body.getRight() || velocity.getY() != 0)) {
            dismount(null);
        }
    }
}
