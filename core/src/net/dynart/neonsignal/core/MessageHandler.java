package net.dynart.neonsignal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dynart.neonsignal.core.listeners.MessageListener;

public class MessageHandler {

    private final Entity entity;
    private final Map<String, List<MessageListener>> listeners = new HashMap<>();

    MessageHandler(Entity entity) {
        this.entity = entity;
    }

    public void subscribe(String message, MessageListener listener) {
        if (!listeners.containsKey(message)) {
            listeners.put(message, new ArrayList<MessageListener>());
        }
        listeners.get(message).add(listener);
    }

    public void unsubscribe(String message, MessageListener listener) {
        if (listeners.containsKey(message)) {
            listeners.get(message).remove(listener);
        }
    }

    public void send(String message) {
        if (listeners.containsKey(message)) {
            for (MessageListener listener : listeners.get(message)) {
                listener.receive(entity, message);
            }
        }
    }

}
