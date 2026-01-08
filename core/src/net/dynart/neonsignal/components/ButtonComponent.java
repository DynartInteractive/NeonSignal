package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class ButtonComponent extends Component {

    private final String spriteOn;
    private final String spriteOff;
    private float startY;
    private BodyComponent body;
    private ViewComponent view;
    private SwitchComponent sw;

    public ButtonComponent(String spriteOn, String spriteOff) {
        this.spriteOff = spriteOff;
        this.spriteOn = spriteOn;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        body = entity.getComponent(BodyComponent.class);
        view = entity.getComponent(ViewComponent.class);
        sw = entity.getComponent(SwitchComponent.class);
        startY = body.getY();
        messageHandler.subscribe(PlatformComponent.MOUNTED, new MessageListener() {
            @Override
            public void receive(Entity sender, String message) {
                switchOn();
            }
        });
        messageHandler.subscribe(SwitchComponent.OFF, new MessageListener() {
            @Override
            public void receive(Entity sender, String message) {
                switchOff();
            }
        });
    }

    public void switchOn() {
        view.setSprite(0, spriteOn);
        body.setY(startY - 2);
        sw.switchOn();

    }

    public void switchOff() {
        view.setSprite(0, spriteOff);
        view.setOffsetY(0);
        body.setY(startY);
    }


}
