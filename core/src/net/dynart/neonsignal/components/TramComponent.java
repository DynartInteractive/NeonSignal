package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameSprite;
import net.dynart.neonsignal.core.SoundManager;
import net.dynart.neonsignal.core.listeners.MessageListener;

public class TramComponent extends Component {

    private SoundManager soundManager;
    private BodyComponent body;
    private VelocityComponent velocity;
    private ViewComponent view;

    private float elapsed;
    private boolean started;
    private boolean stopped;
    private float playSoundTime;
    private float volumeDownTime;
    private float speed;
    private float accTime;
    private boolean wasSideCollided;

    public TramComponent(float speed) {
        this.speed = speed;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);

        soundManager = engine.getSoundManager();
        body = entity.getComponent(BodyComponent.class);
        velocity = entity.getComponent(VelocityComponent.class);
        view = entity.getComponent(ViewComponent.class);

        GameSprite sprite = view.getSprite(0);
        sprite.setOffsetX(4);
        sprite.setOffsetY(2);
        view.addSprite(new GameSprite());
        view.setSprite(1, "tram_wheel");
        view.getSprite(1).setOffsetX(-4);
        view.getSprite(1).setOffsetY(2);
        view.addSprite(new GameSprite());
        view.setSprite(2, "tram");
        view.getSprite(2).setOffsetY(6);
        view.setLayer(100);

        messageHandler.subscribe(PlatformComponent.MOUNTED, (sender, message) -> {
            if (stopped) {
                return;
            }
            if (!started) {
                VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
                velocity.setX(speed);
            }
            started = true;
            engine.getSoundManager().play("tram"); // TODO: tram_starts
            playSoundTime = 0.8f;
        });
        messageHandler.subscribe(PlatformComponent.DISMOUNTED, (sender, message) -> playSoundTime = 0);

    }

    @Override
    public void update(float delta) {
        if (!started || stopped) {
            return;
        }
        if (body.isSideCollided()) {
            if (!wasSideCollided) {
                soundManager.play("tram_collide", entity.getVolumeRelatedToPlayer());
            }
            speed = -speed;
            accTime = 0.25f;
        }

        // lame sound loop solution?
        if (playSoundTime > 0) {
            playSoundTime -= delta;
            if (playSoundTime < 0) {
                playSoundTime = 0.8f; // we have to know the length of the sound ...
                soundManager.play("tram2"); // TODO: tram_goes
            }
        }
        //

        elapsed += delta; // how many components counting this?

        accTime += delta;
        if (!body.isSideCollided()) {
            if (accTime < 1f) {
                velocity.setX(accTime * speed);
            } else {
                velocity.setX(speed);
            }
        }
        view.getSprite(0).setRotation(-elapsed * 360 * Math.signum(speed));
        view.getSprite(1).setRotation(-elapsed * 360 * Math.signum(speed));
    }

    public void postUpdate(float delta) {
        if (!started || stopped) {
            return;
        }
        wasSideCollided = body.isSideCollided();
        if (body.getTop() < -50 || body.overlapOther(RailEndComponent.class) != null) {
            stopped = true;
            velocity.setX(0);
            soundManager.play("tram_collide", entity.getVolumeRelatedToPlayer());
        }

    }

}
