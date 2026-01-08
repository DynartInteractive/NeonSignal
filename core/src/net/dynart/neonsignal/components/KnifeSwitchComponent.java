package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class KnifeSwitchComponent extends Component {

    private final String animOn;
    private final String anomOff;
    private BodyComponent body;
    private ViewComponent view;
    private SwitchComponent sw;
    private boolean wasOverlap;
    private float switchTime;

    public KnifeSwitchComponent(String animOn, String animOff) {
        this.anomOff = animOff;
        this.animOn = animOn;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        body = entity.getComponent(BodyComponent.class);
        view = entity.getComponent(ViewComponent.class);
        sw = entity.getComponent(SwitchComponent.class);
        messageHandler.subscribe(SwitchComponent.ON, new MessageListener() {
            @Override
            public void receive(Entity sender, String message) {
                view.setAnimationTime(0,0);
                view.setAnimation(0, "switch_blue_on");
                switchTime = 0.14f;
                engine.getSoundManager().play("switch");
            }
        });
        messageHandler.subscribe(SwitchComponent.OFF, new MessageListener() {
            @Override
            public void receive(Entity sender, String message) {
                view.setAnimationTime(0,0);
                view.setAnimation(0, "switch_blue_off");
                switchTime = 0.14f;
                engine.getSoundManager().play("switch");
            }
        });
    }

    @Override
    public void update(float delta) {
        Entity player = engine.getGameScene().getPlayer();
        BodyComponent playerBody = player.getComponent(BodyComponent.class);
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        if (switchTime > 0) {
            switchTime -= delta;
        } else {
            view.setAnimation(0, null);
            String prefix = sw.isOn() ? "switch_blue_on" : "switch_blue_off";
            if (body.isOverlap(playerBody) && sw.getCurrentRepeatTime() == 0) {
                view.setSprite(0, prefix + "_active");
                playerComponent.setOnSwitch(entity);
                wasOverlap = true;
            } else {
                if (wasOverlap) {
                    playerComponent.setOnSwitch(null);
                    wasOverlap = false;
                }
                view.setSprite(0, prefix);
            }
        }
    }

    public void use() {
        if (switchTime > 0) {
            return;
        }
        if (sw.isOn()) {
            sw.switchOff();
        } else {
            sw.switchOn();
        }
    }

}
