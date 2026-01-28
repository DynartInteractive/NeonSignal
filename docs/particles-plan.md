# LibGDX Particle System Integration Plan

## Overview

Integrate LibGDX's built-in particle system with GDX Particle Editor support into the Neon Signal engine.

## LibGDX Particle Classes

```java
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
```

## Architecture

### 1. ParticleEffectManager (`core/ParticleEffectManager.java`)

Loads `.p` files created with GDX Particle Editor and pools effects using `ParticleEffectPool` for each effect type. Registered in `GameScene` like other managers. Uses `TextureManager.getAtlas()` to load particle textures from existing atlases.

```java
public class ParticleEffectManager {
    private TextureManager textureManager;
    private Map<String, ParticleEffectPool> pools = new HashMap<>();
    private List<PooledEffect> activeEffects = new ArrayList<>();

    public ParticleEffectManager(Engine engine) {
        this.textureManager = engine.getTextureManager();
    }

    public void load(String name, String path, String atlasName) {
        ParticleEffect prototype = new ParticleEffect();
        TextureAtlas atlas = textureManager.getAtlas(atlasName);
        prototype.load(Gdx.files.internal(path), atlas);
        pools.put(name, new ParticleEffectPool(prototype, 4, 16));
    }

    public PooledEffect spawn(String name, float x, float y) {
        PooledEffect effect = pools.get(name).obtain();
        effect.setPosition(x, y);
        effect.start();
        activeEffects.add(effect);
        return effect;
    }

    public void update(float delta) {
        for (Iterator<PooledEffect> it = activeEffects.iterator(); it.hasNext();) {
            PooledEffect effect = it.next();
            effect.update(delta);
            if (effect.isComplete()) {
                effect.free();  // Returns to pool
                it.remove();
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (PooledEffect effect : activeEffects) {
            effect.draw(batch);
        }
    }
}
```

### 2. ParticleEmitterComponent (`components/ParticleEmitterComponent.java`)

Attaches a particle effect to an entity position. Uses `Align` enum to determine emission point (like other components). Can be continuous or triggered.

```java
public class ParticleEmitterComponent extends Component {
    private ParticleEffectManager particleManager;
    private BodyComponent body;
    private PooledEffect effect;

    private String effectName;
    private boolean continuous;
    private Align align;           // Emission point alignment
    private float offsetX, offsetY; // Additional offset from aligned position

    @Override
    public void postConstruct(Entity entity) {
        super.postConstruct(entity);
        particleManager = engine.getGameScene().getParticleEffectManager();
        body = entity.getComponent(BodyComponent.class);

        if (continuous) {
            effect = particleManager.spawn(effectName, getEmitX(), getEmitY());
            effect.setContinuous(true);
        }
    }

    private float getEmitX() {
        return body.getCenterX() + align.getLeftMultiplier() * body.getHalfWidth() + offsetX;
    }

    private float getEmitY() {
        return body.getCenterY() + align.getBottomMultiplier() * body.getHalfHeight() + offsetY;
    }

    @Override
    public void update(float delta) {
        if (effect != null) {
            effect.setPosition(getEmitX(), getEmitY());
        }
    }

    // For one-shot effects triggered by events
    public void emit() {
        particleManager.spawn(effectName, getEmitX(), getEmitY());
    }
}
```

### 3. GameScene Integration

```java
// In GameScene.java
private ParticleEffectManager particleEffectManager;

public void init() {
    // ... existing code ...
    particleEffectManager = new ParticleEffectManager();
}

public void update(float deltaTime) {
    // ... existing code ...
    particleEffectManager.update(deltaTime);
}

public void draw() {
    // ... existing layers ...

    // Draw particles after entities but before front layer
    batch.begin();
    particleEffectManager.draw(batch);
    batch.end();

    // ... front layers ...
}
```

### 4. Resource Loading (`resources.json`)

```json
"particles": {
    "dust": {"path": "data/particles/dust.p", "atlas": "sprites"},
    "fire": {"path": "data/particles/fire.p", "atlas": "sprites"},
    "sparkle": {"path": "data/particles/sparkle.p", "atlas": "particles"}
}
```

The `atlas` field references an atlas name from the `atlases` section, loaded via `TextureManager.getAtlas()`.

### 5. Factory Method (`NeonSignalEntityFactory.java`)

```java
public Entity createParticleEmitter(Parameters parameters) {
    Entity result = new Entity(engine);
    result.addComponents(
        createBody(parameters),
        new ParticleEmitterComponent(
            parameters.get("effect", "dust"),
            parameters.getBoolean("continuous", true),
            Align.valueOf(parameters.get("align", "center").toUpperCase()),
            parameters.getFloat("offsetX", 0),
            parameters.getFloat("offsetY", 0)
        )
    );
    return result;
}
```

## File Structure

```
assets/data/particles/
├── dust.p           # Created with GDX Particle Editor
├── dust.png         # Particle texture (or use atlas)
├── fire.p
├── fire.png
└── sparkle.p
```

## Level Placement (Tiled)

Entity type uses snake_case:

```
type: "particle_emitter"
effect: "fire"
continuous: true
align: "center_bottom"   # Uses Align enum: center, center_bottom, left_top, etc.
offsetX: 0               # Additional offset from aligned position
offsetY: 0
```

## Code-Triggered Effects

For one-shot effects from components (e.g., in PlayerComponent):

```java
// One-shot effect on dash
particleEffectManager.spawn("dash_dust", body.getCenterX(), body.getBottom());
```

## Advantages of LibGDX Particles

| Feature | Benefit |
|---------|---------|
| Visual editor | Design effects without code |
| Built-in pooling | `ParticleEffectPool` handles recycling |
| Rich features | Gravity, rotation, scaling, color over time |
| Texture atlas support | Use existing sprite atlas |
| Additive blending | Glow effects built-in |

## Implementation Steps

1. Create `ParticleEffectManager` class
2. Add manager to `GameScene` (init, update, draw, dispose)
3. Add `particles` section to `resources.json` loading
4. Create `ParticleEmitterComponent`
5. Add `createParticleEmitter` factory method
6. Create test particle effect with GDX Particle Editor
7. Place test emitter in level
