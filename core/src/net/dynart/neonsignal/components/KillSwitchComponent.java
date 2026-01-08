package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class KillSwitchComponent extends Component {

    private boolean playerTouched;
    private BodyComponent body;
    private SwitchComponent sw;

    @Override
    public void postConstruct(Entity entity) {
        body = entity.getComponent(BodyComponent.class);
        sw = entity.getComponent(SwitchComponent.class);
    }

    public void update(float delta) {
        if (!playerTouched() || overlapsEnemy()) {
            return;
        }
        sw.switchOn();
        setActive(false);
    }

    private boolean overlapsEnemy() {
        return body.overlapOther(EnemyComponent.class) != null;
    }

    private boolean playerTouched() {
        if (!playerTouched && body.overlapOther(PlayerComponent.class) != null) {
            playerTouched = true;
        }
        return playerTouched;
    }
}
