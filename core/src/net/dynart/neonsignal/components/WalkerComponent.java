package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.listeners.MessageListener;
import net.dynart.neonsignal.core.utils.Direction;
import net.dynart.neonsignal.core.Grid;

public class WalkerComponent extends Component {

    private Direction direction;
    private float speed;
    private BodyComponent body;
    private VelocityComponent velocity;
    private Grid grid;
    private final boolean flipView;
    private final boolean watchEdge;
    private final boolean followPlayer;
    private float stopTime;
    private float noFollowPlayerTime;

    public WalkerComponent(String directionName, float speed, boolean watchEdge, boolean flipView, boolean followPlayer) {
        direction = Direction.get(directionName);
        this.speed = speed;
        this.watchEdge = watchEdge;
        this.flipView = flipView;
        this.followPlayer = followPlayer;
    }

    public void setSpeed(float value) {
        speed = value;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        grid = gameScene.getGrid();
        velocity = entity.getComponent(VelocityComponent.class);
        body = entity.getComponent(BodyComponent.class);

        messageHandler.subscribe(EnemyComponent.DAMAGED, (sender, message) -> stopTime = 0.3f);
    }

    @Override
    public void update(float delta) {

        if (body.isSideCollided()) {
            flipDirection();
        }

        if (stopTime > 0) {

            // only this stops when gets a hit? ..
            stopTime -= delta;
            velocity.setX(0);

        } else {
            if (noFollowPlayerTime > 0) {
                noFollowPlayerTime -= delta;
            }
            if (followPlayer && noFollowPlayerTime < 0) {
                Entity playerEntity = engine.getGameScene().getPlayer();
                BodyComponent playerBody = playerEntity.getComponent(BodyComponent.class);
                float a = body.getCenterX() - playerBody.getCenterX();
                float b = body.getCenterY() - playerBody.getCenterY();
                if (Math.abs(a) < 64 && Math.abs(b) < 16) { // if player is in 4*1 tile distance
                    Direction goodDirection;
                    if (a > 0) { // if player is on left
                        goodDirection = Direction.LEFT;
                    } else { // if player is on right
                        goodDirection = Direction.RIGHT;
                    }
                    // .. follow the player
                    if (goodDirection != direction) {
                        flipDirection();
                    }
                }
            }
            if (watchEdge && isOnEdge()) {
                flipDirection();
            }
            velocity.setX(direction.getX() * speed);
        }
    }

    private boolean isOnEdge() {
        int gridLeftX = grid.getX(body.getLeft());
        int gridRightX = grid.getX(body.getRight());
        int gridY = grid.getY(body.getBottom()) - 1;
        return (
                direction == Direction.RIGHT
                && !grid.get(Grid.Layer.BLOCK, gridRightX, gridY)
                && !grid.get(Grid.Layer.TOP_BLOCK, gridRightX, gridY)
            ) || (
                direction == Direction.LEFT
                && !grid.get(Grid.Layer.BLOCK, gridLeftX, gridY)
                && !grid.get(Grid.Layer.TOP_BLOCK, gridLeftX, gridY)
            );
    }

    private void flipDirection() {
        noFollowPlayerTime = 0.5f;
        direction = direction.inverse();
        if (flipView) {
            ViewComponent view = entity.getComponent(ViewComponent.class);
            view.flipX(direction == Direction.RIGHT);
        }
    }
}
