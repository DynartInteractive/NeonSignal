package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.Screen;
import net.dynart.neonsignal.core.Engine;

public class DelayCommand implements Command {

    private final EntityManager entityManager;
    private final float duration;
    private final Engine engine;
    private final Screen cutsceneScreen;

    private float elapsed;

    public DelayCommand(Engine engine, float duration) {
        this.engine = engine;
        this.duration = duration;
        elapsed = 0;
        entityManager = engine.getGameScene().getEntityManager();
        cutsceneScreen = engine.getScreen("cutscene");
    }

    @Override
    public boolean act(float delta) {
        // if the player wants to skip the animation..
        if (engine.getCurrentScreen() == cutsceneScreen && !entityManager.isInAnimation()) {
            return true;
        }
        elapsed += engine.getDeltaTime();
        return elapsed >= duration;
    }

}
