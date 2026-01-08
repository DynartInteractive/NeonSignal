package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.Grid;

public class WaterCollisionComponent extends Component {

    public static final String COLLISION = "water_collision";

    private BodyComponent body;
    private Grid grid;
    private boolean inWater;
    private boolean lastInWater;
    private float waterY;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        body = entity.getComponent(BodyComponent.class);
        grid = gameScene.getGrid();
        adjustInWater();
    }

    public boolean isInWater() {
        return inWater;
    }

    public boolean wasInWater() {
        return lastInWater;
    }

    public float getWaterY() {
        return waterY;
    }

    @Override
    public void preUpdate(float deltaTime) {
        lastInWater = inWater;
        adjustInWater();
        if (isInWaterChanged()) {
            messageHandler.send(COLLISION);
        }
    }

    private boolean isInWaterChanged() {
        return lastInWater && !inWater || !lastInWater && inWater;
    }

    private void adjustInWater() {
        inWater = false;
        int startY =  grid.getY(body.getBottom());
        int endY = grid.getY(body.getTop());
        int startX = grid.getX(body.getLeft());
        int endX = grid.getX(body.getRight());
        for (int j = startY; j <= endY; j++) {
            for (int i = startX; i <= endX; i++) {
                if (grid.get(Grid.Layer.WATER, i, j)) {
                    inWater = true;
                    waterY = grid.getWorldY(j + 1);
                    break;
                }
            }
        }
    }

}
