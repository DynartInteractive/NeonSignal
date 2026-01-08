package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.Grid;

public class StartFallingInDistanceComponent extends Component {

    private final float distance;

    private BodyComponent body;
    private FallingComponent falling;
    private EntityManager entityManager;
    private float groundDistance;

    public StartFallingInDistanceComponent(float distance) {
        this.distance = distance;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);
        falling = entity.getComponent(FallingComponent.class);
        calculateGroundDistance(gameScene.getGrid());
    }

    private void calculateGroundDistance(Grid grid) {
        int gridX = grid.getX(body.getCenterX());
        for (int i = grid.getY(body.getTop()); i >= 0 && !grid.get(Grid.Layer.BLOCK, gridX, i) && !grid.get(Grid.Layer.TOP_BLOCK, gridX, i); i--) {
            groundDistance += config.getTileHeight();
        }
    }

    @Override
    public void update(float delta) {
        if (falling.isActive()) {
            return;
        }
        for (Entity player : entityManager.getAllByClass(PlayerComponent.class)) {
            BodyComponent playerBody = player.getComponent(BodyComponent.class);
            float dy = body.getTop() - playerBody.getBottom() - GridCollisionComponent.CORRIGATION;
            float dx = Math.abs(body.getCenterX() - playerBody.getCenterX());
            if (dy < groundDistance && dx < distance) {
                falling.start();
            }
        }
    }

}
