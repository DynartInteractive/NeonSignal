package net.dynart.neonsignal.core;

import java.util.List;

public class World {

    private final String name;
    private final String title;
    private final List<Level> levels;

    public World(String name, String title, List<Level> levels) {
        this.name = name;
        this.title = title;
        this.levels = levels;
    }

    public String getTitle() {
        return title;
    }

    public List<Level> getLevels() {
        return levels;
    }
}
