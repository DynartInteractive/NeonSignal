package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class ElectricSpikeComponent extends Component {

    private final ViewComponent spikeView;
    private final float inactiveTime;
    private final float activeTime;
    private boolean on;
    private boolean state;
    private boolean wasOn;
    private float currentInactiveTime;
    private float currentActiveTime;
    private OverlapAttackComponent overlapAttack;

    public ElectricSpikeComponent(final ViewComponent spikeView, float inactiveTime, float activeTime, boolean currentlyActive, boolean start, float currentTime) {
        this.spikeView = spikeView;
        this.inactiveTime = inactiveTime;
        this.activeTime = activeTime;
        state = start;
        on = currentlyActive;
        wasOn = on;
        if (on) {
            currentActiveTime = currentTime;
        } else {
            currentInactiveTime = currentTime;
        }
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        overlapAttack = entity.getComponent(OverlapAttackComponent.class);
        overlapAttack.setActive(on);
        messageHandler.subscribe(SwitchableComponent.ON, (sender, message) -> state = false);
        messageHandler.subscribe(SwitchableComponent.OFF, (sender, message) -> state = true);
    }

    @Override
    public void update(float delta) {
        if (state) {
            if (on) {
                if (activeTime != 0) {
                    currentActiveTime += delta;
                    if (currentActiveTime > activeTime) {
                        currentActiveTime = 0;
                        on = false;
                    }
                }
            } else {
                currentInactiveTime += delta;
                if (currentInactiveTime > inactiveTime) {
                    currentInactiveTime = 0;
                    on = true;
                }
            }
            if (!wasOn && on) {
                engine.getSoundManager().play("electric_zap", entity.getVolumeRelatedToPlayer());
            }
            overlapAttack.setActive(on);
            spikeView.setVisible(on);
        } else {
            on = false;
            overlapAttack.setActive(false);
            spikeView.setVisible(false);
        }
        wasOn = on;
    }

}
