package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class SpikeComponent extends Component {

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        messageHandler.subscribe(PlatformComponent.MOUNTED, (sender, message) -> {
            PlatformComponent platform = sender.getComponent(PlatformComponent.class);
            Entity entity1 = platform.getLastMountedEntity();
            if (entity1.hasComponent(HealthComponent.class)) {
                HealthComponent health = entity1.getComponent(HealthComponent.class);
                health.decrease(0.5f, entity1);
            }
        });
    }
}
