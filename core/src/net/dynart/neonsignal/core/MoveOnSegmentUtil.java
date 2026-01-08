package net.dynart.neonsignal.core;

public class MoveOnSegmentUtil {

    private float fullDistance;
    private float currentX;
    private float currentY;
    private float targetX;
    private float targetY;
    private int lastDirectionSignX;
    private int lastDirectionSignY;
    private float directionX;
    private float directionY;
    private float speedRatio;
    private float speed;
    private boolean slowing;
    private float distanceToSlow;
    private boolean distanceToSlowIsDivider;

    public void setSpeed(float value) {
        speed = value;
    }

    public void setDistanceToSlow(float value, boolean divider, boolean slowing) {
        this.slowing = slowing;
        distanceToSlow = value;
        distanceToSlowIsDivider = divider;
    }

    public void init(float startX, float startY, float targetX, float targetY) {
        currentX = startX;
        currentY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        float x = startX - targetX;
        float y = startY - targetY;
        lastDirectionSignX = (int)Math.signum(x);
        lastDirectionSignY = (int)Math.signum(y);
        fullDistance = (float)Math.sqrt(x * x + y * y);
        directionX = fullDistance == 0 ? 0 : -x / fullDistance;
        directionY = fullDistance == 0 ? 0 : -y / fullDistance;
        calcSpeedRatio();
    }

    public float getVelocityX() {
        return directionX * speed * speedRatio;
    }

    public float getVelocityY() {
        return directionY * speed * speedRatio;
    }

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    public boolean update(float delta) {
        if (fullDistance == 0) {
            currentX = targetX;
            currentY = targetY;
            return false;
        }
        calcSpeedRatio();
        int signX = (int)Math.signum(currentX - targetX);
        int signY = (int)Math.signum(currentY - targetY);
        currentX += getVelocityX() * delta;
        currentY += getVelocityY() * delta;
        boolean result = lastDirectionSignX == signX && lastDirectionSignY == signY;
        if (!result) {
            currentX = targetX;
            currentY = targetY;
        }
        return result;
    }

    private void calcSpeedRatio() {
        float distanceX = currentX - targetX;
        float distanceY = currentY - targetY;
        float distance = (float)Math.sqrt(distanceX*distanceX + distanceY*distanceY);
        if (slowing) {
            float distanceToSlow = distanceToSlowIsDivider ? fullDistance / this.distanceToSlow : this.distanceToSlow;
            if (distance > fullDistance - distanceToSlow) {
                speedRatio = (fullDistance - distance) / distanceToSlow;
            } else if (distance < distanceToSlow) {
                speedRatio = 1f - (distanceToSlow - distance) / distanceToSlow;
            }
            speedRatio = (float)Math.sqrt(speedRatio);
            if (speedRatio < 0.05f) {
                speedRatio = 0.05f;
            }
        } else {
            speedRatio = 1.0f;
        }
    }

}
