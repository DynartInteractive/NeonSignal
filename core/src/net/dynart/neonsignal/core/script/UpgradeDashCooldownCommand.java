package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.GameScene;

public class UpgradeDashCooldownCommand implements Command {

    private final Engine engine;

    public UpgradeDashCooldownCommand(Engine engine) {
        this.engine = engine;
    }

    @Override
    public boolean act(float delta) {
        GameScene gameScene = engine.getGameScene();
        int level = gameScene.getDashCooldownLevel();
        if (level < GameScene.MAX_DASH_COOLDOWN_LEVEL) {
            gameScene.setDashCooldownLevel(level + 1);
        }
        return true;
    }
}
