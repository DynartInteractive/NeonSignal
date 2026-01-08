package net.dynart.neonsignal.core.ui;

import net.dynart.neonsignal.core.Level;

public class MenuCursorItemLevelData {

    private final Level level;
    private final int worldIndex;
    private final int levelIndex;

    public MenuCursorItemLevelData(Level level, int worldIndex, int levelIndex) {
        this.level = level;
        this.worldIndex = worldIndex;
        this.levelIndex = levelIndex;
    }

    public int getWorldIndex() {
        return worldIndex;
    }

    public Level getLevel() {
        return level;
    }

    public int getLevelIndex() {
        return levelIndex;
    }

}
