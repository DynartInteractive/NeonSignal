package net.dynart.neonsignal.components;

import java.util.LinkedList;
import java.util.List;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.utils.Direction;

public class PlatformComponent extends Component {

    public static final String MOUNTED = "platform_mounted";
    public static final String DISMOUNTED = "platform_dismounted";

    private EntityManager entityManager;
    private BodyComponent body;
    private final List<Entity> mountedEntities = new LinkedList<>();

    public PlatformComponent() {
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);
        addCollisionListener(entity);
    }

    private void addCollisionListener(final Entity entity) {
        if (entity.hasComponent(ColliderComponent.class)) {
            ColliderComponent collider = entity.getComponent(ColliderComponent.class);
            collider.addListener((otherEntity, direction) -> {
                if (direction == Direction.UP && otherEntity.hasComponent(MountableComponent.class)) {
                    MountableComponent mountable = otherEntity.getComponent(MountableComponent.class);
                    mount(mountable);
                }
            });
        }
    }

    @Override
    public void postUpdate(float delta) {
        for (Entity otherEntity : entityManager.getAllByClassAndArea(MountableComponent.class, entity)) {
            BodyComponent otherBody = otherEntity.getComponent(BodyComponent.class);
            MountableComponent mountable = otherEntity.getComponent(MountableComponent.class);
            boolean overlap = body.isOverlap(otherBody);
            if (overlap && otherBody.getLastBottom() >= body.getLastTop() && mountable.getMustLeaveBody() != body) {
                mount(mountable);
            }
            if (!overlap && mountable.getMustLeaveBody() == body) {
                mountable.setBodyLeaved();
            }
        }
    }

    public List<Entity> getMountedEntities() {
        return mountedEntities;
    }

    public Entity getLastMountedEntity() {
        if (mountedEntities.isEmpty()) {
            return null;
        }
        return mountedEntities.get(mountedEntities.size() - 1);
    }

    public void dismount(Entity e) {
        messageHandler.send(PlatformComponent.DISMOUNTED);
        mountedEntities.remove(e);
    }

    private void mount(MountableComponent mountable) {
        mountable.mount(entity);
        mountedEntities.add(mountable.getEntity());
        messageHandler.send(MOUNTED);
    }

}
