package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.CameraHandler;
import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.ParticlePool;
import net.dynart.neonsignal.core.SoundManager;

public class CrushComponent extends Component {

    private BodyComponent body;
    private EntityManager entityManager;
    private ParticlePool particlePool;
    private CameraHandler cameraHandler;
    private SoundManager soundManager;
    private final String soundName;
    private final String namePrefix;

    public CrushComponent(String namePrefix, String soundName) {
        this.namePrefix = namePrefix;
        this.soundName = soundName;
    }

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        soundManager = engine.getSoundManager();
        GameScene gameScene = engine.getGameScene();
        entityManager = gameScene.getEntityManager();
        particlePool = gameScene.getParticlePool();
        cameraHandler = gameScene.getCameraHandler();
        body = entity.getComponent(BodyComponent.class);
        setActive(false);
    }

    @Override
    public void update(float delta) {
        entity.remove();
        soundManager.play(soundName, entity.getVolumeRelatedToPlayer());
        // TODO: dynamic (from config maybe?)
        addPart(namePrefix + "_part1", body.getGlobalX(), body.getGlobalY(), -660, -25f, 150f);
        addPart(namePrefix + "_part2", body.getGlobalX(), body.getGlobalY(), -660, 25f, 150f);
        addPart(namePrefix + "_part3", body.getGlobalX(), body.getGlobalY(), -560, -15f, 100f);
        addPart(namePrefix + "_part4", body.getGlobalX(), body.getGlobalY(), -560, 15f, 100f);
        //
        cameraHandler.startQuake(0.5f);
    }

    private void addPart(String regionName, float x, float y, float gravity, float velocityX, float velocityY) {
        Entity particle = particlePool.obtain();
        ViewComponent view = particle.getComponent(ViewComponent.class);
        view.setSprite(0, regionName);

        BodyComponent body = particle.getComponent(BodyComponent.class);
        body.setSize(view.getSpriteWidth(0), view.getSpriteHeight(0));
        body.setX(x);
        body.setY(y);

        VelocityComponent velocity = particle.getComponent(VelocityComponent.class);
        velocity.setX(velocityX);
        velocity.setGravity(gravity);
        velocity.setY(velocityY);
        entityManager.add(particle);
    }
}
