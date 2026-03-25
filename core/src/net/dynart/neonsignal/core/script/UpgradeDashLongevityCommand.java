package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.core.User;

public class UpgradeDashLongevityCommand implements Command {

    private final Engine engine;

    public UpgradeDashLongevityCommand(Engine engine) {
        this.engine = engine;
    }

    @Override
    public boolean act(float delta) {
        User user = engine.getUser();
        int level = user.getDashLongevityLevel();
        if (level < User.MAX_DASH_LONGEVITY_LEVEL) {
            user.setDashLongevityLevel(level + 1);
            user.save();
        }
        return true;
    }
}
