package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.screens.CutsceneScreen;
import net.dynart.neonsignal.screens.GameScreen;

public class ActionComponent extends Component {

    private EntityManager entityManager;
    private BodyComponent body;
    private BodyComponent mustLeaveBody;
    private boolean used;
    private boolean once;
    private String path;
    private boolean cutscene;


    public ActionComponent(String path, boolean cutscene, boolean once) {
        super();
        this.path = path;
        this.cutscene = cutscene;
        this.once = once;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        body = entity.getComponent(BodyComponent.class);
    }

    public void update(float delta) {
        if (path.isEmpty() || used) {
            return;
        }
        if (mustLeaveBody != null && !body.isOverlap(mustLeaveBody)) {
            mustLeaveBody = null;
        }
        for (Entity player : entityManager.getAllByClass(PlayerComponent.class)) {
            BodyComponent playerBody = player.getComponent(BodyComponent.class);
            if (mustLeaveBody == playerBody) {
                continue;
            }
            startWhenOverlap(playerBody);
        }
    }

    private void startWhenOverlap(BodyComponent playerBody) {
        if (!body.isOverlap(playerBody)) {
            return;
        }
        if (once) {
            used = true;
        } else {
            mustLeaveBody = playerBody;
        }
        if (cutscene) {
            CutsceneScreen cutsceneScreen = (CutsceneScreen)engine.getScreen("cutscene");
            cutsceneScreen.load(path);
            engine.moveToScreen("cutscene");
        } else {
            GameScreen gameScreen = (GameScreen)engine.getScreen("game");
            gameScreen.runScript(path);
        }
    }

}
