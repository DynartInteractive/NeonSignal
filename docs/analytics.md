# Plan: Google Analytics 4 Integration (Measurement Protocol)

## Context

The goal is to track player paths and identify where they get stuck in NeonSignal.
Using GA4's **Measurement Protocol** (pure REST API over HTTPS) so it works
identically on desktop, Android, iOS, and muOS — no platform SDK needed.
LibGDX's built-in `Gdx.net.sendHttpRequest()` handles cross-platform HTTP.

Events to track: level start, player death (with coordinates), checkpoint reached,
and level completion stats. Fire-and-forget (events dropped if offline). Users can
opt out via a new toggle in SettingsScreen.

---

## GA4 Setup (manual step before implementation)

1. Create a **GA4 property** at analytics.google.com
2. Under Admin → Data Streams → Add stream → choose "Web" (not app — GA4 MP works via web stream for games)
3. Copy the **Measurement ID** (`G-XXXXXXXXXX`)
4. Under Admin → Data Streams → your stream → Measurement Protocol API secrets → Create → copy the **API secret**
5. Store both values in `assets/data/config.json` (see step below)

---

## Files to Create

### 1. `core/src/net/dynart/neonsignal/core/AnalyticsManager.java` (NEW)

Responsibilities:
- Holds `clientId` (anonymous UUID, persisted in `User.preferences` under key `"analytics_client_id"`)
- Holds `sessionId` (epoch timestamp string, generated at launch)
- Maintains `attemptCount` per level (reset on `trackLevelStart`, incremented on `trackDeath`)
- Reads `measurementId` and `apiSecret` from `EngineConfig`
- `setEnabled(boolean)` — checks `Settings.isAnalyticsEnabled()`
- `trackLevelStart(Level level)`
- `trackDeath(Level level, float x, float y)`
- `trackCheckpoint(Level level, float x, float y)`
- `trackLevelCompleted(Level level, PlayerComponent player, GameScene scene)`
- Private `send(String eventName, Map<String,String> params)` — builds GA4 JSON payload and calls `Gdx.net.sendHttpRequest()` with a no-op listener

**GA4 Measurement Protocol endpoint:**
```
POST https://www.google-analytics.com/mp/collect
     ?measurement_id={measurementId}&api_secret={apiSecret}
Content-Type: application/json

{
  "client_id": "<uuid>",
  "events": [{
    "name": "<event_name>",
    "params": {
      "session_id": "...",
      "level_name": "...",
      "death_x": "...",   // GA4 MP requires string or number values
      ...
    }
  }]
}
```

Build JSON with `StringBuilder` (no new dependency needed).
Use `Gdx.app.postRunnable()` if called from a non-render thread.

**clientId bootstrap:**
```java
String id = user.getPreferences().getString("analytics_client_id", "");
if (id.isEmpty()) {
    id = java.util.UUID.randomUUID().toString();
    user.getPreferences().putString("analytics_client_id", id).flush();
}
```

---

## Files to Modify

### 2. `assets/data/config.json`
Add to `"all"` section:
```json
"analytics_measurement_id": "",
"analytics_api_secret": ""
```
Leave empty by default; user fills in their own GA4 credentials. The `EngineConfig`
pattern already supports custom overrides via `config-custom.json` (gitignored) so
credentials can stay out of the repo.

### 3. `core/src/net/dynart/neonsignal/NeonSignalEngineConfig.java` (or `EngineConfig.java`)
Add two fields with getters:
- `String analyticsMeasurementId`
- `String analyticsApiSecret`
  Load from config JSON in `loadFromJson()`.

### 4. `core/src/net/dynart/neonsignal/core/Engine.java`
- Add `private AnalyticsManager analyticsManager;`
- Initialize in `loadingFinished()` (after `User` is available):
  `analyticsManager = new AnalyticsManager(config, getUser(), settings);`
- Add `public AnalyticsManager getAnalyticsManager()` getter

### 5. `core/src/net/dynart/neonsignal/core/Settings.java`
Add:
- `boolean analyticsEnabled` (default `true`)
- `public boolean isAnalyticsEnabled()` / `setAnalyticsEnabled(boolean)`
- Persist in `Preferences` under key `"analytics_enabled"`

### 6. `core/src/net/dynart/neonsignal/screens/SettingsScreen.java`
Add an "Analytics" toggle row (reuse existing UI pattern for boolean settings already
present in the screen). On toggle: call `settings.setAnalyticsEnabled(value)` and
`engine.getAnalyticsManager().setEnabled(value)`.

### 7. `core/src/net/dynart/neonsignal/screens/GameScreen.java`
- **`loadLevel(Level level)`** → after `currentLevel = level;` add:
  `engine.getAnalyticsManager().trackLevelStart(level);`
- **`prepareForGameOver()`** → before `gameOverCountDown = 0.5f;` add:
  ```java
  Entity player = gameScene.getPlayer();
  BodyComponent body = player.getComponent(BodyComponent.class);
  engine.getAnalyticsManager().trackDeath(currentLevel, body.getCenterX(), body.getBottom());
  ```

### 8. `core/src/net/dynart/neonsignal/components/ReviveComponent.java`
- In `update()`, after `messageHandler.send(TOUCHED)` add:
  ```java
  GameScreen gameScreen = (GameScreen) engine.getScreen("game");
  engine.getAnalyticsManager().trackCheckpoint(
      gameScreen.getCurrentLevel(), body.getCenterX(), body.getBottom());
  ```
  (If `getCurrentLevel()` doesn't exist yet, add it as a simple getter in GameScreen.)

### 9. `core/src/net/dynart/neonsignal/screens/CompletedScreen.java`
- In `show()`, after loading player and gameScene, add:
  ```java
  engine.getAnalyticsManager().trackLevelCompleted(
      gameScreen.getCurrentLevel(), player, gameScene);
  ```

---

## Event Schema

| Event name | Key params |
|---|---|
| `level_start` | `level_name`, `session_id` |
| `player_death` | `level_name`, `death_x`, `death_y`, `attempt_number`, `session_id` |
| `checkpoint_reached` | `level_name`, `checkpoint_x`, `checkpoint_y`, `session_id` |
| `level_completed` | `level_name`, `enemies_defeated`, `total_enemies`, `secrets_found`, `total_secrets`, `items_collected`, `total_items`, `score`, `session_id` |

All numeric values sent as strings (GA4 MP accepts both).

---

## Verification

1. Fill in real GA4 credentials in `config.json` (or `config-custom.json`)
2. Run desktop: `./gradlew desktop:run -Pargs="-window -level first"`
3. Play through a level: start, die, reach checkpoint, complete
4. In GA4 → DebugView (enable via `?debug_mode=1` param in the payload temporarily)
   or check **Realtime** report — events should appear within seconds
5. Check Settings screen shows the analytics toggle and disabling it stops events
6. Build Android and verify events appear from that platform too

---

## Key File Paths

- `core/src/net/dynart/neonsignal/core/Engine.java`
- `core/src/net/dynart/neonsignal/core/Settings.java`
- `core/src/net/dynart/neonsignal/core/AnalyticsManager.java` ← new file
- `core/src/net/dynart/neonsignal/NeonSignalEngineConfig.java`
- `core/src/net/dynart/neonsignal/screens/GameScreen.java`
- `core/src/net/dynart/neonsignal/screens/SettingsScreen.java`
- `core/src/net/dynart/neonsignal/screens/CompletedScreen.java`
- `core/src/net/dynart/neonsignal/components/ReviveComponent.java`
- `assets/data/config.json`
