package net.dynart.neonsignal.core.script;

import net.dynart.lisa.core.Engine;
import net.dynart.lisa.core.Entity;
import net.dynart.lisa.core.script.Command;
import net.dynart.neonsignal.components.PlayerComponent;

public class UpgradeDashCooldownCommand implements Command {

    private final Engine engine;

    public UpgradeDashCooldownCommand(Engine engine) {
        this.engine = engine;
    }

    @Override
    public boolean act(float delta) {
        Entity player = engine.getGameScene().getPlayer();
        if (player == null) {
            return true;
        }
        PlayerComponent pc = player.getComponent(PlayerComponent.class);
        int level = pc.getDashCooldownLevel();
        if (level < PlayerComponent.MAX_DASH_COOLDOWN_LEVEL) {
            pc.setDashCooldownLevel(level + 1);
        }
        return true;
    }
}
