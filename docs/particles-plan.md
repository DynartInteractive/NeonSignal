# LibGDX Particle System Integration Plan

## Overview

Integrate LibGDX's built-in particle system with GDX Particle Editor support into the Neon Signal engine.

## LibGDX Particle Classes

```java
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.collision.BoundingBox;
```

## Architecture

### 1. ParticleEffectManager (`core/ParticleEffectManager.java`)

Loads `.p` files created with GDX Particle Editor and pools effects using `ParticleEffectPool` for each effect type. Registered in `GameScene` like other managers. Uses `TextureManager.getAtlas()` to load particle textures from existing atlases.

```java
public class ParticleEffectManager {
    private TextureManager textureManager;
    private Map<String, ParticleEffectPool> pools = new HashMap<>();
    private List<PooledEffect> backEffects = new ArrayList<>();  // Behind entities
    private List<PooledEffect> frontEffects = new ArrayList<>(); // In front of entities

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
        return spawn(name, x, y, false);
    }

    public PooledEffect spawn(String name, float x, float y, boolean behind) {
        ParticleEffectPool pool = pools.get(name);
        if (pool == null) {
            Gdx.app.error("Particles", "Unknown effect: " + name);
            return null;
        }
        PooledEffect effect = pool.obtain();
        effect.setPosition(x, y);
        effect.start();
        (behind ? backEffects : frontEffects).add(effect);
        return effect;
    }

    public void update(float delta) {
        updateList(backEffects, delta);
        updateList(frontEffects, delta);
    }

    private void updateList(List<PooledEffect> effects, float delta) {
        for (Iterator<PooledEffect> it = effects.iterator(); it.hasNext();) {
            PooledEffect effect = it.next();
            effect.update(delta);
            if (effect.isComplete()) {
                effect.free();  // Returns to pool
                it.remove();
            }
        }
    }

    public void drawBack(SpriteBatch batch, Rectangle viewport) {
        drawList(backEffects, batch, viewport);
    }

    public void drawFront(SpriteBatch batch, Rectangle viewport) {
        drawList(frontEffects, batch, viewport);
    }

    private void drawList(List<PooledEffect> effects, SpriteBatch batch, Rectangle viewport) {
        for (PooledEffect effect : effects) {
            BoundingBox bb = effect.getBoundingBox();
            if (viewport.overlaps(bb.min.x, bb.min.y, bb.getWidth(), bb.getHeight())) {
                effect.draw(batch);
            }
        }
    }

    public void dispose() {
        for (PooledEffect effect : backEffects) {
            effect.free();
        }
        for (PooledEffect effect : frontEffects) {
            effect.free();
        }
        backEffects.clear();
        frontEffects.clear();
        for (ParticleEffectPool pool : pools.values()) {
            pool.clear();
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
    private boolean behind;        // Render behind entities
    private Align align;           // Emission point alignment
    private float offsetX, offsetY; // Additional offset from aligned position
    private boolean flipWithEntity; // Flip effect when entity faces left
    private float prewarmTime;     // Pre-simulate this many seconds on spawn

    @Override
    public void postConstruct(Entity entity) {
        super.postConstruct(entity);
        particleManager = engine.getGameScene().getParticleEffectManager();
        body = entity.getComponent(BodyComponent.class);

        if (continuous) {
            effect = particleManager.spawn(effectName, getEmitX(), getEmitY(), behind);
            if (effect != null) {
                effect.setContinuous(true);
                if (prewarmTime > 0) {
                    effect.update(prewarmTime); // Pre-simulate to avoid empty start
                }
            }
        }
    }

    private float getEmitX() {
        float flipMultiplier = (flipWithEntity && !body.isFacingRight()) ? -1 : 1;
        return body.getCenterX() + (align.getLeftMultiplier() * body.getHalfWidth() + offsetX) * flipMultiplier;
    }

    private float getEmitY() {
        return body.getCenterY() + align.getBottomMultiplier() * body.getHalfHeight() + offsetY;
    }

    @Override
    public void update(float delta) {
        if (effect != null) {
            effect.setPosition(getEmitX(), getEmitY());
            // Flip particle direction when entity changes facing
            if (flipWithEntity) {
                effect.setFlip(!body.isFacingRight(), false);
            }
        }
    }

    // For one-shot effects triggered by events
    public void emit() {
        PooledEffect spawned = particleManager.spawn(effectName, getEmitX(), getEmitY(), behind);
        if (spawned != null && flipWithEntity && !body.isFacingRight()) {
            spawned.setFlip(true, false);
        }
    }

    @Override
    public void destroy() {
        if (effect != null) {
            effect.allowCompletion(); // Let particles fade out naturally
            effect = null;
        }
    }
}
```

### 3. GameScene Integration

```java
// In GameScene.java
private ParticleEffectManager particleEffectManager;

public void init() {
    // ... existing code ...
    particleEffectManager = new ParticleEffectManager(engine);
}

public void update(float deltaTime) {
    // ... existing code ...
    particleEffectManager.update(deltaTime);
}

public void draw() {
    Rectangle viewport = camera.getViewportBounds();

    // ... background layers ...

    // Draw particles behind entities
    batch.begin();
    particleEffectManager.drawBack(batch, viewport);
    batch.end();

    // ... draw entities ...

    // Draw particles in front of entities
    batch.begin();
    particleEffectManager.drawFront(batch, viewport);
    batch.end();

    // ... front layers (UI, etc.) ...
}

public void dispose() {
    // ... existing dispose code ...
    particleEffectManager.dispose();
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
            parameters.getBoolean("behind", false),
            Align.valueOf(parameters.get("align", "center").toUpperCase()),
            parameters.getFloat("offsetX", 0),
            parameters.getFloat("offsetY", 0),
            parameters.getBoolean("flipWithEntity", false),
            parameters.getFloat("prewarmTime", 0)
        )
    );
    return result;
}
```

## File Structure

```
assets/data/textures/
├── particles.atlas  # Created with GDX Texture Packer
└── particles.png    # Created with GDX Texture Packer
assets/data/particles/
├── dust.p           # Created with GDX Particle Editor
└── fire.p           # Created with GDX Particle Editor
```

## Level Placement (Tiled)

Entity type uses snake_case:

```
type: "particle_emitter"
effect: "fire"
continuous: true
behind: false            # true = render behind entities, false = in front
align: "center_bottom"   # Uses Align enum: center, center_bottom, left_top, etc.
offsetX: 0               # Additional offset from aligned position
offsetY: 0
flipWithEntity: false    # true = flip effect direction when entity faces left
prewarmTime: 0           # Seconds to pre-simulate (avoids empty start for continuous effects)
```

## Code-Triggered Effects

For one-shot effects from components (e.g., in PlayerComponent):

```java
// One-shot effect on dash (renders in front of entities)
particleEffectManager.spawn("dash_dust", body.getCenterX(), body.getBottom());

// Ambient effect behind entities
particleEffectManager.spawn("ambient_dust", x, y, true);
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

1. Create `ParticleEffectManager` class (with back/front layers, culling, dispose)
2. Add manager to `GameScene` (init, update, drawBack/drawFront, dispose)
3. Add `particles` section to `resources.json` loading
4. Create `ParticleEmitterComponent` (with flip support, prewarm, destroy cleanup)
5. Add `createParticleEmitter` factory method
6. Create test particle effect with GDX Particle Editor
7. Place test emitter in level
