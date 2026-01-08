package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class SpringboardComponent extends Component {

    private static final String[] boingSounds = { "boing1", "boing2" };
    private static final float bounceSpeed = 64f;

    private final Entity spring;
    private PlatformComponent platform;
    private float bounceTime;
    private boolean bounceUp;
    private float startY;
    private final float speed;

    public SpringboardComponent(Entity spring, float speed) {
        this.spring = spring;
        this.speed = speed;
    }

    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        platform = entity.getComponent(PlatformComponent.class);
        messageHandler.subscribe(PlatformComponent.MOUNTED, (sender, message) -> bounce());
        messageHandler.subscribe(PlatformComponent.DISMOUNTED, (sender, message) -> reset());

        BodyComponent body = entity.getComponent(BodyComponent.class);
        startY = body.getY();
    }

    @Override
    public void update(float delta) {
        if (bounceTime > 0) {
            bounceTime -= delta;
            if (bounceTime < 0.05f) {
                if (!bounceUp) {
                    bounceUp = true;
                    VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
                    velocity.setY(bounceSpeed);
                }
            }
            if (bounceTime < 0) {
                reset();
                shoot();
            }
        }
    }

    private void reset() {
        BodyComponent body = entity.getComponent(BodyComponent.class);
        body.setY(startY);
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.setY(0);
        bounceTime = 0;
        ViewComponent springView = spring.getComponent(ViewComponent.class);
        springView.setSprite(0, "springboard_bottom");
        springView.setAnimation(0, null);
    }

    private void bounce() {
        bounceUp = false;
        bounceTime = 0.1f;
        VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
        velocity.setY(-bounceSpeed);
        ViewComponent springView = spring.getComponent(ViewComponent.class);
        springView.setAnimationTime(0, 0);
        springView.setAnimation(0, "springboard_bottom");
    }

    private void shoot() {
        engine.getSoundManager().playRandom(boingSounds);
        for (Entity e : platform.getMountedEntities()) {
            MountableComponent mountable = e.getComponent(MountableComponent.class);
            mountable.dismount(null);
            if (e.hasComponent(VelocityComponent.class)) {
                VelocityComponent v = e.getComponent(VelocityComponent.class);
                v.setY(speed);
            }
        }
    }

}
