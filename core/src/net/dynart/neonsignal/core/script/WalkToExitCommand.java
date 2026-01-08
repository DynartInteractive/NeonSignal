package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.VelocityComponent;
import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.EngineConfig;
import net.dynart.neonsignal.core.Grid;

public class WalkToExitCommand implements Command {

    private final BodyComponent body;
    private final VelocityComponent velocity;
    private final EntityManager entityManager;
    private final EngineConfig config;
    private final boolean left;
    private final Grid grid;
    private final Engine engine;

    public WalkToExitCommand(final Engine engine, boolean left) {
        this.engine = engine;
        this.left = left;
        config = engine.getConfig();
        GameScene gameScene = engine.getGameScene();
        grid = gameScene.getGrid();
        entityManager = gameScene.getEntityManager();
        Entity player = gameScene.getPlayer();
        velocity = player.getComponent(VelocityComponent.class);
        body = player.getComponent(BodyComponent.class);
    }


    public boolean act(float delta) {
        float speed = config.getPlayerMaxRunningVelocity();
        float levelWidth = grid.getWidth() * config.getTileWidth();
        boolean done = (!left && body.getLeft() > levelWidth + 10) || (left && body.getRight() < -10);
        entityManager.setInAnimation(true);
        velocity.setX(left ? -speed : speed);
        if (done) {
            engine.moveToScreen("completed");
        }
        return done;
    }

}
