package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;

public class SwitchComponent extends Component {

    public static final String ON = "switch_on";
    public static final String OFF = "switch_off";

    protected final float repeatTime;
    protected float currentRepeatTime;
    protected final String[] names;
    protected final boolean inverse;
    protected boolean on;

    public SwitchComponent(String namesString, boolean inverse, float repeatTime) {
        names = namesString.split(",");
        for (int i = 0; i < names.length; i++) {
            names[i] = names[i].trim();
        }
        this.inverse = inverse;
        this.repeatTime = repeatTime;
        on = inverse;
    }

    public float getCurrentRepeatTime() {
        return currentRepeatTime;
    }

    public SwitchableComponent getSwitchableComponentFromEntity(String name) {
        Entity entity = engine.getGameScene().getEntityManager().getByName(name);
        if (entity == null) {
            throw new RuntimeException("Entity '" + name + "' not found for switch.");
        }
        SwitchableComponent switchable = entity.getComponent(SwitchableComponent.class);
        if (switchable == null) {
            throw new RuntimeException("Entity '" + name + "' is not a switchable.");
        }
        return switchable;
    }

    @Override
    public void postUpdate(float delta) {
        if (currentRepeatTime > 0) {
            currentRepeatTime -= delta;
            if (currentRepeatTime < 0) {
                currentRepeatTime = 0;
                switchOff();
            }
        }
    }

    public void switchOn() {
        currentRepeatTime = repeatTime;
        on = !inverse;
        for (String name : names) {
            SwitchableComponent switchable = getSwitchableComponentFromEntity(name);
            if (inverse) {
                switchable.switchOff();
            } else {
                switchable.switchOn();
            }
        }
        messageHandler.send(ON);
    }

    public void switchOff() {
        currentRepeatTime = 0;
        on = inverse;
        for (String name : names) {
            SwitchableComponent switchable = getSwitchableComponentFromEntity(name);
            if (inverse) {
                switchable.switchOn();
            } else {
                switchable.switchOff();
            }
        }
        messageHandler.send(OFF);
    }

    public boolean isOn() {
        return on;
    }
}
