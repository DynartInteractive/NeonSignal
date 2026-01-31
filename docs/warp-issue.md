# SceneWarpComponent Timing Issue

## Problem

When using `SceneWarpComponent` with `fade=true`, the warp would sometimes occur before the fade completed, causing the player to briefly appear at the wrong position.

## Root Cause

The timing mismatch between `SceneWarpComponent` and `CameraHandler`:

1. **Frame N**: Player enters warp trigger zone
   - `SceneWarpComponent` detects overlap, starts its delay timer
   - `CameraLimitTriggerComponent` sets `fadeToLimit = true`

2. **Frame N+1**: CameraHandler processes the fade
   - `CameraHandler.update()` sees `fadeToLimit = true`, starts `gameScreen.fadeOut()`
   - Fade animation begins (0.5s duration via `CHANGE_LIMIT_FULL_TIME`)

The problem: SceneWarpComponent's timer started **one frame before** the actual fade began. With both using 0.5s delays, the warp would complete slightly before the fade finished.

## Solution

Set `SceneWarpComponent.FADE_DELAY` to 0.45s (slightly less than the 0.5s fade duration). This ensures:

1. The warp happens while the screen is still black (~90% through fade)
2. The warp completes before `CameraHandler.fadeEndAction` switches to the "game_fade_in" screen
3. The player teleport is invisible to the user

## Technical Details

### CameraHandler Fade Flow

```
fadeToLimit = true
    ↓
CameraHandler.update() starts fadeOut (0.5s)
    ↓
Screen fades to black
    ↓
fadeEndAction executes:
  - Updates camera limits
  - Switches to "game_fade_in" screen
  - Starts fadeIn
```

### Why 0.45s Works

- SceneWarp timer: starts at Frame N, expires at ~0.45s
- CameraHandler fade: starts at Frame N+1, completes at ~0.51s (from Frame N perspective)
- Result: Warp happens ~0.05s before screen switch, while screen is black

## Related Files

- `core/src/net/dynart/neonsignal/components/SceneWarpComponent.java`
- `core/src/net/dynart/neonsignal/core/CameraHandler.java`
- `core/src/net/dynart/neonsignal/components/CameraLimitTriggerComponent.java`



---

Let me trace through exactly why this bug happens:

1. Unpause with Escape: PauseScreen's InputListener catches keyUp, returns true (consuming event), so KeyboardListener never gets the keyUp → menuDown stays true
2. Later, SceneWarp triggers camera fade: CameraHandler calls gameScreen.fadeOut()
3. Next frame: GameController.update() sees fade is OUT, calls reset() → calls setMenuDown(false) → since menuDown is still true, menuKeyPressed becomes true
4. Same frame: GameScreen.update() checks isMenuPressed() → returns true → pause() is called!   