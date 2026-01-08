package net.dynart.neonsignal.core;

public abstract class Component {

    protected EngineConfig config;
    protected Engine engine;
    protected Entity entity;
    protected MessageHandler messageHandler;
    protected boolean active = true;

    public void postConstruct(final Entity entity) {
        this.entity = entity;
        engine = entity.getEngine();
        config = engine.getConfig();
        messageHandler = entity.getMessageHandler();
    }

    public Entity getEntity() {
        return entity;
    }

    public void setActive(boolean value) {
        active = value;
    }

    public boolean isActive() {
        return active;
    }

    public void preUpdate(float delta) {}
    public void update(float delta) {}
    public void postUpdate(float delta) {}

}
