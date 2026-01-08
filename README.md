# Neon Signal

Retro platformer game

## Build settings

Use JDK 17+ in Android Studio, for example: OpenJDK 17, Amazon Corretto 17, ...
Settings > Build, Execution, Deployment > Build tools > Gradle > Gradle JDK

## How to run on desktop

After `gradle build`: right click on `desktop/main/java/net.dynart.neonsignal/DesktopLauncher`, then
`Run DesktopLauncher.main()`.

### Desktop arguments

`[n]` = numerical value, for example: 123
`[s]` = string value without space or between quotes, for example: string-value or "string value" 

* `-w [n]` or `-width [n]`: width of the video resolution
* `-h [n]` or `-height [n]`: height of the video resolution
* `-fps [n]`: the forced frame per seconds value
* `-novsync`: if set, the render will NOT wait for vertical synchronization
* `-window`: if set, the game will run in a window
* `-borderless`: if this and the `-window` set, the window will not have border
* `-display [n]`: if set, the game tries to run on the given display
* `-level [s]`: if set, the game tries to load and start with the given level

## Repository Overview

This project is a cross-platform retro platformer game built on **LibGDX**. The
build uses Gradle with four sub-projects—`core`, `desktop`, `android`, and
`ios`—all configured in `settings.gradle`. The main build script sets common
properties (library versions, repositories) and defines per-project
dependencies for each platform module.

### Key modules

- **core** – Contains all game logic, entities, components, and screen
  definitions. The core module is shared across platforms.
- **desktop** – Desktop launcher with configuration for LWJGL3.
- **android** and **ios** – Platform launchers/build scripts for mobile devices.

Example: `DesktopLauncher` creates a `CoolFox` instance and configures the
window based on command-line arguments.

### Core Module

The entry point for the engine is `CoolFox.java`. It builds an `Engine` with a
configuration object, sets up screens, and handles startup logic. When the
engine finishes loading resources, it registers game screens like `GameScreen`,
`MainMenuScreen`, `SettingsScreen`, and others.

`Engine` orchestrates resource loading, input handling, screens, and the
update/render loop. It loads resources from `resources.json`, prepares graphics
assets, sets up controllers, and manages screen transitions.

### Configuration and Resources

Settings and resource locations are defined in JSON under `assets/data/`. The
main configuration file (`config.json`) specifies physics constants, controller
mappings, and platform overrides. `resources.json` lists sounds, music, levels,
textures, and sprite animations. It also defines worlds and level ordering for
the game.

Scripts describing in-game events or abilities live in `assets/data/scripts/`,
e.g., adding the double jump ability or cutscene scripts under
`scripts/cutscenes/`.

### Important Classes

- `GameScreen` – Primary gameplay screen. It loads levels via
  `GameSceneLoader` and manages the game camera, updates, and transitions to
  pause or game-over states.
- `GameStage` – Scene2D stage for HUD and player status (score, health, floppy
  icons).
- Numerous component classes implement entity behaviors, physics, collision
  logic, AI, etc., living under `core/src/net/dynart/neonsignal/components/`.

### Next Steps for Exploration

1. **Review individual components** in `core/components` to understand how
   entities interact (e.g., `PlayerComponent`, `ActionComponent`).
2. **Study the scripting system** in `core/src/net/dynart/neonsignal/core/script`
   and its usage from cutscene and command scripts.
3. **Explore assets**—level files under `assets/data/levels`, textures in
   `textures/`, and music/sounds. Understanding resource organization helps when
   creating or modifying content.
4. **Investigate input and controller handling** within
   `core/src/net/dynart/neonsignal/core/controller` to see how keyboard, touch,
   and gamepad inputs are unified.

This repository provides a solid example of a modular LibGDX project with a
custom engine, asset pipeline, and cross-platform launchers.
