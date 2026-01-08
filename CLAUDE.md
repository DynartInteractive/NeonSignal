# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Neon Signal is a cross-platform retro platformer game built on LibGDX 1.13.5 with a custom game engine. The project uses Gradle for builds and supports multiple platforms: desktop (LWJGL3), Android, iOS (RoboVM), and muOS (a minimal desktop build for handheld devices).

## Build Commands

**Requirements**: JDK 17+ (OpenJDK 17, Amazon Corretto 17, etc.)

### Build the project
```bash
./gradlew build
```

### Run desktop version
After building, run the desktop launcher from your IDE:
- Right-click `desktop/src/info/dynart/neonsignal/DesktopLauncher.java`
- Select "Run DesktopLauncher.main()"

### Desktop command-line arguments
- `-w [n]` or `-width [n]`: Set video resolution width
- `-h [n]` or `-height [n]`: Set video resolution height
- `-fps [n]`: Force specific frame rate
- `-novsync`: Disable vertical synchronization
- `-window`: Run in windowed mode
- `-borderless`: Make window borderless (requires `-window`)
- `-display [n]`: Run on specific display number
- `-level [s]`: Start with specific level (path or level name from resources.json)

Example: `./gradlew desktop:run -Pargs="-window -level first"`

### Platform-specific builds
- Desktop: `./gradlew desktop:build`
- Android: `./gradlew android:build`
- iOS: `./gradlew ios:build`
- muOS: `./gradlew muos:build`

## Architecture

### Module Structure
- **core**: Platform-independent game logic, entities, components, and screens
- **desktop**: LWJGL3 desktop launcher with command-line argument parsing
- **android**: Android build configuration and launcher
- **ios**: RoboVM iOS build configuration and launcher
- **muos**: Minimal desktop build for handheld devices (no controller support, uses gptokeyb)

### Core Engine Flow

1. **Neon Signal.java** (entry point): Creates and configures the Engine instance
   - Loads configuration from `assets/data/config.json`
   - Sets up `Neon SignalEntityFactory`, `Neon SignalGlassesFadeRenderer`, `Neon SignalScriptLoader`
   - Registers all game screens after resources finish loading

2. **Engine.java**: Core orchestrator managing:
   - Resource loading via AssetManager (from `resources.json`)
   - Screen management and transitions
   - Input handling (keyboard, touch, gamepad unified through GameController)
   - Game loop (update/render)
   - Sound, texture, font, sprite animation, and level managers

3. **Screens** (in `core/src/info/dynart/neonsignal/screens/`):
   - `LoadingScreen`: Initial resource loading
   - `LogoScreen`: Startup logo display
   - `MainMenuScreen`: Main menu navigation
   - `GameScreen`: Primary gameplay screen, manages game scene and level loading
   - `PauseScreen`, `GameOverScreen`, `CompletedScreen`: Game state screens
   - `SettingsScreen`: Game configuration
   - `LevelScreen`: Level selection interface
   - `CutsceneScreen`: In-game cutscene playback
   - `CustomizeKeyboardScreen`, `CustomizeGamepadScreen`, `CustomizeTouchScreen`: Input customization

### Entity Component System

The engine uses a component-based entity system:
- **Entity** (`core/Entity.java`): Base container with position, size, sprite
- **Components** (`core/src/info/dynart/neonsignal/components/`): Define behavior
  - `PlayerComponent`: Player-specific logic and abilities
  - `BodyComponent`: Physics body management
  - `ViewComponent`: Rendering and animation
  - `ActionComponent`: Action states and behaviors
  - `EnemyComponent`: AI and enemy behavior
  - `ColliderComponent`, `GridCollisionComponent`, `EntityCollisionComponent`: Collision handling
  - Many specialized components: `SpringboardComponent`, `ConveyorComponent`, `SpikeComponent`, etc.

Components are attached to entities via `EntityFactory.java` which uses reflection to call factory methods based on entity type from level data.

### Resource Management

**Configuration files** (`assets/data/`):
- `config.json`: Physics constants, player parameters, controller mappings, platform overrides
- `resources.json`: Defines all sounds, music, levels, textures, sprite animations, and world structure

**Level structure**:
- Levels are Tiled TMX files in `assets/data/levels/`
- Organized by worlds: world1 (Beach), world2 (Industrial), world3 (Mountain), etc.
- `GameSceneLoader.java` parses TMX files and instantiates entities using `EntityFactory`

**World organization** (from `resources.json`):
- Worlds have titles and ordered level lists
- Example: "beach" world contains levels: first, beach2, beach3, beach4, beach5

### Scripting System

Scripts enable cutscenes and dynamic behavior without code changes:
- Located in `assets/data/scripts/` (JSON format)
- Script commands in `core/src/info/dynart/neonsignal/core/script/`:
  - `SayCommand`: Display dialog
  - `WalkToCommand`, `WalkToExitCommand`: NPC movement
  - `SetAnimationCommand`: Change entity animation
  - `MoveCameraToCommand`, `SetCameraTargetCommand`, `SetCameraLimitCommand`: Camera control
  - `PlayMusicCommand`: Music playback
  - `DelayCommand`: Timing control
  - `SequenceCommand`, `ParallelCommand`: Command composition
  - `AddPlayerAbilityCommand`: Grant abilities (e.g., double jump)
  - `TriggerCommand`: Activate triggers

Script example: `assets/data/scripts/add_double_jump.json` grants the player double jump ability.

### Input System

Unified input handling via `GameController` (`core/src/info/dynart/neonsignal/core/controller/`):
- Abstracts keyboard, touch, and gamepad input
- Button mappings defined in `config.json`: left, right, up, down, a (jump), b (fire), c (switch), menu
- Platform-specific listeners: `KeyboardListener`, `TouchListener`, `GamepadListener`
- Users can customize mappings in settings screens

## Key Classes to Understand

- **Neon Signal.java**: Application entry point and screen registration
- **Engine.java**: Core game loop and manager orchestration
- **EngineConfig.java**: Configuration loading and platform detection
- **EntityFactory.java**: Entity creation from level data via reflection
- **GameScene.java**: Contains and updates all entities in a level
- **GameSceneLoader.java**: Parses TMX level files and builds GameScene
- **GameScreen.java**: Main gameplay screen managing scene, camera, and transitions
- **GameStage.java**: Scene2D UI layer for HUD (score, health, floppy icons)
- **GameController.java**: Unified input abstraction
- **SoundManager.java**, **TextureManager.java**, **FontManager.java**: Resource managers

## Common Modifications

### Adding a new entity type
1. Create component class in `core/src/info/dynart/neonsignal/components/`
2. Add factory method in `EntityFactory.java` or `Neon SignalEntityFactory.java`
3. Add entity to level TMX file using Tiled editor
4. Define sprite animations in `resources.json` if needed

### Adding a new screen
1. Create screen class extending `Screen` in `core/src/info/dynart/neonsignal/screens/`
2. Register in `Neon Signal.loadingFinished()` using `engine.addScreen()`
3. Transition to screen via `engine.setScreen("screen_name")`

### Adding new sounds/music
1. Place audio file in `assets/data/sounds/` or `assets/data/music/`
2. Add entry to `resources.json` under "sounds" or "music"
3. Play via `engine.getSoundManager().play("sound_name")`

### Creating cutscenes
1. Write script JSON in `assets/data/scripts/cutscenes/`
2. Use script commands to orchestrate dialog, movement, camera control
3. Trigger script from level trigger entity or game event

## File Structure Notes

- **Component files**: All in `core/src/info/dynart/neonsignal/components/`
- **Core engine**: `core/src/info/dynart/neonsignal/core/`
- **Screens**: `core/src/info/dynart/neonsignal/screens/`
- **Platform launchers**: `desktop/src/`, `android/src/`, `ios/src/`, `muos/src/`
- **Assets**: `assets/` (shared across all platforms)
- **Configuration**: `build.gradle` (root), `settings.gradle`, platform-specific `build.gradle` files

## Platform-Specific Notes

### Desktop
- Uses LWJGL3 backend
- Full controller support (gamepad)
- Configurable via command-line arguments

### Android
- Uses Android backend
- Touch controls with customizable button positions
- Native library: libgdx-oboe for low-latency audio

## Documentation

The `docs/` directory is a git submodule containing Neon Signal engine documentation. Use `git submodule update --init` to initialize it after cloning.
