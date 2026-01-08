package net.dynart.neonsignal.core;

public class Level {

    private final String name;
    private final String title;
    private String path;
    private String worldName;

    public Level(String name, String path, String title) {
        this.name = name;
        this.path = path;
        this.title = title;
    }

    public void setWorldName(String value) {
        worldName = value;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String value) {
        path = value;
    }
}

