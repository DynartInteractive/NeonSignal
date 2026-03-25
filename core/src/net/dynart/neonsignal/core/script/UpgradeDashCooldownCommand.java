package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.User;

public class UpgradeDashCooldownCommand implements Command {

    private final Engine engine;

    public UpgradeDashCooldownCommand(Engine engine) {
        this.engine = engine;
    }

    @Override
    public boolean act(float delta) {
        User user = engine.getUser();
        int level = user.getDashCooldownLevel();
        if (level < User.MAX_DASH_COOLDOWN_LEVEL) {
            user.setDashCooldownLevel(level + 1);
            user.save();
        }
        return true;
    }
}
