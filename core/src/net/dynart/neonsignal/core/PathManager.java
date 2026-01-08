package net.dynart.neonsignal.core;

import java.util.HashMap;
import java.util.Map;

public class PathManager {

    private final Map<String, Path> paths = new HashMap<String, Path>();

    public void add(String name, Path path) {
        paths.put(name, path);
    }

    public Path get(String name) {
        return paths.get(name);
    }

    public void clear() {
        paths.clear();
    }
}
