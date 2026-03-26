package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.GameScene;

public class UpgradeDashLongevityCommand implements Command {

    private final Engine engine;

    public UpgradeDashLongevityCommand(Engine engine) {
        this.engine = engine;
    }

    @Override
    public boolean act(float delta) {
        GameScene gameScene = engine.getGameScene();
        int level = gameScene.getDashLongevityLevel();
        if (level < GameScene.MAX_DASH_LONGEVITY_LEVEL) {
            gameScene.setDashLongevityLevel(level + 1);
        }
        return true;
    }
}
