package net.dynart.neonsignal.core;

import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {

    private final List<World> worlds = new ArrayList<>();

    private Map<String, Level> levelMap;

    public void load(JsonValue resourcesJson) {
        levelMap = loadLevels(resourcesJson);
        JsonValue jsonWorlds = resourcesJson.get("worlds");
        String[] worldNames = jsonWorlds.get("order").asStringArray();
        for (String worldName : worldNames) {
            JsonValue jsonWorld = jsonWorlds.get(worldName);
            String title = jsonWorld.getString("title");
            String[] levelNames = jsonWorld.get("levels").asStringArray();
            List<Level> levels = createLevels(levelMap, levelNames, worldName);
            World world = new World(jsonWorld.name(), title, levels);
            worlds.add(world);
        }
    }

    public List<World> getWorlds() {
        return worlds;
    }

    private List<Level> createLevels(Map<String, Level> levelMap, String[] levelNames, String worldName) {
        List<Level> levels = new ArrayList<>();
        for (String levelName : levelNames) {
            Level level = levelMap.get(levelName);
            level.setWorldName(worldName);
            levels.add(level);
        }
        return levels;
    }

    public boolean has(String name) {
        return levelMap.containsKey(name);
    }

    public Level get(String name) {
        return levelMap.get(name);
    }

    private Map<String, Level> loadLevels(JsonValue resourcesJson) {
        JsonValue jsonLevels = resourcesJson.get("levels");
        Map<String, Level> levelMap = new HashMap<>();
        for (JsonValue jsonLevel = jsonLevels.child(); jsonLevel != null; jsonLevel = jsonLevel.next()) {
            String title = jsonLevel.getString("title", null);
            String path = jsonLevel.getString("path");
            Level level = new Level(jsonLevel.name(), path, title);
            levelMap.put(jsonLevel.name(), level);
        }
        return levelMap;
    }
}
