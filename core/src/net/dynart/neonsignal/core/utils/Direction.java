package net.dynart.neonsignal.core.utils;

import java.util.HashMap;
import java.util.Map;

public enum Direction {

    RIGHT(1, 0),
    UP(0, 1),
    LEFT(-1, 0),
    DOWN(0, -1);

    public static final Map<String, Direction> ByName = new HashMap<String, Direction>() {{
        put("left", LEFT);
        put("right", RIGHT);
        put("down", DOWN);
        put("up", UP);
    }};

    public static final Map<Direction, Direction> Inverse = new HashMap<Direction, Direction>() {{
        put(RIGHT, LEFT);
        put(LEFT, RIGHT);
        put(UP, DOWN);
        put(DOWN, UP);
    }};

    private int x;
    private int y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Direction inverse() {
        return Inverse.get(this);
    }

    public static Direction get(String name) {
        return ByName.get(name);
    }

}
