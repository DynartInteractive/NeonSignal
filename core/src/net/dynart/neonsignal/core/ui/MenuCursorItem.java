package net.dynart.neonsignal.core.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.HashMap;
import java.util.Map;

public class MenuCursorItem {

    private final Actor actor;
    private final Map<MenuCursor.Neighbour, Actor> neighbours = new HashMap<>();
    private final Map<MenuCursor.Event, MenuCursor.Listener> listeners = new HashMap<>();
    private Object data;

    MenuCursorItem(Actor actor) {
        this.actor = actor;
    }

    public Actor getActor() {
        return actor;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setNeighbour(MenuCursor.Neighbour neighbour, Actor actor) {
        neighbours.put(neighbour, actor);
    }

    public void setListener(MenuCursor.Event event, MenuCursor.Listener listener) {
        listeners.put(event, listener);
    }

    boolean hasListener(MenuCursor.Event event) {
        return listeners.containsKey(event);
    }

    MenuCursor.Listener getListener(MenuCursor.Event event) {
        return listeners.get(event);
    }

    boolean hasNeighbour(MenuCursor.Neighbour neighbour) {
        return neighbours.containsKey(neighbour);
    }

    Actor getNeighbour(MenuCursor.Neighbour neighbour) {
        return neighbours.get(neighbour);
    }
}
