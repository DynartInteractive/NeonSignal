package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class StartFallingOnMountComponent extends Component {

    private FallingComponent falling;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        falling = entity.getComponent(FallingComponent.class);
        messageHandler.subscribe(PlatformComponent.MOUNTED, new MessageListener() {
            @Override
            public void receive(Entity sender, String message) {
                falling.start();
            }
        });
        setActive(false);
    }

}
