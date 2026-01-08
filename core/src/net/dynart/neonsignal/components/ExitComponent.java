package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.screens.GameScreen;
import net.dynart.neonsignal.core.Grid;

public class ExitComponent extends Component {

    private EntityManager entityManager;
    private BodyComponent body;
    private boolean left;
    private GameScreen gameScreen;

    public ExitComponent(boolean left) {
        this.left = left;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);
        gameScreen = (GameScreen)engine.getScreen("game");
    }

    @Override
    public void update(float delta) {
        for (Entity otherEntity : entityManager.getAllByClass(PlayerComponent.class)) {
            BodyComponent otherBody = otherEntity.getComponent(BodyComponent.class);
            if (body.isOverlap(otherBody)) {
                setActive(false);
                engine.moveToScreen("completed");
                //grid.open();
                //gameScreen.runCommand(new WalkToExitCommand(engine, left));
                break;
            }
        }
    }
}
