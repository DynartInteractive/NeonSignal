package net.dynart.neonsignal.core.script;

import net.dynart.lisa.core.Engine;
import net.dynart.lisa.core.Entity;
import net.dynart.lisa.core.script.Command;
import net.dynart.neonsignal.components.PlayerComponent;

public class UpgradeDashLongevityCommand implements Command {

    private final Engine engine;

    public UpgradeDashLongevityCommand(Engine engine) {
        this.engine = engine;
    }

    @Override
    public boolean act(float delta) {
        Entity player = engine.getGameScene().getPlayer();
        if (player == null) {
            return true;
        }
        PlayerComponent pc = player.getComponent(PlayerComponent.class);
        int level = pc.getDashLongevityLevel();
        if (level < PlayerComponent.MAX_DASH_LONGEVITY_LEVEL) {
            pc.setDashLongevityLevel(level + 1);
        }
        return true;
    }
}
