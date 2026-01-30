# Slope Collision System Implementation Plan

## Overview

Implement path-based slopes using Tiled polylines with `type=slope` attribute. When entities fall through a slope line, they snap onto the slope surface.

## Files to Create

### 1. `SlopeColliderComponent.java`
**Path:** `core/src/net/dynart/neonsignal/components/SlopeColliderComponent.java`

Component attached to slope entities that holds the path data and provides collision math:
- Stores the `Path` (polyline vertices)
- `checkCrossing(lastY, currentY, centerX)` - detects if vertical movement at centerX crosses the slope, returns Y on slope or NaN
- `getYAtX(x)` - returns Y coordinate on slope at given X
- Bounds getters (`getMinX`, `getMaxX`, `getMinY`, `getMaxY`) for spatial queries

### 2. `SlopeCollisionComponent.java`
**Path:** `core/src/net/dynart/neonsignal/components/SlopeCollisionComponent.java`

Component attached to entities (player, enemies) that should collide with slopes:
- Subscribes to `VelocityComponent.Y_ADDED` (same pattern as `GridCollisionComponent`)
- Queries all slope entities via `EntityManager.getAllByClass(SlopeColliderComponent.class)`
- On crossing detection: snaps entity to slope, sets `body.setInAir(false)`, `body.setBottomCollision(true)`, zeroes Y velocity
- Publishes `SLOPE_COLLISION` message

## Files to Modify

### 3. `GameSceneLoader.java`
**Path:** `core/src/net/dynart/neonsignal/core/GameSceneLoader.java`

Modify `loadPath()` (line 384-388) to check for `type=slope`:

```java
private void loadPath(PolylineMapObject object) {
    Polyline polyline = object.getPolyline();
    Path path = new Path(polyline.getTransformedVertices());

    MapProperties properties = object.getProperties();
    String type = properties.containsKey("type") ? properties.get("type").toString() : null;

    if ("slope".equals(type)) {
        Entity slopeEntity = entityFactory.createSlope(path, object.getName());
        if (slopeEntity != null) {
            entityManager.addToLists(slopeEntity);
        }
    } else {
        pathManager.add(object.getName(), path);
    }
}
```

### 4. `EntityFactory.java`
**Path:** `core/src/net/dynart/neonsignal/core/EntityFactory.java`

Add `createSlope(Path path, String name)` factory method:
- Creates entity with `SlopeColliderComponent(path)`
- Calculates bounding box from path vertices for body dimensions
- Sets entity active and calls `postConstruct()`

### 5. `NeonSignalEntityFactory.java`
**Path:** `core/src/net/dynart/neonsignal/NeonSignalEntityFactory.java`

In `createPlayer()`, add `SlopeCollisionComponent` to the player's component list.

## Collision Algorithm

1. Subscribe to `Y_ADDED` velocity message (triggered when gravity/jumping applies)
2. Only check when falling (globalVY < 0)
3. Create movement ray from `(body.getCenterX(), body.getLastBottom())` to `(body.getCenterX(), body.getBottom())`
4. For each slope entity:
   - Quick bounds check (is entity X within slope X range?)
   - Use `Intersector.intersectSegments()` to test if movement ray crosses slope segment
   - If crossing found: calculate Y on slope at entity's centerX using linear interpolation
   - Snap entity bottom to that Y position

## Tiled Setup

Create slopes in Tiled:
1. Use Polyline tool to draw slope path
2. Set custom property `type` = `slope`
3. Optionally set `name` for debugging

```xml
<object id="100" name="slope1" x="256" y="128">
  <properties>
    <property name="type" value="slope"/>
  </properties>
  <polyline points="0,0 64,-32 128,-32"/>
</object>
```

## Verification

1. Create a test slope in `test1.tmx` using Tiled
2. Run desktop version: `./gradlew desktop:run`
3. Test scenarios:
   - Player falls onto slope from above - should snap to surface
   - Player walks off slope edge - should fall normally
   - Player jumps while on slope - should work normally
   - High-velocity fall - should still catch on slope
