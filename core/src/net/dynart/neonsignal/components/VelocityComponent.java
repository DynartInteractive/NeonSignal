package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class VelocityComponent extends Component {

    public static final String X_ADDED = "velocity_x_added";
    public static final String Y_ADDED = "velocity_y_added";

    private BodyComponent body;

    // horizontal
    private float x;
    private float maxX;
    private float lastX;
    private float initialX;
    private float initialBodyX;
    private float elapsedTimeX;
    private float acceleration;

    // vertical
    private float y;
    private float maxY;
    private float lastY;
    private float initialY;
    private float initialBodyY;
    private float elapsedTimeY;
    private float gravity;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        maxX = config.getMaxVelocity();
        maxY = config.getMaxVelocity();
        body = entity.getComponent(BodyComponent.class);
        setInitialX();
        setInitialY();
    }

    @Override
    public void preUpdate(float delta) {
        lastX = x;
        lastY = y;
    }

    public void setMaxX(float value) {
        maxX = value;
        if (Math.abs(x) > maxX) {
            x = Math.signum(x) * maxX;
        }
        setInitialX();
    }

    public void setMaxY(float value) {
        maxY = value;
        if (Math.abs(y) > maxY) {
            y = Math.signum(y) * maxY;
        }
        setInitialY();
    }

    public void updateVertically(float delta) {
        if (delta == 0) { // delta = 0 means: the app is in background
            return;
        }
        float distanceY = 0;
        float bodyY = 0;
        boolean calculated = false;
        // if it has gravity calculate the vertical velocity
        if (gravity != 0) {
            elapsedTimeY += delta;
            y = initialY + gravity * elapsedTimeY;
            // if it is under the maximum velocity
            if (Math.abs(y) < maxY) {
                // calculate the distance and set the initial body position
                distanceY = initialY * elapsedTimeY + (1/2f * gravity * elapsedTimeY * elapsedTimeY);
                bodyY = initialBodyY;
                calculated = true;
            } else {
                // otherwise maximize the velocity
                y = adjust(y, maxY);
            }
        }
        // if it wasn't calculated
        if (!calculated) {
            // calculate the distance and body position with linear velocity
            distanceY = y * delta;
            bodyY = body.getY();
        }
        body.setY(bodyY + distanceY);
        messageHandler.send(Y_ADDED);

    }

    @Override
    public void update(float delta) {
        if (delta == 0) { // delta = 0 means: the app is in background
            return;
        }
        float distanceX = 0;
        float bodyX = 0;
        boolean calculated = false;
        // if it has acceleration calculate the horizontal velocity
        if (acceleration != 0) {
            elapsedTimeX += delta;
            x = initialX + acceleration * elapsedTimeX;
            // if it is under the maximum velocity
            if (Math.abs(x) < maxX) {
                // calculate the distance and set the initial body position
                distanceX = initialX * elapsedTimeX + (1/2f * acceleration * elapsedTimeX * elapsedTimeX);
                bodyX = initialBodyX;
                calculated = true;
            } else {
                // otherwise maximize the velocity
                x = adjust(x, maxX);
            }
        }
        // if it wasn't calculated
        if (!calculated) {
            // calculate the distance with linear velocity and set the current body position as initial
            distanceX = x * delta;
            bodyX = body.getX();
        }
        body.setX(bodyX + distanceX);
        messageHandler.send(X_ADDED);
    }

    public void convertInitialBodyPositionFromGlobalToLocal() {
        if (entity == null || entity.getParent() == null) {
            return;
        }
        BodyComponent parentBody = entity.getParent().getComponent(BodyComponent.class);
        initialBodyX = initialBodyX - parentBody.getGlobalX();
        initialBodyY = initialBodyY - parentBody.getGlobalY();
    }

    public void convertInitialBodyPositionFromLocalToGlobal() {
        if (entity == null || entity.getParent() == null) {
            return;
        }
        BodyComponent parentBody = entity.getParent().getComponent(BodyComponent.class);
        initialBodyX = parentBody.getGlobalX() + initialBodyX;
        initialBodyY = parentBody.getGlobalY() + initialBodyY;
    }

    private float adjust(float value, float max) {
        if (Math.abs(value) > max) {
            return Math.signum(value) * max;
        }
        return value;
    }

    public void setX(float value) {
        x = adjust(value, maxX);
        setInitialX();
    }

    public void setY(float value) {
        y = adjust(value, maxY);
        setInitialY();
    }

    public float getGlobalX() {
        if (entity == null || entity.getParent() == null) {
            return x;
        }
        VelocityComponent parentVelocity = entity.getParent().getComponent(VelocityComponent.class);
        return parentVelocity == null ? x : parentVelocity.getGlobalX() + x;
    }

    public float getGlobalY() {
        if (entity == null || entity.getParent() == null) {
            return y;
        }
        VelocityComponent parentVelocity = entity.getParent().getComponent(VelocityComponent.class);
        return parentVelocity == null ? y : parentVelocity.getGlobalY() + y;
    }

    public float getY() {
        return y;
    }

    public float getLastY() {
        return lastY;
    }

    public float getX() { return x; }

    public float getLastX() {
        return lastX;
    }

    public float getGravity() {
        return gravity;
    }

    public void setInitialX() {
        initialX = x;
        elapsedTimeX = 0;
        if (body != null) {
            initialBodyX = body.getX();
        }
    }

    public void setInitialY() {
        initialY = y;
        elapsedTimeY = 0;
        if (body != null) {
            initialBodyY = body.getY();
        }
    }

    public void setGravity(float value) {
        gravity = value;
    }

    public void setAcceleration(float value) {
        acceleration = value;
    }

    public float getAcceleration() {
        return acceleration;
    }


}
