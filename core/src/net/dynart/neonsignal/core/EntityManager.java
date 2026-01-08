package net.dynart.neonsignal.core;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.dynart.neonsignal.components.BlockComponent;
import net.dynart.neonsignal.components.BodyComponent;
import net.dynart.neonsignal.components.PlatformComponent;
import net.dynart.neonsignal.components.PlayerComponent;
import net.dynart.neonsignal.components.VelocityComponent;
import net.dynart.neonsignal.components.ViewComponent;
import net.dynart.neonsignal.core.utils.AreaPosition;

public class EntityManager {

    public static final int TOP_LAYER = 1000;
    public static final int AREA_SIZE = 160;

    private final List<Entity> entities = new ArrayList<>();
    private final Map<Class, List<Entity>> listByClass = new HashMap<>();
    private final Map<Integer, Map<Class, Set<Entity>>> areas = new HashMap<>();
    private final Map<String, List<Entity>> entitiesByGroup = new HashMap<>();
    private final List<Entity> removeList = new ArrayList<>();
    private final List<Entity> addLaterList = new ArrayList<>();
    private final Map<String, Entity> listByName = new HashMap<>();
    private final Map<Integer, List<Entity>> listByLayer = new HashMap<>();
    private final List<Entity> emptyEntityList = new LinkedList<>();
    private final Set<Entity> emptyEntitySet = new HashSet<>();
    private final Set<Integer> layers = new TreeSet<>(); // need an ordered list
    private boolean inAnimation;
    private boolean lastInAnimation;

    public EntityManager() {
    }

    public void add(Entity entity) {
        addLaterList.add(entity);
    }

    public Entity getByName(String name) {
        if (listByName.containsKey(name)) {
            return listByName.get(name);
        }
        throw new RuntimeException("Can't get entity: " + name);
    }

    public Set<Integer> getLayers() {
        return layers;
    }

    public List<Entity> getAllByLayer(int layer) {
        List<Entity> result = emptyEntityList;
        if (listByLayer.containsKey(layer)) {
            result = listByLayer.get(layer);
        }
        return result;
    }

    public void enableGroup(String group) {
        for (Entity entity : entitiesByGroup.get(group)) {
            entity.setActive(true);
        }
    }

    public List<Entity> getAllByClass(Class cls) {
        return listByClass.get(cls);
    }

    public Set<Entity> getAllByClassAndArea(Class cls, Entity entity) {
        BodyComponent body = entity.getComponent(BodyComponent.class);
        AreaPosition pos = body.getAreaPosition();
        Set<Entity> result = new HashSet<>();
        result.addAll(getAllByClassAndAreaXY(cls, pos.getLeft(), pos.getTop()));
        result.addAll(getAllByClassAndAreaXY(cls, pos.getRight(), pos.getTop()));
        result.addAll(getAllByClassAndAreaXY(cls, pos.getLeft(), pos.getBottom()));
        result.addAll(getAllByClassAndAreaXY(cls, pos.getRight(), pos.getBottom()));
        return result;
    }

    private Set<Entity> getAllByClassAndAreaXY(Class cls, int areaX, int areaY) {
        Map<Class, Set<Entity>> entitiesByClass = areas.get(getAreaId(areaX, areaY));
        if (entitiesByClass == null) {
            return emptyEntitySet;
        }
        Set<Entity> result = entitiesByClass.get(cls);
        return result == null ? emptyEntitySet : result;
    }

    public Set<Entity> getAllBySegment(Vector2 a, Vector2 b, List<Class> clsList) {
        Set<Entity> result = new HashSet<>();
        float areaSize = AREA_SIZE;

        // Convert world coordinates to "area grid" coordinates.
        Vector2 start = new Vector2(a.x / areaSize, a.y / areaSize);
        Vector2 end = new Vector2(b.x / areaSize, b.y / areaSize);

        // Determine the starting and ending area cell coordinates.
        int cellX = (int)Math.floor(start.x);
        int cellY = (int)Math.floor(start.y);
        int endCellX = (int)Math.floor(end.x);
        int endCellY = (int)Math.floor(end.y);

        // Compute differences in the area grid space.
        float dx = end.x - start.x;
        float dy = end.y - start.y;

        // tDeltaX and tDeltaY are the parametric distances needed to cross a cell in x and y.
        float tDeltaX = (dx == 0) ? Float.MAX_VALUE : Math.abs(1 / dx);
        float tDeltaY = (dy == 0) ? Float.MAX_VALUE : Math.abs(1 / dy);

        int stepX = (dx < 0) ? -1 : 1;
        int stepY = (dy < 0) ? -1 : 1;

        // Compute the initial tMax values: distances from the start position to the first vertical/horizontal boundaries.
        float nextBoundaryX = (dx < 0) ? start.x - cellX : (cellX + 1 - start.x);
        float nextBoundaryY = (dy < 0) ? start.y - cellY : (cellY + 1 - start.y);
        float tMaxX = (dx == 0) ? Float.MAX_VALUE : nextBoundaryX * tDeltaX;
        float tMaxY = (dy == 0) ? Float.MAX_VALUE : nextBoundaryY * tDeltaY;

        // Add entities from the starting area using the filter list.
        addEntitiesFromArea(cellX, cellY, result, clsList);

        // Traverse the area grid cells until we reach the cell containing the end point.
        while (cellX != endCellX || cellY != endCellY) {
            if (tMaxX < tMaxY) {
                cellX += stepX;
                tMaxX += tDeltaX;
            } else {
                cellY += stepY;
                tMaxY += tDeltaY;
            }
            addEntitiesFromArea(cellX, cellY, result, clsList);
        }

        return result;
    }

    // Helper method that adds entities from an area cell based on the component class filter.
    private void addEntitiesFromArea(int cellX, int cellY, Set<Entity> result, List<Class> clsList) {
        int areaId = getAreaId(cellX, cellY);
        Map<Class, Set<Entity>> areaEntities = areas.get(areaId);
        if (areaEntities != null) {
            for (Class cls : clsList) {
                Set<Entity> entities = areaEntities.get(cls);
                if (entities != null) {
                    result.addAll(entities);
                }
            }
        }
    }

    private int getAreaId(int areaX, int areaY) {
        return areaX + areaY * 100;
    }

    public boolean getIntersection(Vector2 a, Vector2 b, Vector2 out, Entity excludeEntity) {

        final Vector2 bestIntersection = new Vector2();
        final Vector2 temp = new Vector2();
        final Vector2 leftTop = new Vector2();
        final Vector2 leftBottom = new Vector2();
        final Vector2 rightTop = new Vector2();
        final Vector2 rightBottom = new Vector2();

        float bestDistSq = Float.MAX_VALUE;

        Set<Entity> entities = getAllBySegment(a, b, List.of(
            BlockComponent.class,
            PlatformComponent.class
        ));

        boolean segmentGoesDownwards = a.y > b.y;

        for (Entity entity : entities) {

            if (entity == excludeEntity) continue;

            BodyComponent body = entity.getComponent(BodyComponent.class);

            leftTop.set(body.getLeft(), body.getTop());
            rightTop.set(body.getRight(), body.getTop());

            boolean fullBlock = entity.hasComponent(BlockComponent.class);
            boolean canHaveTopIntersection = fullBlock || segmentGoesDownwards;

            // Intersect with top side
            if (canHaveTopIntersection && Intersector.intersectSegments(a, b, leftTop, rightTop, temp)) {
                if (out == null) return true;
                float dSq = a.dst2(temp);
                if (dSq < bestDistSq) {
                    bestDistSq = dSq;
                    bestIntersection.set(temp);
                }
            }

            if (!fullBlock) continue;

            leftBottom.set(body.getLeft(), body.getBottom());

            // Intersect with left side
            if (Intersector.intersectSegments(a, b, leftTop, leftBottom, temp)) {
                if (out == null) return true;
                float dSq = a.dst2(temp);
                if (dSq < bestDistSq) {
                    bestDistSq = dSq;
                    bestIntersection.set(temp);
                }
            }

            rightBottom.set(body.getRight(), body.getBottom());

            // Intersect with right side
            if (Intersector.intersectSegments(a, b, rightTop, rightBottom, temp)) {
                if (out == null) return true;
                float dSq = a.dst2(temp);
                if (dSq < bestDistSq) {
                    bestDistSq = dSq;
                    bestIntersection.set(temp);
                }
            }

            // Intersect with bottom side
            if (Intersector.intersectSegments(a, b, leftBottom, rightBottom, temp)) {
                if (out == null) return true;
                float dSq = a.dst2(temp);
                if (dSq < bestDistSq) {
                    bestDistSq = dSq;
                    bestIntersection.set(temp);
                }
            }
        }

        if (bestDistSq != Float.MAX_VALUE) {
            out.set(bestIntersection);
            return true;
        }

        return false;
    }

    public void update(float deltaTime) {
        for (Entity entity : entities) {
            entity.preUpdate(deltaTime);
        }
        // very ugly, but working solution (cause: parented entities)
        for (Entity entity : entities) {
            entity.updateVerticalVelocity(deltaTime);
        }
        //
        for (Entity entity : entities) {
            entity.update(deltaTime);
            updateAreas(entity);
        }
        for (Entity entity : entities) {
            entity.postUpdate(deltaTime);
        }
        removeEntities();
        addToLists();
        lastInAnimation = inAnimation;
    }

    void addToGroup(String group, Entity entity) {
        if (!entitiesByGroup.containsKey(group)) {
            entitiesByGroup.put(group, new ArrayList<>());
        }
        entitiesByGroup.get(group).add(entity);
    }

    private void updateAreas(Entity entity) {
        BodyComponent body = entity.getComponent(BodyComponent.class);
        removeFromAreas(body.getLastAreaPosition(), entity);
        addToAreas(body.getAreaPosition(), entity);
    }

    private void removeFromAreas(AreaPosition pos, Entity entity) {
        removeFromArea(pos.getLeft(), pos.getTop(), entity);
        removeFromArea(pos.getRight(), pos.getTop(), entity);
        removeFromArea(pos.getLeft(), pos.getBottom(), entity);
        removeFromArea(pos.getRight(), pos.getBottom(), entity);
    }

    private void removeFromArea(int areaX, int areaY, Entity entity) {
        int areaId = getAreaId(areaX, areaY);
        Map<Class, Set<Entity>> entitiesByClass = areas.get(areaId);
        try {
            for (Class cls : entitiesByClass.keySet()) {
                Set<Entity> entities = entitiesByClass.get(cls);
                entities.remove(entity);
            }
        } catch (NullPointerException ignore) {
        }
    }

    private void addToAreas(AreaPosition pos,  Entity entity) {
        addToArea(pos.getLeft(), pos.getTop(), entity);
        addToArea(pos.getRight(), pos.getTop(), entity);
        addToArea(pos.getLeft(), pos.getBottom(), entity);
        addToArea(pos.getRight(), pos.getBottom(), entity);
    }

    private void addToArea(int areaX, int areaY, Entity entity) {
        int areaId = getAreaId(areaX, areaY);
        if (!areas.containsKey(areaId)) {
            areas.put(areaId, new HashMap<>());
        }
        Map<Class, Set<Entity>> entitiesByClass = areas.get(areaId);
        for (Class cls : entity.getClassList()) {
            if (!entitiesByClass.containsKey(cls)) {
                entitiesByClass.put(cls, new HashSet<>());
            }
            Set<Entity> entities = entitiesByClass.get(cls);
            entities.add(entity);
        }
    }

    public void addToLists(Entity entity) {
        entities.add(entity);
        for (Class cls : entity.getClassList()) {
            if (!listByClass.containsKey(cls)) {
                listByClass.put(cls, new ArrayList<>());
            }
            listByClass.get(cls).add(entity);
        }
        listByName.put(entity.getName(), entity);
        addToLayer(entity);
        BodyComponent body = entity.getComponent(BodyComponent.class);
        addToAreas(body.getAreaPosition(), entity);
    }

    public void setInAnimation(boolean value) {
        if (!inAnimation && value) {
            for (Entity e : getAllByClass(PlayerComponent.class)) {
                VelocityComponent velocity = e.getComponent(VelocityComponent.class);
                velocity.setX(0);
                velocity.setY(0);
                velocity.setAcceleration(0);
            }
        }
        inAnimation = value;
    }

    public boolean isInAnimation() {
        return inAnimation;
    }

    public boolean isAnimationJustStarted() {
        return inAnimation && !lastInAnimation;
    }

    void remove(Entity entity) {
        removeList.add(entity);
    }

    void clear() {
        inAnimation = false;
        entities.clear();
        listByClass.clear();
        addLaterList.clear();
        removeList.clear();
        listByName.clear();
        listByLayer.clear(); // is this enough?
        areas.clear(); // is this enough?
    }

    private void addToLists() {
        for (Entity entity : addLaterList) {
            addToLists(entity);
        }
        addLaterList.clear();
    }

    private void removeEntities() {
        for (Entity entity : removeList) {
            entities.remove(entity);
            for (List<Entity> entityList : listByClass.values()) {
                entityList.remove(entity);
            }
            if (entity.getGroup() != null) {
                entitiesByGroup.get(entity.getGroup()).remove(entity);
            }
            if (entity.getName() != null) {
                listByName.remove(entity.getName());
            }
            removeFromLayer(entity);
            BodyComponent body = entity.getComponent(BodyComponent.class);
            removeFromAreas(body.getAreaPosition(), entity);
        }
        removeList.clear();
    }

    public void removeFromLayer(Entity entity) {
        if (!entity.hasComponent(ViewComponent.class)) {
            return;
        }
        ViewComponent view = entity.getComponent(ViewComponent.class);
        int layer = view.getLayer();
        List<Entity> list = listByLayer.get(layer);
        list.remove(entity);
    }

    public void addToLayer(Entity entity) {
        if (!entity.hasComponent(ViewComponent.class)) {
            return;
        }
        ViewComponent view = entity.getComponent(ViewComponent.class);
        int layer = view.getLayer();
        if (!listByLayer.containsKey(layer)) {
            listByLayer.put(layer, new LinkedList<>());
            layers.add(layer);
        }
        List<Entity> list = listByLayer.get(layer);
        list.add(entity);
    }
}
