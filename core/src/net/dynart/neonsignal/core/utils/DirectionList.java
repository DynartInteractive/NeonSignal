package net.dynart.neonsignal.core.utils;

import java.util.ArrayList;
import java.util.List;

public class DirectionList {

    private final List<Direction> list = new ArrayList<Direction>();

    public DirectionList(boolean right, boolean up, boolean left, boolean down) {
        if (left) {
            list.add(Direction.LEFT);
        }
        if (right) {
            list.add(Direction.RIGHT);
        }
        if (up) {
            list.add(Direction.UP);
        }
        if (down) {
            list.add(Direction.DOWN);
        }
    }

    public boolean has(Direction direction) {
        return list.contains(direction);
    }

}
