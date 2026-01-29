# Neon Signal Scripting System - Technical Documentation

## Overview

Scripts are JSON files that enable cutscenes and dynamic behavior. All scripts must have a root `"sequence"` array. Commands implement the `Command` interface with `boolean act(float delta)` - returning `true` means finished, `false` means still running.

---

## Command Reference

### Blocking Commands (wait for completion)

| Command | Parameters | Description |
|---------|-----------|-------------|
| `say` | `name`, `text`, `start`, `finish`, `left` | Character dialog box |
| `nexus_says` | `lines[]`, `char_delay`, `line_delay`, `hold_time`, `button_label`, `begin_fade`, `end_fade` | Multi-line typewriter text |
| `walk_to` | `entity`, `target`, `exact` | Move entity to target position |
| `walk_to_exit` | `left` | Move player off-screen (triggers level end) |
| `delay` | `duration` | Wait for N seconds |
| `move_camera_to` | `target`, `speed` | Pan camera to entity |
| `sequence` | `[commands...]` | Run commands in order |
| `parallel` | `[commands...]` | Run commands simultaneously (blocks until ALL finish) |

### Non-Blocking Commands (execute immediately)

| Command | Parameters | Description |
|---------|-----------|-------------|
| `set_camera_target` | `target`, `smooth` | Set entity for camera to follow |
| `set_camera_limit` | `entity` | Set camera boundary from CameraLimitTriggerComponent |
| `set_animation` | `entity`, `animation`, `layer`, `flip_x`, `flip_y`, `time` | Change entity animation |
| `set_visible` | `entity`, `visible` | Show/hide entity |
| `set_parent` | `entity`, `parent` | Set entity parent relationship |
| `set_movement_active` | `entity`, `active`, `finish_on_skip`, `finish_index` | Enable/disable MovableComponent |
| `add_player_ability` | `ability` | Grant player ability (e.g., `"double_jump"`) |
| `trigger` | `name` | Activate SwitchableComponent |
| `play_music` | `name` | Start music track |

---

## Detailed Command Reference

### `say` - Character Dialog
```json
{"say": {
  "name": "coolfox",      // Character ID (default: "neonsignal")
  "text": "Hello there!", // Dialog text (required)
  "start": true,          // Character enters scene (default: false)
  "finish": true,         // Character exits after (default: false)
  "left": false           // Appear on left side (default: false)
}}
```

### `nexus_says` - Typewriter Text Display
```json
{"nexus_says": {
  "lines": [
    {"text": "Welcome to [#00ffff]Neon Signal[#ffffff]!", "delay": 0, "margin_bottom": 5},
    {"text": "Press A to continue.", "delay": 0.5, "font": "small"}
  ],
  "char_delay": 0.03,     // Seconds between characters (default: 0.03)
  "line_delay": 0.5,      // Seconds between lines (default: 0.5)
  "hold_time": 2.0,       // Display time after completion (default: 2.0)
  "button_label": "Next", // Optional continue button
  "begin_fade": true,     // Fade in at start (default: true)
  "end_fade": true        // Fade out at end (default: true)
}}
```

### `walk_to` - Entity Movement
```json
{"walk_to": {
  "entity": "player",    // Entity to move (required)
  "target": "marker_1",  // Destination entity (required)
  "exact": false         // Stop at exact center vs adjacent (default: false)
}}
```

### `walk_to_exit` - Exit Level
```json
{"walk_to_exit": {
  "left": true           // Walk off left edge (false = right edge)
}}
```

### `delay` - Wait
```json
{"delay": {
  "duration": 1.5        // Seconds to wait (required)
}}
```

### `move_camera_to` - Camera Pan
```json
{"move_camera_to": {
  "target": "boss",      // Entity to pan toward (required)
  "speed": 256           // Pixels per second (default: 256)
}}
```

### `set_camera_target` - Camera Follow
```json
{"set_camera_target": {
  "target": "player",    // Entity for camera to follow (required)
  "smooth": false        // Smooth transition (default: false)
}}
```

### `set_camera_limit` - Camera Boundary
```json
{"set_camera_limit": {
  "entity": "cam_limit_1" // Entity with CameraLimitTriggerComponent (required)
}}
```

### `set_animation` - Animation Control
```json
{"set_animation": {
  "entity": "npc",       // Entity name (required)
  "animation": "walk",   // Animation name (required)
  "layer": 0,            // Animation layer (optional)
  "flip_x": true,        // Mirror horizontally (optional)
  "flip_y": false,       // Mirror vertically (optional)
  "time": 0.5            // Set animation time position (optional)
}}
```

### `set_visible` - Visibility Control
```json
{"set_visible": {
  "entity": "secret_door", // Entity name (required)
  "visible": true          // Show or hide (required)
}}
```

### `set_parent` - Entity Parenting
```json
{"set_parent": {
  "entity": "item",      // Child entity (required)
  "parent": "npc"        // Parent entity (required)
}}
```

### `set_movement_active` - Movement Control
```json
{"set_movement_active": {
  "entity": "patrol_npc",   // Entity with MovableComponent (required)
  "active": true,           // Enable/disable movement (required)
  "finish_on_skip": true,   // Complete movement when skipped (default: true)
  "finish_index": -1        // Waypoint to finish at when skipped (default: -1)
}}
```

### `add_player_ability` - Grant Ability
```json
{"add_player_ability": {
  "ability": "double_jump"  // Ability name from PlayerAbility enum (required)
}}
```

### `trigger` - Activate Switch
```json
{"trigger": {
  "name": "door_1"       // Entity with SwitchableComponent (required)
}}
```

### `play_music` - Music Playback
```json
{"play_music": {
  "name": "boss_theme"   // Music track name from resources.json (required)
}}
```

### `sequence` - Sequential Execution
```json
{"sequence": [
  {"delay": {"duration": 0.5}},
  {"say": {"name": "npc", "text": "First line"}},
  {"say": {"name": "npc", "text": "Second line"}}
]}
```

### `parallel` - Simultaneous Execution
```json
{"parallel": [
  {"walk_to": {"entity": "npc1", "target": "pos1"}},
  {"walk_to": {"entity": "npc2", "target": "pos2"}},
  {"move_camera_to": {"target": "center"}}
]}
```

---

## Quick Reference Table

| Command | Blocking | Skippable |
|---------|:--------:|:---------:|
| `say` | Yes | Yes |
| `nexus_says` | Yes | Yes |
| `walk_to` | Yes | No |
| `walk_to_exit` | Yes | No |
| `delay` | Yes | Yes |
| `move_camera_to` | Yes | No |
| `sequence` | Yes | No |
| `parallel` | Yes | No |
| `set_camera_target` | No | - |
| `set_camera_limit` | No | - |
| `set_animation` | No | - |
| `set_visible` | No | - |
| `set_parent` | No | - |
| `set_movement_active` | No | Yes |
| `add_player_ability` | No | - |
| `trigger` | No | - |
| `play_music` | No | - |

---

## Script File Location

Scripts are stored in `assets/data/scripts/` and loaded via `ScriptLoader`. The loader uses reflection to map JSON command names (e.g., `walk_to`) to factory methods (e.g., `createWalkTo`).

## Example Script

```json
{
  "sequence": [
    {"set_camera_target": {"target": "npc_guide"}},
    {"move_camera_to": {"target": "npc_guide", "speed": 200}},
    {"delay": {"duration": 0.5}},
    {"say": {"name": "guide", "text": "Welcome, traveler!", "start": true}},
    {"say": {"name": "guide", "text": "Let me show you around.", "finish": true}},
    {"parallel": [
      {"walk_to": {"entity": "npc_guide", "target": "destination"}},
      {"set_camera_target": {"target": "player"}}
    ]},
    {"add_player_ability": {"ability": "double_jump"}},
    {"nexus_says": {
      "lines": [
        {"text": "You learned [#00ffff]Double Jump[#ffffff]!"}
      ],
      "hold_time": 2.0
    }}
  ]
}
```
