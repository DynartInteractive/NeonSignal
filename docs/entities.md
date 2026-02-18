# Entity Reference

This document describes all available entity types in Neon Signal and their configurable properties. Entities are defined in Tiled TMX level files as objects with a `type` property.

## Table of Contents

- [Universal Properties](#universal-properties)
- [Player](#player)
- [Platforms](#platforms)
- [Items](#items)
- [Enemies](#enemies)
- [Hazards](#hazards)
- [Triggers & Actions](#triggers--actions)
- [Switches & Buttons](#switches--buttons)
- [Decorations & Effects](#decorations--effects)
- [Miscellaneous](#miscellaneous)

---

## Universal Properties

All entities support these base properties from their TMX object definition:

| Property | Type | Description |
|----------|------|-------------|
| `x` | float | X position in pixels |
| `y` | float | Y position in pixels |
| `width` | float | Width in pixels |
| `height` | float | Height in pixels |
| `name` | string | Unique identifier for referencing this entity |
| `group` | string | Group name for entity grouping |
| `active` | boolean | Whether entity starts active |

---

## Player

### player

The main playable character with jumping, dashing, firing, and ability management.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `direction` | string | `"right"` | Initial facing direction (`"left"` or `"right"`) |

**Components:** BodyComponent, VelocityComponent, GridCollisionComponent, EntityCollisionComponent, OverlapAttackableComponent, ColliderComponent, BlockComponent, MountableComponent, HealthComponent, MiniBarComponent, WaterCollisionComponent, SplashComponent, PlayerComponent, ViewComponent

---

## Platforms

### platform

Stationary or moving platform that the player can stand on.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sprite` | string | - | Sprite name to display |
| `offset` | int | `0` | Vertical sprite offset |
| `path` | string | - | Path name for movement (optional) |
| `speed` | float | `60.0` | Movement speed if path is set |
| `slowing` | boolean | `true` | Decelerate at path endpoints |
| `start_index` | int | `0` | Starting position in path |
| `move_type` | string | `"fc"` | Movement type (see [Movement Types](#movement-types)) |
| `wait_time` | float | `0` | Wait time at path points |
| `start` | boolean | `false` | Start moving immediately |
| `inputs` | string | - | Comma-separated switch names if switchable |

### dplatform

Disappearing platform that alternates between visible and invisible states.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `visible` | boolean | `true` | Initial visibility state |
| `time` | float | `1.0` | Toggle visibility interval in seconds |
| `sprite` | string | `"dplatform"` | Animation prefix |
| `offset` | int | `0` | Sprite offset |

### falling_platform

Platform that falls when the player stands on it.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sprite` | string | `"falling_floor1"` | Sprite name |

### conveyor

Moving belt that pushes entities in a direction.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `speed` | float | `0` | Belt speed (positive = right, negative = left) |

### tram

Rail-based moving platform that travels along tracks.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `speed` | float | `160` | Movement speed on rails |

### rail_end

Marker for tram track endpoints. Trams reverse direction when reaching a rail_end.

*No custom properties.*

---

## Items

### coin

Points collectible that adds 1 point when collected.

*No custom properties. Fixed size: 16x16 pixels.*

### tequila

Health item that restores 5% health and adds 5 points.

*No custom properties. Fixed size: 16x16 pixels.*

### lollipop

Health item that restores 10% health and adds 10 points.

*No custom properties. Fixed size: 16x16 pixels.*

### oxygen

Underwater collectible that resets the oxygen counter.

*No custom properties. Fixed size: 20x20 pixels.*

### box

Destructible container that can hold items.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `item` | string | - | Item type to contain (`"tequila"`, `"lollipop"`, `"coin"`) |

*Fixed size: ~16x16 pixels.*

---

## Enemies

### fly

Flying enemy that hovers and attacks on contact.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `mountable` | boolean | `false` | Can player stand on it |

### beaver

Walking enemy that patrols back and forth.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `direction` | string | `"left"` | Initial walking direction |
| `speed` | float | `40` | Walking speed |
| `watch_edge` | boolean | `true` | Turn around at ledges |
| `mountable` | boolean | `false` | Can player stand on it |

### hedgehog

Fast walking enemy that rushes toward the player.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `direction` | string | `"left"` | Initial walking direction |
| `speed` | float | `20` | Walking speed |
| `watch_edge` | boolean | `true` | Turn around at ledges |
| `mountable` | boolean | `false` | Can player stand on it |

### frog

Jumping enemy that hops toward the player.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `jump_speed` | float | `200` | Jump velocity |
| `forward_speed` | float | `100` | Horizontal velocity |
| `wait` | float | `0.6` | Wait time between jumps |
| `direction` | string | `"left"` | Initial facing direction |
| `mountable` | boolean | `false` | Can player stand on it |

### purple_frog

Purple variant of frog with different visuals.

*Same properties as [frog](#frog).*

### piranha

Water enemy that jumps vertically out of water.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `start_speed` | float | `420.0` | Jump start velocity |
| `gravity` | float | config gravity | Fall acceleration |
| `wait` | float | `1.0` | Wait time between jumps |

### purple_piranha

Purple piranha that moves along a path.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `path` | string | - | Path name for movement |
| `speed` | float | `60.0` | Movement speed |
| `slowing` | boolean | `true` | Decelerate at endpoints |
| `start_index` | int | `0` | Starting position in path |
| `move_type` | string | `"fc"` | Movement type |
| `wait_time` | float | `0` | Wait time at waypoints |
| `power` | float | `0.25` | Attack damage |

### ufo

Boss entity with alien passengers that moves along a path.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `path` | string | - | Path name for movement |
| `speed` | float | `60.0` | Movement speed |
| `slowing` | boolean | `true` | Decelerate at endpoints |
| `start_index` | int | `0` | Starting position in path |
| `move_type` | string | `"fc"` | Movement type |
| `wait_time` | float | `0` | Wait time at waypoints |
| `start` | boolean | `false` | Start moving immediately |
| `inputs` | string | - | Switch names for control |

---

## Hazards

### spike

Static hazard that damages the player on contact.

*Uses `width` and `height` to determine sprite repetition.*

### electric_spike

Pulsing electrical hazard that alternates between active and inactive states.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `inactive_time` | float | `0.5` | Duration spike is inactive |
| `active_time` | float | `0.5` | Duration spike is active |
| `currently_active` | boolean | `true` | Initial active state |
| `start` | boolean | `true` | Start cycling immediately |
| `current_time` | float | `0` | Current cycle time |
| `power` | float | `1.0` | Damage amount |
| `inputs` | string | - | Switch names for control |

*Uses `width` and `height` to determine orientation (horizontal or vertical).*

### circular_saw

Rotating saw blade that moves along a path.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `path` | string | - | Path name for movement |
| `speed` | float | `60.0` | Movement speed |
| `slowing` | boolean | `true` | Decelerate at endpoints |
| `start_index` | int | `0` | Starting position in path |
| `move_type` | string | `"fc"` | Movement type |
| `wait_time` | float | `0` | Wait time at waypoints |
| `power` | float | `0.5` | Damage amount |

### watermine

Floating mine that moves along a path. Can be destroyed by bullets.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `path` | string | - | Path name for movement |
| `speed` | float | `60.0` | Movement speed |
| `slowing` | boolean | `true` | Decelerate at endpoints |
| `start_index` | int | `0` | Starting position in path |
| `move_type` | string | `"fc"` | Movement type |
| `wait_time` | float | `0` | Wait time at waypoints |
| `power` | float | `0.5` | Damage amount |

### coconut

Falling projectile that drops when the player approaches.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `distance` | float | `64` | Trigger distance before falling |

### stamper

Stationary block with pusher component, typically part of a piston mechanism.

*No custom properties.*

---

## Triggers & Actions

### action

Trigger zone that executes a script when the player overlaps.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `path` | string | `""` | Path to script file (JSON) |
| `cutscene` | boolean | `false` | Triggers cutscene screen |
| `once` | boolean | `true` | Triggers only once |

### exit

Level exit trigger. Completes the level when the player overlaps.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `left` | boolean | - | `true` if exit leads left, `false` for right |

### revive

Checkpoint marker. Player respawns here after dying.

*No custom properties.*

### secret

Hidden collectible that increments the secret counter when found.

*No custom properties.*

### target

Invisible marker entity used as a reference point for camera limits and scene warps.

*No custom properties.*

### camera_limit_trigger

Defines camera bounds when the player overlaps.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `left` | string | - | Target entity name for left limit |
| `right` | string | - | Target entity name for right limit |
| `top` | string | - | Target entity name for top limit |
| `bottom` | string | - | Target entity name for bottom limit |
| `fade` | boolean | `false` | Fade camera transition |
| `instant` | boolean | `false` | Instant transition |
| `in_air_check` | boolean | `true` | Only trigger if player is on ground |

### scene_warp

Warps the player to another location when overlapped.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `target` | string | - | Target entity name (destination) |
| `fade` | boolean | `false` | Fade transition |
| `left` | string | - | Camera left limit target |
| `right` | string | - | Camera right limit target |
| `top` | string | - | Camera top limit target |
| `bottom` | string | - | Camera bottom limit target |
| `instant` | boolean | `false` | Instant transition |

---

## Switches & Buttons

### button

Pressure plate that toggles switchable entities while the player stands on it.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sprite` | string | `"button1_off"` | Off-state sprite |
| `names` | string | `""` | Comma-separated entity names to switch |
| `inverse` | boolean | `false` | Inverted switch logic |
| `repeat_time` | float | `0` | Auto-repeat interval |

### switch

Toggle switch that changes state when the player interacts.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sprite` | string | `"switch_blue_off"` | Off-state sprite |
| `names` | string | `""` | Comma-separated entity names to control |
| `inverse` | boolean | `false` | Inverted switch logic |
| `repeat_time` | float | `0` | Auto-repeat interval |

### kill_switch

Switch that removes itself when activated.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `names` | string | `""` | Entity names to control |
| `inverse` | boolean | `false` | Inverted switch logic |
| `repeat_time` | float | `0` | Auto-repeat interval |

---

## Decorations & Effects

### decoration

Non-interactive visual element that can be static or follow a path.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sprite` | string | - | Static sprite name |
| `animation` | string | - | Animation name (if sprite is empty) |
| `path` | string | - | Path for movement |
| `speed` | float | `60.0` | Movement speed |
| `slowing` | boolean | `true` | Decelerate at endpoints |
| `start_index` | int | `0` | Starting position in path |
| `move_type` | string | `"fc"` | Movement type |
| `wait_time` | float | `0` | Wait time at waypoints |
| `start` | boolean | `false` | Start moving immediately |
| `flip_x` | boolean | `false` | Flip sprite horizontally |
| `flip_y` | boolean | `false` | Flip sprite vertically |
| `layer` | int | `100` | Render layer |
| `visible` | boolean | - | Initial visibility |

### particle_emitter

Attaches a particle effect to an entity position.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `effect` | string | `"dust"` | Particle effect name |
| `continuous` | boolean | `true` | Continuous or one-shot |
| `behind` | boolean | `false` | Render behind or in front |
| `align` | string | `"center"` | Alignment (CENTER, LEFT_BOTTOM, etc.) |
| `offsetX` | float | `0` | Horizontal offset |
| `offsetY` | float | `0` | Vertical offset |
| `flipWithEntity` | boolean | `false` | Flip with entity direction |
| `prewarmTime` | float | `0` | Pre-simulate time |
| `rotation` | float | `0` | Particle rotation angle |

---

## Miscellaneous

### springboard

Bouncy platform that launches the player upward.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `speed` | float | player_jump_velocity * 2 | Launch velocity |

### movable

Pushable block that can move along a path or be pushed by the player.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `sprite` | string | `"movable1"` | Sprite name |
| `path` | string | - | Path name for movement |
| `speed` | float | `60.0` | Movement speed |
| `slowing` | boolean | `true` | Decelerate at endpoints |
| `start_index` | int | `0` | Starting position in path |
| `move_type` | string | `"fc"` | Movement type |
| `wait_time` | float | `0` | Wait time at waypoints |
| `start` | boolean | `false` | Start moving immediately |
| `inputs` | string | - | Switch names for control |

### bullet_spawner

Static entity that spawns bullets.

*No custom properties.*

### enemy_block

Invisible block that prevents non-player entities from passing.

*No custom properties.*

### big_rock

Large immovable obstacle.

*No custom properties.*

---

## Movement Types

Entities with path-based movement support the following `move_type` values:

| Type | Description |
|------|-------------|
| `fc` | Forward continuous - loops the path continuously |
| `fo` | Forward once - travels the path once and stops |
| `fstop` | Forward stoppable - can be stopped mid-path |
| `fstep` | Forward step - moves one waypoint at a time |
| `po` | Ping-pong once - travels forward then back once |

---

## TMX Object Example

```xml
<object id="1" name="enemy1" x="256" y="128" width="32" height="32">
  <properties>
    <property name="type" value="beaver"/>
    <property name="direction" value="right"/>
    <property name="speed" type="float" value="60"/>
    <property name="watch_edge" type="bool" value="true"/>
  </properties>
</object>
```

---

## See Also

- [Scripting Reference](scripting.md) - Script commands for cutscenes and events
- [CLAUDE.md](../CLAUDE.md) - Project overview and architecture
