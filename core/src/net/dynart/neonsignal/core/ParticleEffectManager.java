package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.JsonValue;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manages LibGDX particle effects loaded from .p files created with GDX Particle Editor.
 * Effects are pooled using ParticleEffectPool for efficient reuse.
 */
public class ParticleEffectManager {

    private static final String LOG_TAG = "ParticleEffectManager";

    private final Engine engine;
    private final TextureManager textureManager;
    private final Map<String, ParticleEffectPool> pools = new HashMap<>();
    private final Map<String, float[][]> originalAngles = new HashMap<>(); // [emitterIndex][0=highMin,1=highMax,2=lowMin,3=lowMax]
    private final List<PooledEffect> backEffects = new ArrayList<>();
    private final List<PooledEffect> frontEffects = new ArrayList<>();

    public ParticleEffectManager(Engine engine) {
        this.engine = engine;
        this.textureManager = engine.getTextureManager();
    }

    /**
     * Loads particle effect definitions from resources.json.
     */
    public void load(JsonValue resourcesJson) {
        JsonValue particles = resourcesJson.get("particles");
        if (particles == null) {
            Gdx.app.log(LOG_TAG, "No particles section in resources.json");
            return;
        }
        for (JsonValue particle = particles.child(); particle != null; particle = particle.next()) {
            String name = particle.name();
            String path = particle.getString("path");
            String atlasName = particle.getString("atlas");
            loadEffect(name, path, atlasName);
        }
    }

    /**
     * Loads a single particle effect and creates a pool for it.
     */
    public void loadEffect(String name, String path, String atlasName) {
        ParticleEffect prototype = new ParticleEffect();
        TextureAtlas atlas = textureManager.getAtlas(atlasName);
        prototype.load(Gdx.files.internal(path), atlas);
        pools.put(name, new ParticleEffectPool(prototype, 4, 16));

        // Store original angle values for each emitter
        Array<ParticleEmitter> emitters = prototype.getEmitters();
        float[][] angles = new float[emitters.size][4];
        for (int i = 0; i < emitters.size; i++) {
            ParticleEmitter.ScaledNumericValue angle = emitters.get(i).getAngle();
            angles[i][0] = angle.getHighMin();
            angles[i][1] = angle.getHighMax();
            angles[i][2] = angle.getLowMin();
            angles[i][3] = angle.getLowMax();
        }
        originalAngles.put(name, angles);

        Gdx.app.log(LOG_TAG, "Loaded particle effect: " + name);
    }

    /**
     * Spawns a particle effect at the given position, rendered in front of entities.
     */
    public PooledEffect spawn(String name, float x, float y) {
        return spawn(name, x, y, false);
    }

    /**
     * Spawns a particle effect at the given position.
     * @param behind true to render behind entities, false for in front
     */
    public PooledEffect spawn(String name, float x, float y, boolean behind) {
        return spawn(name, x, y, behind, 0);
    }

    /**
     * Spawns a particle effect at the given position with rotation.
     * @param behind true to render behind entities, false for in front
     * @param rotation rotation in degrees to add to emitter angles
     */
    public PooledEffect spawn(String name, float x, float y, boolean behind, float rotation) {
        ParticleEffectPool pool = pools.get(name);
        if (pool == null) {
            Gdx.app.error(LOG_TAG, "Unknown particle effect: " + name);
            return null;
        }
        PooledEffect effect = pool.obtain();
        // Set angles before position and start (don't call reset() - it might reset angles)
        resetAndRotateEffect(name, effect, rotation);
        effect.setPosition(x, y);
        effect.start();
        (behind ? backEffects : frontEffects).add(effect);
        return effect;
    }

    /**
     * Spawns a particle effect rotated to match a velocity direction.
     * Useful for impact effects that should spray opposite to the direction of travel.
     * @param velocityX the X velocity of the impacting object
     * @param velocityY the Y velocity of the impacting object
     * @param behind true to render behind entities, false for in front
     */
    public PooledEffect spawnWithVelocity(String name, float x, float y, float velocityX, float velocityY, boolean behind) {
        // Calculate angle from velocity (atan2 returns radians, convert to degrees)
        // Add 180 to make particles spray in the opposite direction (impact effect)
        float angle = MathUtils.atan2(velocityY, velocityX) * MathUtils.radiansToDegrees + 180;
        return spawn(name, x, y, behind, angle);
    }

    /**
     * Spawns a particle effect rotated to match a velocity direction.
     */
    public PooledEffect spawnWithVelocity(String name, float x, float y, Vector2 velocity, boolean behind) {
        return spawnWithVelocity(name, x, y, velocity.x, velocity.y, behind);
    }

    /**
     * Resets emitter angles to original values and applies rotation.
     */
    private void resetAndRotateEffect(String name, PooledEffect effect, float degrees) {
        float[][] angles = originalAngles.get(name);
        if (angles == null) return;

        Array<ParticleEmitter> emitters = effect.getEmitters();
        for (int i = 0; i < emitters.size && i < angles.length; i++) {
            ParticleEmitter.ScaledNumericValue angle = emitters.get(i).getAngle();
            float highMin = angles[i][0] + degrees;
            float highMax = angles[i][1] + degrees;
            float lowMin = angles[i][2] + degrees;
            float lowMax = angles[i][3] + degrees;
            // Set to original values + rotation (not cumulative)
            angle.setHigh(highMin, highMax);
            angle.setLow(lowMin, lowMax);
        }
    }

    /**
     * Updates all active particle effects.
     */
    public void update(float delta) {
        updateList(backEffects, delta);
        updateList(frontEffects, delta);
    }

    private void updateList(List<PooledEffect> effects, float delta) {
        for (Iterator<PooledEffect> it = effects.iterator(); it.hasNext();) {
            PooledEffect effect = it.next();
            effect.update(delta);
            if (effect.isComplete()) {
                effect.free();
                it.remove();
            }
        }
    }

    /**
     * Draws particle effects that render behind entities.
     */
    public void drawBack(SpriteBatch batch, Camera camera) {
        drawList(backEffects, batch, camera);
    }

    /**
     * Draws particle effects that render in front of entities.
     */
    public void drawFront(SpriteBatch batch, Camera camera) {
        drawList(frontEffects, batch, camera);
    }

    private void drawList(List<PooledEffect> effects, SpriteBatch batch, Camera camera) {
        EngineConfig config = engine.getConfig();
        float halfWidth = config.getGameMaxVirtualWidth() / 2f;
        float halfHeight = config.getGameVirtualHeight() / 2f;
        float viewLeft = camera.position.x - halfWidth;
        float viewRight = camera.position.x + halfWidth;
        float viewBottom = camera.position.y - halfHeight;
        float viewTop = camera.position.y + halfHeight;

        for (PooledEffect effect : effects) {
            BoundingBox bb = effect.getBoundingBox();
            if (bb.max.x >= viewLeft && bb.min.x <= viewRight
                && bb.max.y >= viewBottom && bb.min.y <= viewTop) {
                effect.draw(batch);
            }
        }

        // Reset blend function to default after particles (they may use additive blending)
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Clears all active effects without disposing pools.
     * Called when changing levels.
     */
    public void clear() {
        for (PooledEffect effect : backEffects) {
            effect.free();
        }
        for (PooledEffect effect : frontEffects) {
            effect.free();
        }
        backEffects.clear();
        frontEffects.clear();
    }

    /**
     * Disposes all resources.
     */
    public void dispose() {
        clear();
        for (ParticleEffectPool pool : pools.values()) {
            pool.clear();
        }
        pools.clear();
    }

}
