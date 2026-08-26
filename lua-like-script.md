# Lua-like Script Format — Plan

A plan to replace the JSON cutscene/script format with Lua-syntax files.

## Goal

Make scripts in `assets/data/scripts/` more readable than JSON, gain comments and multi-line strings, and get free editor support (syntax highlighting, bracket matching, basic linting) by piggybacking on Lua's tooling ecosystem.

We are **not** building a scripting language. We borrow Lua's syntax and use a Lua parser to read structured data — every existing Command stays as Java.

## Why Lua syntax

Lua's `name { ... }` call form is exactly the shape our commands already have. Compare:

```json
{"say": {"name": "coolfox", "text": "Hi", "start": true}}
```

```lua
say { name = "coolfox", text = "Hi", start = true }
```

Wins for free:
- `--` comments
- `[[ ... ]]` multi-line strings (no more `\n` salad in dialog)
- Trailing commas
- Nested array/record tables map cleanly to nested commands and `nexus_says.lines`
- Every editor on the planet has a Lua highlighter

## Approach

1. Add **LuaJ** (pure Java, RoboVM-safe in principle) as a `core` dependency.
2. Build a sandboxed Lua environment per script load:
   - No `os`, `io`, `package`, `require`, `debug`, `loadfile`, `dofile`. We don't need them and skipping them dodges RoboVM gaps and security surface.
   - Register one global Lua function per command (`say`, `delay`, `parallel`, `sequence`, `walk_to`, `move_camera_to`, `set_camera_target`, `play_music`, `set_visible`, `set_animation`, `set_movement_active`, `set_parent`, `add_player_ability`, `trigger`, `set_camera_limit`, `walk_to_exit`, `upgrade_dash_cooldown`, `upgrade_dash_longevity`, `nexus_says`).
3. Each Lua command function:
   - Receives a Lua table.
   - Converts it to a `JsonValue` via a small recursive helper (Lua table → `JsonValue` object with named children for keyed fields and array children for positional entries).
   - **Reuses the existing `ScriptLoader.createXxx(JsonValue)` factory** to produce the actual `Command`. No command class moves.
   - Returns a marker (the produced `Command`) so block commands like `parallel` / `sequence` can collect children by inspecting the Lua table.
4. Top-level is an implicit `sequence`. Statements at file scope queue into one ordered list.

Result: one new `LuaScriptLoader.java`, one `LuaTableToJsonValue` helper, and a dependency. Existing command classes are untouched.

## Long-string indent stripping

We want this to look nice:

```lua
say {
    name = "barman", start = true, finish = true,
    text = [[
        What's ya flavor,
        my man?
    ]]
}
```

Lua's `[[ ]]` doesn't strip indentation, so we do it at the boundary in `LuaTableToJsonValue` (or right before passing to `createSay`):

- If the string starts with a newline, drop the first newline.
- Find the minimum leading-whitespace length across all non-blank lines.
- Strip that many leading whitespace chars from every line.
- Optionally trim a single trailing newline.

This is the same rule Python's `textwrap.dedent` uses. ~20 lines of Java. Apply uniformly to every string field — no need to mark which fields are "text-y."

## File layout

- Scripts live in `assets/data/scripts/` with `.lua` extension (alongside or replacing the `.json` files).
- During migration, the loader can sniff the extension: `.lua` → `LuaScriptLoader`, `.json` → existing `ScriptLoader`. After all scripts are converted, drop the JSON path.
- `NeonSignalScriptLoader` becomes a thin subclass of `LuaScriptLoader` (same role it has today).

## Migration

1. Land `LuaScriptLoader` with the JSON loader still active and extension-sniffing in place.
2. Write a one-shot converter (Java or a script) that walks `assets/data/scripts/**/*.json` and emits `.lua` siblings. The conversion is mechanical:
   - `{"name": {...}}` → `name { ... }`
   - JSON arrays → Lua array tables
   - Strings containing `\n` → `[[ ... ]]` with proper indentation
3. Verify each cutscene/event script in-game.
4. Delete the JSON files and the JSON loader path.

## RoboVM smoke test (do this first)

Before committing to the port, verify LuaJ works on iOS under RoboVM with a trivial script — load a string, run it, read back a table. LuaJ uses some reflection that has historically been fine on RoboVM but is worth confirming on the current toolchain rather than discovering at the end.

## Naming review (optional, separate from format change)

Most current command names read fine. A few worth considering:

- **`set_movement_active`** — double modifier (`set X active`) is awkward. Suggest `enable_movement` / `disable_movement`, or just `set_movement { enabled = true }`.
- **`upgrade_dash_longevity`** — "longevity" is unusual game vocabulary; `duration` or `length` is more standard. Pairs better with `upgrade_dash_cooldown` too. Or fold both into `upgrade_dash { kind = "duration" }` if more dash upgrades are coming.
- **`trigger`** — fine as a verb, but `trigger` is also a noun elsewhere in the engine (trigger entities). `fire_trigger` or `activate_trigger` would remove ambiguity at the call site.
- **`nexus_says`** — fine if Nexus is a specific character with a unique typewriter style; if it's actually a general typewriter-monologue UI just used for that character, `monologue` or `typewriter_say` would generalize.

These are independent of the format change — could be done in the same converter pass, or never. Leaving as-is is also fine.

## Open questions

- **Skip handling.** Today `ScriptLoader` collects `SkippableCommand` instances during load. The Lua flow goes through the same factories, so this should keep working unchanged — confirm during implementation.
- **Hot reload.** Lua scripts are easy to re-parse on change. Worth wiring up for cutscene authoring? Not in scope for the initial port, but cheap to add later.
- **Error messages.** When a Lua script has a typo (e.g., `slay { ... }` instead of `say`), we'd get an undefined-global error from LuaJ. Worth adding a strict mode that turns unknown globals into a clearer "no such command" error pointing at file/line.

## Out of scope

- Loops, conditionals, variables, expressions in scripts. We're using Lua's syntax, not its semantics. If a script wants logic, extend the command set instead.
- Cross-script imports / `require`.
- Replacing the `Command` interface or the per-frame `act(delta)` model.

## Reference: example before/after

`assets/data/scripts/cutscenes/level1_intro2.lua` (already written as a sample) shows the target style — that file is the visual reference for the conversion.
