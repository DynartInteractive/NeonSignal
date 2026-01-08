package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;

public class SwitchableComponent extends Component {

    // TODO: sound?

    public static final String ON = "switch_on";
    public static final String OFF = "switch_off";

    public boolean isOn() {
        return on;
    }

    protected boolean on;
    protected String[] inputs;

    public SwitchableComponent(String inputNames) {
        if (inputNames != null) {
            inputs = inputNames.split(",");
            for (int i = 0; i < inputs.length; i++) {
                inputs[i] = inputs[i].trim();
            }
        } else {
            inputs = new String[]{}; // because of a possible exception in switchOn
        }
    }

    public void switchOn() {
        if (inputs.length == 0 || areInputsOn()) {
            on = true;
            messageHandler.send(ON);
        }
    }

    public void switchOff() {
        on = false;
        messageHandler.send(OFF);
    }

    private boolean areInputsOn() {
        EntityManager em = engine.getGameScene().getEntityManager();
        for (String input : inputs) {
            Entity inputEntity = em.getByName(input);
            if (inputEntity == null) {
                throw new RuntimeException("Input entity doesn't exist: " + input);
            }
            if (!inputEntity.hasComponent(SwitchComponent.class)) {
                throw new RuntimeException("Entity is not a switch: " + input);
            }
            SwitchComponent sw = inputEntity.getComponent(SwitchComponent.class);
            if (!sw.isOn()) {
                return false;
            }
        }
        return true;
    }

}
