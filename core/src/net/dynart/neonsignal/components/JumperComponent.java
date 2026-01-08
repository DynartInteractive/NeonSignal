package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class JumperComponent extends Component {

    private BodyComponent body;
    private VelocityComponent velocity;
    private float startY;
    private final float startSpeed;
    private final float waitTime;
    private float timeToStart;
    private final float gravity;

    public JumperComponent(float startSpeed, float gravity, float waitTime) {
        this.gravity = gravity;
        this.waitTime = waitTime;
        this.startSpeed = startSpeed;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);
        timeToStart = waitTime;
        startY = body.getGlobalY();
    }

    @Override
    public void update(float delta) {
        timeToStart -= delta;
        if (timeToStart <= 0) {
            velocity.setY(startSpeed);
            velocity.setGravity(gravity);
            timeToStart = Float.MAX_VALUE;
        }
        if (body.getGlobalY() < startY) {
            body.setGlobalY(startY);
            velocity.setY(0);
            velocity.setGravity(0);
            timeToStart = waitTime;
        }
    }

}
