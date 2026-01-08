package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.Grid;

public class GridCollisionComponent extends Component {

    public static final String LEFT_COLLISION = "grid_left_collision";
    public static final String RIGHT_COLLISION = "grid_right_collision";
    public static final String BOTTOM_COLLISION = "grid_bottom_collision";
    public static final String TOP_COLLISION = "grid_top_collision";
    public static final String SLIDER_COLLISION = "grid_slider_collision";

    public static final float CORRIGATION = 0.001f;


    private BodyComponent body;
    private Grid grid;
    private VelocityComponent velocity;
    private boolean wantsToDrop;

    public GridCollisionComponent() {
        super();
    }

    @Override
    public void postConstruct(Entity entity) {

        super.postConstruct(entity);

        grid = engine.getGameScene().getGrid();
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);

        messageHandler.subscribe(VelocityComponent.Y_ADDED, (sender, message) -> collideVertically());
        messageHandler.subscribe(VelocityComponent.X_ADDED, (sender, message) -> collideHorizontally());
        messageHandler.subscribe(PlayerComponent.WANTS_TO_DROP, (sender, message) -> wantsToDrop = true);
    }

    private void collideVertically() {
        if (doesNotNeedVerticalCheck()) {
            return;
        }
        if (velocity.getGlobalY() < 0 || wantsToDrop) {
            collideBottom();
        } else {
            collideTop();
        }
        wantsToDrop = false;
    }

    private boolean doesNotNeedVerticalCheck() {
        return !active || (velocity.getGlobalY() == 0 && !wantsToDrop);
    }

    private void collideTop() {
        int gridY = grid.getY(body.getTop());
        for (int gridX = grid.getX(body.getLeft()); gridX <= grid.getX(body.getRight()); gridX++) {
            if (isBlock(gridY, gridX)) {
                body.setTop(grid.getWorldY(gridY) - CORRIGATION);
                body.setTopCollision(true);
                messageHandler.send(TOP_COLLISION);
                velocity.setY(0);
                if (isSlider(gridY, gridX)) { // slider only exists within block
                    messageHandler.send(SLIDER_COLLISION);
                }
                return;
            }
        }
    }

    private void collideBottom() {
        boolean lastGridYIsDifferent = grid.getY(body.getLastGlobalY()) != grid.getY(body.getGlobalY());
        int gridY = grid.getY(body.getBottom());
        for (int gridX = grid.getX(body.getLeft()); gridX <= grid.getX(body.getRight()); gridX++) {
            if (isBlock(gridY, gridX) || (lastGridYIsDifferent && !wantsToDrop && isTopBlock(gridY, gridX))) {
                body.setBottom(grid.getWorldY(gridY + 1));
                body.setInAir(false);
                body.setBottomCollision(true);
                messageHandler.send(BOTTOM_COLLISION);
                velocity.setY(0);
                if (isSlider(gridY, gridX)) { // slider only exists within block
                    messageHandler.send(SLIDER_COLLISION);
                }
                return;
            }
        }
    }

    private void collideHorizontally() {
        if (doesNotNeedHorizontalCheck()) {
            return;
        }
        if (velocity.getGlobalX() < 0) {
            collideLeft();
        } else {
            collideRight();
        }
    }

    private boolean doesNotNeedHorizontalCheck() {
        return !active || velocity.getGlobalX() == 0;
    }

    private void collideLeft() {
        int gridX = grid.getX(body.getLeft());
        for (int gridY = grid.getY(body.getBottom()); gridY <= grid.getY(body.getTop()); gridY++) {
            if (isBlock(gridY, gridX)) {
                body.setLeft(grid.getWorldX(gridX + 1));
                body.setLeftCollision(true);
                messageHandler.send(LEFT_COLLISION);
                velocity.setX(0);
                return;
            }
        }
    }

    private void collideRight() {
        int gridX = grid.getX(body.getRight());
        for (int gridY = grid.getY(body.getBottom()); gridY <= grid.getY(body.getTop()); gridY++) {
            if (isBlock(gridY, gridX)) {
                body.setRight(grid.getWorldX(gridX) - CORRIGATION);
                body.setRightCollision(true);
                messageHandler.send(RIGHT_COLLISION);
                velocity.setX(0);
                return;
            }
        }
    }

    private boolean isBlock(int gridY, int gridX) {
        return grid.get(Grid.Layer.BLOCK, gridX, gridY);
    }

    private boolean isSlider(int gridY, int gridX) {
        return grid.get(Grid.Layer.SLIDER, gridX, gridY);
    }

    private boolean isTopBlock(int gridY, int gridX) {
        return grid.get(Grid.Layer.TOP_BLOCK, gridX, gridY);
    }

}
