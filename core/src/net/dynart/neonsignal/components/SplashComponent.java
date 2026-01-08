package net.dynart.neonsignal.components;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.listeners.MessageListener;
import net.dynart.neonsignal.core.ParticlePool;
import net.dynart.neonsignal.core.SoundManager;

public class SplashComponent extends Component {

    private static final String[] sounds = { "splash1", "splash2", "splash3" }; // avosound.com Sounds ID: 171123, WaterLiquid-SloshSlap-Wat_5.wav; Sound ID: 171120, WaterLiquid-SloshSlap-Wat_2.wav

    private WaterCollisionComponent waterCollision;
    private EntityManager entityManager;
    private SoundManager soundManager;
    private ParticlePool particlePool;
    private BodyComponent body;

    @Override
    public void postConstruct(final Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        soundManager = engine.getSoundManager();
        entityManager = gameScene.getEntityManager();
        particlePool = gameScene.getParticlePool();
        body = entity.getComponent(BodyComponent.class);
        waterCollision = entity.getComponent(WaterCollisionComponent.class);
        messageHandler.subscribe(WaterCollisionComponent.COLLISION, (sender, message) -> splash());
    }

    private void splash() {
        Entity particle = particlePool.obtain();
        BodyComponent body = particle.getComponent(BodyComponent.class);
        ViewComponent view = particle.getComponent(ViewComponent.class);
        view.setAnimation(0, "water_splash");
        body.setSize(view.getSpriteWidth(0), view.getSpriteHeight(0));
        body.setX(this.body.getCenterX());
        body.setY(waterCollision.getWaterY() - 2f);
        ParticleComponent particleComponent = particle.getComponent(ParticleComponent.class);
        particleComponent.setLifeTime(0.3f);
        entityManager.add(particle);
        if (view.isOnVirtualScreen(0, 50)) {
            soundManager.playRandom(sounds, entity.getVolumeRelatedToPlayer());
        }
    }
}
