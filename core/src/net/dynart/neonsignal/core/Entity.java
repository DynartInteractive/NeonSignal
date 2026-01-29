package net.dynart.neonsignal.core;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.VelocityComponent;

public class Entity {

    private final Engine engine;
    private final EntityManager entityManager;
    private final MessageHandler messageHandler;
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private final List<Class<? extends Component>> classList = new ArrayList<>();

    private String name;
    private boolean active = true;
    private String group = null;
    private Entity parent = null;

    public Entity(Engine engine) {
        this.engine = engine;
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        messageHandler = new MessageHandler(this);
    }

    public void setParent(Entity value) {

        BodyComponent body = getComponent(BodyComponent.class);
        VelocityComponent velocity = getComponent(VelocityComponent.class);

        boolean clear = parent != null || value == null;

        if (clear) {
            // TODO: clearParent for components
            // if we clear a parent, convert positions from local to global
            if (body != null) {
                body.setX(body.getGlobalX());
                body.setY(body.getGlobalY());
            }
            if (velocity != null) {
                velocity.convertInitialBodyPositionFromLocalToGlobal();
            }
        }

        parent = value;

        if (value != null) {
            // TODO: setParent for components
            // if we set a parent, convert positions from global to local
            if (body != null) {
                body.setGlobalX(body.getX());
                body.setGlobalY(body.getY());
            }
            if (velocity != null) {
                velocity.convertInitialBodyPositionFromGlobalToLocal();
            }
        }

    }

    public Entity getParent() {
        return parent;
    }

    public Entity getRoot() {
        return getRoot(this);
    }

    public Entity getRoot(Entity current) {
        return current.getParent() == null ? current : getRoot(current.getParent());
    }

    public void setName(String value) {
        name = value;
    }

    public String getName() {
        return name;
    }

    public Engine getEngine() {
        return engine;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void addComponent(Component component) {
        components.put(component.getClass(), component);
        classList.add(component.getClass());
    }

    public void addComponents(Component ... components) {
        for (Component component : components) {
            addComponent(component);
        }
    }

    public boolean hasComponent(Class<? extends Component> cls) {
        return classList.contains(cls);
    }

    public <T extends Component> T getComponent(Class<T> cls) {
        Component component = components.get(cls);
        return cls.cast(component);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean value) {
        active = value;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void remove() {
        setActive(false);
        entityManager.remove(this);
    }

    public void postConstruct() {
        for (Class<? extends Component> cls : classList) {
            components.get(cls).postConstruct(this);
        }
    }

    public void preUpdate(float delta) {
        if (active) {
            for (Class<? extends Component> cls : classList) {
                Component c = components.get(cls);
                if (c.isActive()) {
                    c.preUpdate(delta);
                }
            }
        }
    }

    public void updateVerticalVelocity(float deltaTime) {
        if (active && hasComponent(VelocityComponent.class)) {
            VelocityComponent velocity = getComponent(VelocityComponent.class);
            if (velocity.isActive()) {
                velocity.updateVertically(deltaTime);
            }
        }
    }

    public void update(float delta) {
        if (active) {
            for (Class<? extends Component> cls : classList) {
                Component c = components.get(cls);
                if (c.isActive()) {
                    c.update(delta);
                }
            }
        }
    }

    public void postUpdate(float delta) {
        if (active) {
            for (Class<? extends Component> cls : classList) {
                Component c = components.get(cls);
                if (c.isActive()) {
                    c.postUpdate(delta);
                }
            }
        }
    }

    public List<Class<? extends Component>> getClassList() {
        return classList;
    }

    public float getVolumeRelatedToPlayer() {
        BodyComponent body = getComponent(BodyComponent.class);
        float volume = 0;
        Entity player = engine.getGameScene().getPlayer();
        BodyComponent playerBody = player.getComponent(BodyComponent.class);
        Vector2 v = new Vector2(body.getCenterX() - playerBody.getCenterX(), body.getCenterY() - playerBody.getCenterY());
        float distance = v.len();
        float audioMaxDistance = engine.getConfig().getAudioMaxDistance();
        if (distance < audioMaxDistance) {
            volume = 1.0f - distance / audioMaxDistance;
        }
        return volume;
    }

}
