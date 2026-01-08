package net.dynart.neonsignal.components;

import java.util.Set;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.utils.Align;
import net.dynart.neonsignal.core.utils.AreaPosition;
import net.dynart.neonsignal.core.utils.Direction;

public class BodyComponent extends Component {

    private float x = -Float.MAX_VALUE;
    private float y = -Float.MAX_VALUE;
    private float lastGlobalY;
    private float lastGlobalX;
    private float halfWidth;
    private float halfHeight;
    private float width;
    private Align align = Align.CENTER_BOTTOM;

    private EntityManager em;
    private boolean inAir;
    private boolean lastInAir;
    private boolean topCollision;
    private boolean bottomCollision;
    private boolean leftCollision;
    private boolean rightCollision;

    private final AreaPosition areaPosition = new AreaPosition();
    private final AreaPosition lastAreaPosition = new AreaPosition();

    public BodyComponent(float width, float height) {
        halfWidth = width / 2f;
        halfHeight = height / 2f;
        this.width = width;
    }

    public AreaPosition getAreaPosition() {
        return areaPosition;
    }

    public AreaPosition getLastAreaPosition() {
        return lastAreaPosition;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        em = engine.getGameScene().getEntityManager();
    }

    public boolean isInAir() {
        return inAir;
    }

    public boolean wasInAir() {
        return lastInAir;
    }

    public void copySize(BodyComponent body) {
        halfWidth = body.halfWidth;
        halfHeight = body.halfHeight;
        this.width = body.width;
    }

    public void setSize(float width, float height) {
        halfWidth = width / 2f;
        halfHeight = height / 2f;
        this.width = width;
    }

    public void setWidth(float width) {
        halfWidth = width / 2f;
        this.width = width;
    }

    public void setHeight(float height) {
        halfHeight = height / 2f;
    }

    public float getWidth() {
        return width;
    }

    public float getGlobalX() {
        if (entity != null && entity.getParent() != null) {
            BodyComponent parentBody = entity.getParent().getComponent(BodyComponent.class);
            if (parentBody != null) {
                return parentBody.getGlobalX() + x;
            }
        }
        return x;
    }

    public void setX(float x) {
        this.x = x;
        areaPosition.setByBodyHorizontalPosition(this);
    }

    public void setGlobalX(float x) {
        if (entity != null && entity.getParent() != null) {
            BodyComponent parentBody = entity.getParent().getComponent(BodyComponent.class);
            if (parentBody != null) {
                setX(x - parentBody.getGlobalX());
                return;
            }
        }
        setX(x);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getGlobalY() {
        if (entity != null && entity.getParent() != null) {
            BodyComponent parentBody = entity.getParent().getComponent(BodyComponent.class);
            if (parentBody != null) {
                return parentBody.getGlobalY() + y;
            }
        }
        return y;
    }

    public void setY(float y) {
        this.y = y;
        areaPosition.setByBodyVerticalPosition(this);
    }

    public void setGlobalY(float y) {
        if (entity != null && entity.getParent() != null) {
            BodyComponent parentBody = entity.getParent().getComponent(BodyComponent.class);
            if (parentBody != null) {
                setY(y - parentBody.getGlobalY());
                return;
            }
        }
        setY(y);
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public float getBottom() {
        return getGlobalY() + halfHeight * align.getBottomMultiplier();
    }

    public float getLastBottom() {
        return lastGlobalY + halfHeight * align.getBottomMultiplier();
    }

    public void setBottom(float bottom) {
        setGlobalY(bottom - halfHeight * align.getBottomMultiplier());
    }

    public float getTop() {
        return getGlobalY() + halfHeight * align.getTopMultiplier();
    }

    public float getLastTop() {
        return lastGlobalY + halfHeight * align.getTopMultiplier();
    }

    public void setTop(float top) {
        setGlobalY(top - halfHeight * align.getTopMultiplier());
    }

    public float getLeft() {
        return getGlobalX() + halfWidth * align.getLeftMultiplier();
    }

    public float getLastLeft() {
        return lastGlobalX + halfWidth * align.getLeftMultiplier();
    }

    public void setLeft(float left) {
        setGlobalX(left - halfWidth * align.getLeftMultiplier());
    }

    public float getRight() {
        return getGlobalX() + halfWidth * align.getRightMultiplier();
    }

    public float getLastRight() {
        return lastGlobalX + halfWidth * align.getRightMultiplier();
    }

    public void setRight(float right) {
        setGlobalX(right - halfWidth * align.getRightMultiplier());
    }

    public float getLastGlobalY() {
        return lastGlobalY;
    }

    public float getLastGlobalX() { return lastGlobalX; }

    public float getCenterX() {
        return (getLeft() + getRight()) / 2f;
    }

    public float getLastCenterX() {
        return (getLastLeft() + getLastRight()) / 2f;
    }

    public float getCenterY() {
        return (getTop() + getBottom()) / 2f;
    }

    public float getLastCenterY() {
        return (getLastTop() + getLastBottom()) / 2f;
    }

    public float getHalfWidth() {
        return halfWidth;
    }

    public float getHalfHeight() {
        return halfHeight;
    }

    public boolean isOverlap(BodyComponent otherBody) {
        float distanceX = Math.abs(getCenterX() - otherBody.getCenterX());
        float distanceY = Math.abs(getCenterY() - otherBody.getCenterY());
        return distanceX < halfWidth + otherBody.getHalfWidth() && distanceY < halfHeight + otherBody.getHalfHeight();
    }

    @Override
    public void preUpdate(float delta) {
        lastGlobalX = getGlobalX();
        lastGlobalY = getGlobalY();
        lastAreaPosition.setByBodyLastPosition(this);
        lastInAir = inAir;
        inAir = true;
        topCollision = false;
        rightCollision = false;
        leftCollision = false;
        bottomCollision = false;
    }

    public boolean isTopCollided() {
        return topCollision;
    }

    public boolean isRightCollided() {
        return rightCollision;
    }

    public boolean isLeftCollided() {
        return leftCollision;
    }

    public boolean isBottomCollided() {
        return bottomCollision;
    }

    public boolean isSideCollided() {
        return leftCollision || rightCollision;
    }

    public void setLeftCollision(boolean v) {
        leftCollision = v;
    }

    public void setRightCollision(boolean v) {
        rightCollision = v;
    }

    public void setTopCollision(boolean v) {
        topCollision = v;
    }

    public void setBottomCollision(boolean v) {
        bottomCollision = v;
    }

    public void setInAir(boolean value) {
        inAir = value;
    }

    public Direction getDirection(BodyComponent otherBody) {
        // TODO: this is shit
        Direction direction = null;
        float x = getLastCenterX() - otherBody.getLastCenterX();
        float y = getLastCenterY() - otherBody.getLastCenterY();
        if (Math.abs(x) > Math.abs(y)) {
            direction = x < 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = y < 0 ? Direction.DOWN : Direction.UP;
        }
        return direction;
    }

    public Direction getDirection(boolean horizontal, BodyComponent otherBody) {
        Direction direction = null;
        if (horizontal) {
            if (getLastCenterX() > otherBody.getLastCenterX()) {
                direction = Direction.LEFT;
            } else if (getLastCenterX() < otherBody.getLastCenterX()) {
                direction = Direction.RIGHT;
            }
        } else {
            if (getLastCenterY() > otherBody.getLastCenterY()) {
                direction = Direction.DOWN;
            } else if (getLastCenterY() < otherBody.getLastCenterY()) {
                direction = Direction.UP;
            }
        }
        return direction;
    }

    public Entity overlapOther(Class cls) {
        return overlapOther(cls, null);
    }

    public Entity overlapOther(Class cls, BodyComponent exceptBody) {
        Set<Entity> entities = em.getAllByClassAndArea(cls, entity);
        if (entities == null) {
            return null;
        }
        for (Entity otherEntity : entities) {
            BodyComponent otherBody = otherEntity.getComponent(BodyComponent.class);
            if (isOverlap(otherBody) && otherBody != exceptBody && otherBody != this) {
                return otherEntity;
            }
        }
        return null;
    }
}
