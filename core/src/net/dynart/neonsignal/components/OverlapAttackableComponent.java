package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class OverlapAttackableComponent extends Component {

    public static final String ATTACKED = "overlap_attackable_attacked";

    public void attacked(Entity by) {
        messageHandler.send(ATTACKED);
    }

}
