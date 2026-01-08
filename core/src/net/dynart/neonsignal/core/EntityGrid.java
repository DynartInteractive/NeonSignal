package net.dynart.neonsignal.core;

import java.util.LinkedList;
import java.util.List;

import net.dynart.neonsignal.components.BodyComponent;

public class EntityGrid {

    private static class EntitySet extends LinkedList<Entity> { }

    private static class Position {

        final float dividerX;
        final float dividerY;

        int top;
        int bottom;
        int left;
        int right;

        // 1 grid unit = 8x8 tile
        public Position(EngineConfig config) {
            dividerX = config.getTileWidth() * 8f;
            dividerY = config.getTileHeight() * 8f;
        }

        private void init(EntitySet[][] map, Entity e) {
            BodyComponent body = e.getComponent(BodyComponent.class);
            bottom = (int)(body.getBottom() / dividerY);
            if (bottom < 0) bottom = 0;
            if (bottom >= map.length) bottom = map.length - 1;
            top = (int)(body.getTop() / dividerY);
            if (top < 0) top = 0;
            if (top >= map.length) top = map.length - 1;
            left = (int)(body.getLeft() / dividerX);
            if (left < 0) left = 0;
            if (left >= map[0].length) left = map[0].length - 1;
            right = (int)(body.getRight() / dividerX);
            if (right < 0) right = 0;
            if (right >= map[0].length) right = map[0].length - 1;
        }
    }

    private final EntitySet[][] map;
    private final Position position;
    private final EntitySet result = new EntitySet();

    public EntityGrid(EngineConfig config) {
        position = new Position(config);
        int maxWidth = (int)(config.getTilemapMaxWidth() / 8f);
        int maxHeight = (int)(config.getTilemapMaxHeight() / 8f);
        map = new EntitySet[maxHeight][maxWidth];
        for (int j = 0; j < maxHeight; j++) {
            for (int i = 0; i < maxWidth; i++) {
                map[j][i] = new EntitySet();
            }
        }
    }

    public void add(Entity e) {
        position.init(map, e);
        for (int j = position.bottom; j <= position.top; j++) {
            for (int i = position.left; i <= position.right; i++) {
                map[j][i].add(e);
            }
        }
    }

    public void remove(Entity e) {
        position.init(map, e);
        for (int j = position.bottom; j <= position.top; j++) {
            for (int i = position.left; i <= position.right; i++) {
                map[j][i].remove(e);
            }
        }
    }

    public void clear() {
        for (EntitySet[] entitySets : map) {
            for (EntitySet entitySet : entitySets) {
                entitySet.clear();
            }
        }
    }

    public List<Entity> getNearby(Entity e) {
        result.clear();
        position.init(map, e);
        for (int j = position.bottom; j <= position.top; j++) {
            for (int i = position.left; i <= position.right; i++) {
                if (!result.contains(e)) {
                    result.add(e);
                }
            }
        }
        return result;
    }

}
