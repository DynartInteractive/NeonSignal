package net.dynart.neonsignal.core;

import static net.dynart.neonsignal.core.Grid.Layer.BLOCK;
import static net.dynart.neonsignal.core.Grid.Layer.TOP_BLOCK;

import com.badlogic.gdx.math.Vector2;

import net.dynart.neonsignal.components.BodyComponent;

public class Grid {

    public static class Layer {

        public static final int BLOCK = 1;
        public static final int TOP_BLOCK = 2;
        public static final int SLIDER = 4;
        public static final int WATER = 8;
        public static final int SPIKE = 16;
        public static final int POISON = 32;
        public static final int DEATH = 64;

        public static final int[] values = { BLOCK, TOP_BLOCK, SLIDER, WATER, SPIKE, POISON, DEATH };

        public static int[] values() {
            return values;
        }
    }

    private final int[][] data;
    private final EngineConfig config;

    private int width;
    private int height;
    private boolean opened;

    public Grid(Engine engine) {
        config = engine.getConfig();
        data = new int[config.getTilemapMaxHeight()][config.getTilemapMaxWidth()];
    }

    private boolean isInner(int x, int y) {
        return x > -1 && y > -1 && x < width && y < height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void set(int layer, int x, int y, boolean value) {
        if (isInner(x, y)) {
            if (value) {
                data[y][x] |= layer;
            } else {
                data[y][x] &= ~layer;
            }
        }
    }

    public void open() {
        opened = true;
    }

    public void clear() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[j][i] = 0;
            }
        }
        width = 0;
        height = 0;
        opened = false;
    }

    public boolean get(int layer, int x, int y) {
        boolean xOutLeft = x < 0;
        boolean xOutRight = x >= width;
        boolean xOut = xOutLeft || xOutRight;
        boolean blockLayer = layer == BLOCK  || layer == Layer.TOP_BLOCK;
        if (!blockLayer && xOut) {
            return false;
        }
        if (blockLayer && xOut) {
            if (opened) {
                return xOutLeft ? get(layer, 0, y) : get(layer, x - 1, y);
            } else {
                return true;
            }
        }
        if (y >= height) {
            return get(layer, x, height - 1);
        }
        else if (y < 0) {
            return get(layer, x, 0);
        }
        return (data[y][x] & layer) == layer;
    }

    public boolean get(int layer, float worldX, float worldY) {
        return get(layer, getX(worldX), getY(worldY));
    }

    public int getX(float worldX) {
        int result = (int)(worldX / config.getTileWidth());
        if (worldX < 0) {
            result--;
        }
        return result;
    }

    public float getWorldX(int x) {
        return x * config.getTileWidth();
    }

    public int getY(float worldY) {
        int result = (int)(worldY / config.getTileHeight());
        if (result < 0) {
            result--;
        }
        return result;
    }

    public float getWorldY(int y) {
        return y * config.getTileHeight();
    }

    public boolean bodyInBlock(BodyComponent body) {
        return bodyIn(body, BLOCK);
    }

    public boolean bodyIn(BodyComponent body, int layer) {
        for (int j = getY(body.getBottom()); j <= getY(body.getTop()); j++) {
            for (int i = getX(body.getLeft()); i <= getX(body.getRight()); i++) {
                if (get(layer, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Vector2 getIntersection(Vector2 a, Vector2 b) {
        float tileW = config.getTileWidth();
        float tileH = config.getTileHeight();

        // World -> grid space
        Vector2 start = new Vector2(a.x / tileW, a.y / tileH);
        Vector2 end   = new Vector2(b.x / tileW, b.y / tileH);
        Vector2 rayDir = new Vector2(end.x - start.x, end.y - start.y);

        if (rayDir.len() == 0f) return a;

        rayDir.nor();

        int mapX = (int)Math.floor(start.x);
        int mapY = (int)Math.floor(start.y);

        float deltaDistX = (rayDir.x == 0f) ? Float.MAX_VALUE : Math.abs(1f / rayDir.x);
        float deltaDistY = (rayDir.y == 0f) ? Float.MAX_VALUE : Math.abs(1f / rayDir.y);

        int stepX = (rayDir.x < 0f) ? -1 : 1;
        int stepY = (rayDir.y < 0f) ? -1 : 1;

        float sideDistX = (rayDir.x < 0f)
                ? (start.x - mapX) * deltaDistX
                : (mapX + 1f - start.x) * deltaDistX;

        float sideDistY = (rayDir.y < 0f)
                ? (start.y - mapY) * deltaDistY
                : (mapY + 1f - start.y) * deltaDistY;

        // Length of the segment in grid units
        float rayLength = start.dst(end);
        float perpWallDist = 0f;
        boolean hit = false;
        boolean hitFromHorizontal = false;

        final float EPS = 1e-4f;

        // DDA
        while (!hit) {
            // Look ahead: what's the next boundary distance?
            float nextDist = Math.min(sideDistX, sideDistY);

            // If stepping to the next boundary would exceed the segment, stop: no grid hit
            if (nextDist > rayLength + EPS) break;

            // Take the step that hits first
            if (sideDistX < sideDistY) {
                mapX += stepX;
                perpWallDist = sideDistX;
                sideDistX += deltaDistX;
                hitFromHorizontal = false; // crossed a vertical boundary
            } else {
                mapY += stepY;
                perpWallDist = sideDistY;
                sideDistY += deltaDistY;
                hitFromHorizontal = true; // crossed a horizontal boundary
            }

            // Cell tests
            if (get(BLOCK, mapX, mapY)) {
                hit = true;
            } else if (get(TOP_BLOCK, mapX, mapY)) {
                // Only block if hit from above (crossing a horizontal boundary while moving downward)
                if (hitFromHorizontal && stepY == -1) {
                    hit = true;
                }
            }
        }

        // If nothing hit within the segment, return b
        if (!hit) return b;

        // Compute exact intersection in grid space
        Vector2 intersection = new Vector2(
            start.x + rayDir.x * perpWallDist,
            start.y + rayDir.y * perpWallDist
        );

        // Back to world space
        intersection.x *= tileW;
        intersection.y *= tileH;
        return intersection;
    }
}
