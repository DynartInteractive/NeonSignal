package net.dynart.neonsignal.components;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;

import net.dynart.neonsignal.core.Component;
import net.dynart.neonsignal.core.Entity;
import net.dynart.neonsignal.core.GameScene;
import net.dynart.neonsignal.core.ParticleEffectManager;
import net.dynart.neonsignal.core.utils.Align;

/**
 * Attaches a particle effect to an entity position.
 * Can be continuous (always emitting) or triggered on demand.
 */
public class ParticleEmitterComponent extends Component {

    private ParticleEffectManager particleManager;
    private BodyComponent body;
    private ViewComponent view;
    private PooledEffect effect;

    private final String effectName;
    private final boolean continuous;
    private final boolean behind;
    private final Align align;
    private final float offsetX;
    private final float offsetY;
    private final boolean flipWithEntity;
    private final float prewarmTime;
    private final float rotation;

    private boolean lastFlipX;

    public ParticleEmitterComponent(String effectName, boolean continuous, boolean behind,
                                    Align align, float offsetX, float offsetY,
                                    boolean flipWithEntity, float prewarmTime, float rotation) {
        this.effectName = effectName;
        this.continuous = continuous;
        this.behind = behind;
        this.align = align;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.flipWithEntity = flipWithEntity;
        this.prewarmTime = prewarmTime;
        this.rotation = rotation;
    }

    @Override
    public void postConstruct(Entity entity) {
        super.postConstruct(entity);
        GameScene gameScene = engine.getGameScene();
        particleManager = gameScene.getParticleEffectManager();
        body = entity.getComponent(BodyComponent.class);
        view = entity.getComponent(ViewComponent.class);

        if (continuous && particleManager != null) {
            effect = particleManager.spawn(effectName, getEmitX(), getEmitY(), behind, rotation);
            if (effect != null) {
                effect.allowCompletion();
                effect.setEmittersCleanUpBlendFunction(false);
                // Start continuous emission
                for (int i = 0; i < effect.getEmitters().size; i++) {
                    effect.getEmitters().get(i).setContinuous(true);
                }
                effect.start();
                if (prewarmTime > 0) {
                    effect.update(prewarmTime);
                }
                if (view != null) {
                    lastFlipX = view.isFlipX();
                }
            }
        }
    }

    private float getEmitX() {
        float flipMultiplier = (flipWithEntity && view != null && view.isFlipX()) ? -1 : 1;
        return body.getCenterX() + (align.getLeftMultiplier() * body.getHalfWidth() + offsetX) * flipMultiplier;
    }

    private float getEmitY() {
        return body.getCenterY() + align.getBottomMultiplier() * body.getHalfHeight() + offsetY;
    }

    @Override
    public void update(float delta) {
        if (effect != null) {
            effect.setPosition(getEmitX(), getEmitY());

            // Handle flip changes when entity changes facing direction
            if (flipWithEntity && view != null) {
                boolean currentFlipX = view.isFlipX();
                if (currentFlipX != lastFlipX) {
                    effect.setFlip(currentFlipX, false);
                    lastFlipX = currentFlipX;
                }
            }
        }
    }

    /**
     * Spawns a one-shot effect at the current emit position.
     * Use this for triggered effects like jumps, attacks, etc.
     */
    public void emit() {
        if (particleManager == null) return;
        PooledEffect spawned = particleManager.spawn(effectName, getEmitX(), getEmitY(), behind, rotation);
        if (spawned != null && flipWithEntity && view != null && view.isFlipX()) {
            spawned.setFlip(true, false);
        }
    }

    /**
     * Stops the continuous effect, allowing particles to fade out naturally.
     */
    public void stop() {
        if (effect != null) {
            effect.allowCompletion();
        }
    }

    /**
     * Restarts a stopped continuous effect.
     */
    public void restart() {
        if (effect != null) {
            effect.start();
        }
    }

    /**
     * Returns true if the effect has completed (useful for one-shot detection).
     */
    public boolean isComplete() {
        return effect == null || effect.isComplete();
    }

    /**
     * Gets the underlying pooled effect for advanced manipulation.
     */
    public PooledEffect getEffect() {
        return effect;
    }

}
