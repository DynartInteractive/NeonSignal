package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.listeners.MessageListener;
import net.dynart.neonsignal.core.SoundManager;

public class FallingComponent extends Component {

    private BodyComponent body;
    private VelocityComponent velocity;
    private ViewComponent view;
    private SoundManager soundManager;
    private final String soundName;
    private final float gravity;
    private final float delay;
    private float vibratingTime;
    private final boolean repeat;
    private float repeatWaitTime;
    private float startY;
    private boolean blinking;

    public FallingComponent(float gravity, float delay, String soundName, boolean repeat) {
        this.gravity = gravity;
        this.soundName = soundName;
        this.delay = delay;
        this.repeat = repeat;
        setActive(false);
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        final CrushComponent crushComponent = entity.getComponent(CrushComponent.class);
        soundManager = engine.getSoundManager();
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);
        view = entity.getComponent(ViewComponent.class);
        startY = body.getY();
        if (crushComponent != null) {
            messageHandler.subscribe(GridCollisionComponent.BOTTOM_COLLISION, (sender, message) -> crushComponent.setActive(true));
            messageHandler.subscribe(EntityCollisionComponent.BOTTOM_COLLISION, (sender, message) -> crushComponent.setActive(true));
        }
    }

    @Override
    public void update(float delta) {

        // if we have to repeat
        if (repeatWaitTime > 0) {
            repeatWaitTime -= delta;
            if (repeatWaitTime < 0) {
                reset();
            } else if (repeatWaitTime < 0.75f) {
                if (!blinking) {
                    blinking = true;
                    body.setY(startY);
                    velocity.setGravity(0);
                    velocity.setY(0);
                    view.setOffsetY(0);
                    view.setOffsetX(0);
                } else { // .. or in progress
                    boolean v = (int)(repeatWaitTime * 10f) % 2 != 0;
                    view.setAlpha(0, v ? 0.3f : 1f);
                }
            }
            return;
        }

        // if must vibrate
        vibratingTime -= delta;
        if (vibratingTime > 0) {
            view.setOffsetY((float)Math.sin(vibratingTime * 80000f)/2f + 0.5f);
            view.setOffsetX((float)Math.cos(vibratingTime * 80000f)/3f + 0.5f);
        } else {
            velocity.setGravity(gravity);
        }

        // if fall out from the screen
        if (body.getTop() < -100) {
            if (repeat) {
                if (repeatWaitTime <= 0) {
                    repeatWaitTime = 5.0f;
                }
            }
            velocity.setGravity(0);
        }
    }

    public void start() {
        setActive(true);
        vibratingTime = delay;
        if (soundName != null) {
            soundManager.play(soundName, entity.getVolumeRelatedToPlayer());
        }
    }

    public void reset() {
        vibratingTime = 0;
        repeatWaitTime = 0;
        view.setVisible(true);
        blinking = false;
        body.setY(startY);
        velocity.setGravity(0);
        setActive(false);
    }

}
